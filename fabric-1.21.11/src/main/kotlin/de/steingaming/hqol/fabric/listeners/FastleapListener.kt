package de.steingaming.hqol.fabric.listeners

import de.steingaming.hqol.fabric.HypixelQolFabric
import de.steingaming.hqol.fabric.Utilities
import de.steingaming.hqol.fabric.Utilities.cleanupColorCodes
import de.steingaming.hqol.fabric.config.categories.Fastleap
import de.steingaming.hqol.fabric.mixins.MinecraftClientInvoker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.minecraft.client.MinecraftClient
import net.minecraft.network.packet.s2c.play.InventoryS2CPacket
import net.minecraft.network.packet.s2c.play.OpenScreenS2CPacket
import net.minecraft.screen.slot.SlotActionType
import net.minecraft.sound.SoundCategory
import net.minecraft.sound.SoundEvents
import net.minecraft.util.math.Box
import kotlin.random.Random

object FastleapListener {
    data class Window(val id: Int, val nameUnformatted: String)
    data class LeapData(val name: String)

    val scope = CoroutineScope(Dispatchers.Default)
    val LEAP_REGEX = Regex("(Infini[lL]eap|Spirit Leap)")
    val S1 = Box(113.0, 160.0, 48.0, 89.0, 100.0, 122.0)
    val S2 = Box(91.0, 160.0, 145.0, 19.0, 100.0, 121.0)
    val S3 = Box(-6.0, 160.0, 123.0, 19.0, 100.0, 50.0)
    val S4 = Box(17.0, 160.0, 27.0, 90.0, 100.0, 50.0)
    val S4_CORE = Box(65.0, 160.0, 27.0, 43.0, 100.0, 96.0)

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
    fun openedWindow(packet: OpenScreenS2CPacket): Boolean {
        currentLeap ?: return false
        val window = Window(packet.syncId, packet.name.string.cleanupColorCodes())
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
    fun setSlotPacket(packet: InventoryS2CPacket): Boolean {
        val leap = currentLeap ?: return false
        val window = currentWindow ?: return false

        val windowId = packet.syncId
        if (windowId != window.id || window.nameUnformatted != "Spirit Leap") return false

        var (slot, itemStack) = packet.contents!!.withIndex().find { (i, item) ->
            listOf(item.name, item.formattedName, item.customName)
                .any {
                    (it?.literalString ?: it?.string)?.cleanupColorCodes()
                        ?.lowercase() == leap.name
                }
        } ?: return false

        debugMessage("${itemStack.name.string.cleanupColorCodes().lowercase()} matched, resetting and pressing $slot for $windowId")

        resetLeapAndWindow()
        scope.launch {
            val mc = MinecraftClient.getInstance()
            val player = mc.player!!
            delay(random.nextLong(config.timings.lower.toLong(), config.timings.upper.toLong()).also { debugMessage("Waiting $it ms") })
            debugMessage("Sending Packet")
            debugMessage("Window ID: ${mc.player?.currentScreenHandler?.syncId}; Item name for slot: ${player.currentScreenHandler?.getSlot(slot)?.stack?.name?.string?.cleanupColorCodes()?.lowercase()}")
            Utilities.clickSlotUnchecked(
                windowId, slot, 0, SlotActionType.PICKUP, player
            )

            if (config.playNoise) {
                debugMessage("Playing sound")
                mc.world!!.playSound(
                    null, player.x, player.y, player.z, SoundEvents.BLOCK_NOTE_BLOCK_PLING, SoundCategory.MASTER, 1f, 1f
                )
            }
        }
        return combineWithConfigOption(true)
    }


    fun findPlayerByClassName(className: String): String? {
        val regex = Regex("\\[[0-9]+] (?<playerName>.+?) .*\\(${className.lowercase()} .+\\)")
        val playerName = MinecraftClient.getInstance().networkHandler?.playerList?.mapNotNull {
            val displayName = it.displayName ?: return@mapNotNull null
            displayName.string.cleanupColorCodes().lowercase()
        }?.mapNotNull {
            val playerNameIsolated = regex.toPattern().matcher(it)
            if (!playerNameIsolated.find()) null
            else playerNameIsolated.group("playerName")
        }?.firstOrNull {
            it != MinecraftClient.getInstance().player?.gameProfile?.name?.lowercase()
        } // TODO check duplicates classes
        debugMessage("findPlayerByClassName res: $playerName")

        return playerName
    }

    fun appendFastLeap(): Boolean {
        val player = MinecraftClient.getInstance().player ?: return false
        val pos = player.entityPos

        val classes = config.classes
        debugMessage(
            "Which sector? ${
                when {
                    S1.contains(pos) -> "S1"
                    S2.contains(pos) -> "S2"
                    S3.contains(pos) -> "S3"
                    S4.contains(pos) || S4_CORE.contains(pos) -> "S4"
                    else -> "None, defaulting"
                }
            }"
        )
        val className: String = when {
            S1.contains(pos) -> classes.S1
            S2.contains(pos) -> classes.S2
            S3.contains(pos) -> classes.S3
            S4.contains(pos) || S4_CORE.contains(pos) -> classes.S4
            else -> classes.default
        }
        if (className.trim() == "") return false.also { debugMessage("No class set, returning false") }
        val playerName = findPlayerByClassName(className) ?: let {
            Utilities.sendToChat("Couldn't find a player with the class \"$className\"")
            return false
        }
        debugMessage("Match found: $playerName")
        currentLeap = LeapData(playerName)
        return true
    }
    @JvmStatic
    fun punchEvent(): Boolean {
        val player = MinecraftClient.getInstance().player ?: return false

        val name = player.mainHandStack?.name?.string?.cleanupColorCodes() ?: return false
        if (!LEAP_REGEX.matches(name)) return false
        if (appendFastLeap()) {
            debugMessage("Emulating right click")
            (MinecraftClient.getInstance() as MinecraftClientInvoker).doItemUse_hqol()
            return true
        }
        return false
    }
}