package de.steingaming.hqol.listeners

import de.steingaming.hqol.HypixelQol
import de.steingaming.hqol.Utilities
import de.steingaming.hqol.Utilities.cleanupColorCodes
import de.steingaming.hqol.config.subconfigs.FastLeapConfig
import de.steingaming.hqol.mixins.transformers.AccessorMinecraft
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.minecraft.client.Minecraft
import net.minecraft.client.audio.ISound
import net.minecraft.client.audio.PositionedSound
import net.minecraft.network.play.client.C0DPacketCloseWindow
import net.minecraft.network.play.client.C0EPacketClickWindow
import net.minecraft.network.play.server.S2DPacketOpenWindow
import net.minecraft.network.play.server.S2FPacketSetSlot
import net.minecraft.util.AxisAlignedBB
import net.minecraftforge.client.event.MouseEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import kotlin.random.Random

class LeapListener {
    data class Window(val id: Int, val nameUnformatted: String)
    data class LeapData(val name: String, val time: Long = System.currentTimeMillis())

    companion object {
        val scope = CoroutineScope(Dispatchers.Default)
        val LEAP_REGEX = Regex("(Infini[lL]eap|Spirit Leap)")
        val S1 = AxisAlignedBB(113.0, 160.0, 48.0, 89.0, 100.0, 122.0)
        val S2 = AxisAlignedBB(91.0, 160.0, 145.0, 19.0, 100.0, 121.0)
        val S3 = AxisAlignedBB(-6.0, 160.0, 123.0, 19.0, 100.0, 50.0)
        val S4 = AxisAlignedBB(17.0, 160.0, 27.0, 90.0, 100.0, 50.0)
        val S4_CORE = AxisAlignedBB(65.0, 160.0, 27.0, 43.0, 100.0, 96.0)

        val config: FastLeapConfig
            get() = HypixelQol.config.fastLeapConfig

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
        fun openedWindow(packet: S2DPacketOpenWindow): Boolean {
            val leap = currentLeap ?: return false
            if (config.timeout != 0f && System.currentTimeMillis() - leap.time > config.timeout) {
                Utilities.sendToChat("Timeout reached. Either on cooldown, or ping way too high. Modify your settings!")
                resetLeapAndWindow()
                return false
            }
            val window = Window(packet.windowId, packet.windowTitle.unformattedText.cleanupColorCodes())
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
        fun setSlotPacket(packet: S2FPacketSetSlot): Boolean {
            val leap = currentLeap ?: return false
            val window = currentWindow ?: return false

            val itemStack = packet.func_149174_e()
            val slot = packet.func_149173_d()
            val windowId = packet.func_149175_c()
            if (windowId != window.id || window.nameUnformatted != "Spirit Leap") return false

            if (slot > 35) {
                Utilities.sendToChat("§cExceeded amount of slots for window. Couldn't find ${leap.name}!")
                Minecraft.getMinecraft().netHandler.networkManager.sendPacket(
                    C0DPacketCloseWindow(window.id)
                )
                resetLeapAndWindow()
                return false
            }

            if (itemStack.displayName.cleanupColorCodes().lowercase() != leap.name)
                return false

            debugMessage("${itemStack.displayName.cleanupColorCodes().lowercase()} matched, resetting and pressing $slot for $windowId")

            resetLeapAndWindow()
            scope.launch {
                val mc = Minecraft.getMinecraft()
                val player = mc.thePlayer
                delay(random.nextLong(config.timings.lower.toLong(), config.timings.upper.toLong()).also { debugMessage("Waiting $it ms") })
                debugMessage("Sending Packet")
                debugMessage("Window ID: ${player.openContainer?.windowId}; Item name for slot: ${player.openContainer?.getSlot(slot)?.stack?.displayName?.cleanupColorCodes()?.lowercase()}")
                mc.netHandler.networkManager.sendPacket(
                    C0EPacketClickWindow(
                        windowId, slot, 0, 0, null, 0
                    )
                )
                if (config.playNoise) {
                    debugMessage("Playing sound")
                    mc.theWorld.playSound(
                        player.posX, player.posY, player.posZ, "note.pling", 1f, 1f, false
                    )
                }
            }
            return combineWithConfigOption(true)
        }
    }


    fun findPlayerByClassName(className: String): String? {
        val regex = Regex("\\[[0-9]+] (?<playerName>.+?) .*\\(${className.lowercase()} .+\\)")
        val playerName = Minecraft.getMinecraft().netHandler.playerInfoMap.mapNotNull {
            val displayName = it.displayName ?: return@mapNotNull null
            displayName.unformattedText.cleanupColorCodes().lowercase()
        }.mapNotNull {
            val playerNameIsolated = regex.toPattern().matcher(it)
            if (!playerNameIsolated.find()) null
            else playerNameIsolated.group("playerName")
        }.firstOrNull {
            it != Minecraft.getMinecraft().thePlayer.gameProfile.name.lowercase()
        } // TODO check duplicates classes
        debugMessage("findPlayerByClassName res: $playerName")

        return playerName
    }

    fun appendFastLeap(): Boolean {
        val player = Minecraft.getMinecraft().thePlayer
        val pos = player.positionVector

        val classes = config.classes
        debugMessage("Which sector? ${
            when {
                S1.isVecInside(pos) -> "S1"
                S2.isVecInside(pos) -> "S2"
                S3.isVecInside(pos) -> "S3"
                S4.isVecInside(pos) || S4_CORE.isVecInside(pos) -> "S4"
                else -> "None, defaulting"
            }
        }")
        val className: String = when {
            S1.isVecInside(pos) -> classes.S1
            S2.isVecInside(pos) -> classes.S2
            S3.isVecInside(pos) -> classes.S3
            S4.isVecInside(pos) || S4_CORE.isVecInside(pos) -> classes.S4
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

    @SubscribeEvent
    fun punchEvent(e: MouseEvent) {
        if (e.button != 0 || !e.buttonstate) return
        val player = Minecraft.getMinecraft().thePlayer ?: return

        val name = player.heldItem?.displayName?.cleanupColorCodes() ?: return
        if (!LEAP_REGEX.matches(name)) return
        e.isCanceled = true
        if (appendFastLeap()) {
            debugMessage("Emulating right click")
            (Minecraft.getMinecraft() as AccessorMinecraft).rightClickMouse_hqol()
        }
    }
}