package de.steingaming.hqol.fabric.listeners

import de.steingaming.hqol.fabric.HypixelQolFabric
import de.steingaming.hqol.fabric.Utilities
import de.steingaming.hqol.fabric.Utilities.cleanupColorCodes
import de.steingaming.hqol.fabric.config.categories.Fastleap
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.minecraft.client.Minecraft
import net.minecraft.network.protocol.game.ClientboundContainerSetContentPacket
import net.minecraft.network.protocol.game.ClientboundOpenScreenPacket
import net.minecraft.sounds.SoundEvents
import net.minecraft.sounds.SoundSource
import net.minecraft.world.inventory.ClickType
import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.Vec3
import kotlin.random.Random

object FastleapListener {
    data class Window(val id: Int, val nameUnformatted: String)
    data class LeapData(val name: String, val time: Long)

    val scope = CoroutineScope(Dispatchers.Default)
    val LEAP_REGEX = Regex("(Infini[lL]eap|Spirit Leap)")
    val S1 = AABB(113.0, 160.0, 48.0, 89.0, 100.0, 122.0)
    val S2 = AABB(91.0, 160.0, 145.0, 19.0, 100.0, 121.0)
    val S3 = AABB(-6.0, 160.0, 123.0, 19.0, 100.0, 50.0)
    val S4 = AABB(17.0, 160.0, 27.0, 90.0, 100.0, 50.0)
    val S4_CORE = AABB(65.0, 160.0, 27.0, 43.0, 100.0, 96.0)

    val config: Fastleap
        get() = HypixelQolFabric.INSTANCE.configManager.config.fastleap

    fun debugMessage(msg: String) {
        if (config.debug)
            Utilities.sendToChat(msg)
    }

    val random = Random(System.currentTimeMillis())

    var currentLeap: LeapData? = null
    var currentWindow: Window? = null

    fun combineWithConfigOption(other: Boolean): Boolean =
        config.hideUI && other

    @JvmStatic
    fun openedWindow(packet: ClientboundOpenScreenPacket): Boolean {
        currentLeap ?: return false
        val window = Window(packet.containerId, packet.title.string.cleanupColorCodes())
        currentWindow = window
        debugMessage("Is Spirit Leap with queued? ${window.nameUnformatted == "Spirit Leap" && currentLeap != null}")
        return combineWithConfigOption(window.nameUnformatted == "Spirit Leap" && currentLeap != null)
    }

    fun resetLeapAndWindow() {
        debugMessage("Resetting leap and window to null")
        currentLeap = null
        currentWindow = null
    }

    @JvmStatic
    fun setSlotPacket(packet: ClientboundContainerSetContentPacket): Boolean {
        val leap = currentLeap ?: return false
        val window = currentWindow ?: return false

        val windowId = packet.containerId
        if (windowId != window.id || window.nameUnformatted != "Spirit Leap") return false
        debugMessage("Timeout diff: ${(leap.time + config.timeout * 1000.0f).toInt() - System.currentTimeMillis()}")
        if (config.timeout != 0.0f && leap.time + (config.timeout * 1000.0f).toInt() < System.currentTimeMillis()) {
            Utilities.sendToChat("Timeout of ${config.timeout}s reached! Not interacting.")
            resetLeapAndWindow()
            return false
        }

        var (slot, itemStack) = packet.items!!.withIndex().find { (i, item) ->
            listOf(item.hoverName, item.displayName, item.customName)
                .any {
                    (it?.tryCollapseToString() ?: it?.string)?.cleanupColorCodes()
                        ?.lowercase() == leap.name
                }
        } ?: return false

        debugMessage("${itemStack.hoverName.string.cleanupColorCodes().lowercase()} matched, resetting and pressing $slot for $windowId")

        resetLeapAndWindow()
        scope.launch {
            val mc = Minecraft.getInstance()
            val player = mc.player!!
            delay(random.nextLong(config.timings.lower.toLong(), config.timings.upper.toLong()).also { debugMessage("Waiting $it ms") })
            debugMessage("Sending Packet")
            debugMessage("Window ID: ${mc.player?.containerMenu?.containerId}; Item name for slot: ${player.containerMenu?.getSlot(slot)?.item?.hoverName?.string?.cleanupColorCodes()?.lowercase()}")
            Utilities.clickSlotUnchecked(
                windowId, slot, 0, ClickType.PICKUP, player
            )

            if (config.playNoise) {
                debugMessage("Playing sound")
                mc.level!!.playSound(
                    null, player.x, player.y, player.z, SoundEvents.NOTE_BLOCK_PLING, SoundSource.MASTER, 1f, 1f
                )
            }
        }.invokeOnCompletion {
            it ?: return@invokeOnCompletion
            Utilities.sendToChat("§cError when executing leap scheduler, view logs!")
            it.printStackTrace()
        }
        return combineWithConfigOption(true)
    }


    fun findPlayerByClassName(className: String): String? {
        val regex = Regex("\\[[0-9]+] (?<playerName>.+?) .*\\(${className.lowercase()} .+\\)")
        val playerName = Minecraft.getInstance().connection?.onlinePlayers?.mapNotNull {
            val displayName = it.tabListDisplayName ?: return@mapNotNull null
            displayName.string.cleanupColorCodes().lowercase()
        }?.mapNotNull {
            val playerNameIsolated = regex.toPattern().matcher(it)
            if (!playerNameIsolated.find()) null
            else playerNameIsolated.group("playerName")
        }?.firstOrNull {
            it != Minecraft.getInstance().player?.gameProfile?.name?.lowercase()
        } // TODO check duplicates classes
        debugMessage("findPlayerByClassName res: $playerName")

        return playerName
    }

    fun positionToSectorAndClass(pos: Vec3): Pair<String, String> {
        val classes = config.classes
        return when {
            S1.contains(pos) -> "S1" to classes.S1
            S2.contains(pos) -> "S2" to classes.S2
            S3.contains(pos) -> "S3" to classes.S3
            S4.contains(pos) || S4_CORE.contains(pos) -> "S4" to classes.S4
            else -> "None" to classes.default
        }
    }

    fun appendFastLeap(): Boolean {
        val player = Minecraft.getInstance().player ?: return false
        val pos = player.position()

        val (sector, selectedClass) = positionToSectorAndClass(pos)
        debugMessage("Which sector? $sector")
        if (selectedClass == "none") {
            debugMessage("No class set, canceling")
            return false
        }
        val playerName = findPlayerByClassName(selectedClass) ?: let {
            Utilities.sendToChat("Couldn't find a player with the class \"$selectedClass\"")
            return false
        }
        debugMessage("Match found: $playerName")
        currentLeap = LeapData(playerName, System.currentTimeMillis())
        return true
    }
    @JvmStatic
    fun punchEvent(): Boolean {
        if (!config.enabled) return false
        val player = Minecraft.getInstance().player ?: return false

        val name = player.mainHandItem?.hoverName?.string?.cleanupColorCodes() ?: return false
        if (!LEAP_REGEX.matches(name)) return false
        if (currentLeap?.time?.let { it > System.currentTimeMillis() + 2000 } ?: false) return false
        if (appendFastLeap()) {
            debugMessage("Emulating right click")
            Minecraft.getInstance().startUseItem()
            return true
        }
        return false
    }
}