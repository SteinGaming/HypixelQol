package de.steingaming.hqol.listeners

import de.steingaming.hqol.Utilities
import de.steingaming.hqol.Utilities.cleanupColorCodes
import de.steingaming.hqol.mixins.transformers.AccessorMinecraft
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.minecraft.client.Minecraft
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
        val LEAP_REGEX = Regex("(Infinileap|Spirit Leap)")
        val S1 = AxisAlignedBB(113.0, 160.0, 48.0, 89.0, 100.0, 122.0)
        val S2 = AxisAlignedBB(91.0, 160.0, 145.0, 19.0, 100.0, 121.0)
        val S3 = AxisAlignedBB(-6.0, 160.0, 123.0, 19.0, 100.0, 50.0)
        val S4 = AxisAlignedBB(17.0, 160.0, 27.0, 90.0, 100.0, 50.0)

        val random = Random(System.currentTimeMillis())

        var currentLeap: LeapData? = null
        var currentWindow: Window? = null

        @JvmStatic
        fun openedWindow(packet: S2DPacketOpenWindow): Boolean {
            val window = Window(packet.windowId, packet.windowTitle.unformattedText.cleanupColorCodes())
            currentWindow = window
            return window.nameUnformatted == "Spirit Leap"
        }

        fun resetLeapAndWindow() {
            currentLeap = null
            currentWindow = null
        }

        @JvmStatic
        fun setSlotPacket(packet: S2FPacketSetSlot): Boolean {
            val leap = currentLeap ?: return false
            val window = currentWindow ?: return false

            if (System.currentTimeMillis() - leap.time > 500) {
                Utilities.sendToChat("Timeout reached. Either on cooldown, or ping way too high. Modify your settings!")
                resetLeapAndWindow()
                return false
            }
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

            resetLeapAndWindow()
            scope.launch {
                delay(random.nextLong(50, 100))
                Minecraft.getMinecraft().netHandler.networkManager.sendPacket(
                    C0EPacketClickWindow(
                        windowId, slot, 0, 0, null, 0
                    )
                )
            }
            return true
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

        return playerName
    }

    fun appendFastLeap(): Boolean {
        val player = Minecraft.getMinecraft().thePlayer
        val pos = player.positionVector
        val className: String = when {
            S1.isVecInside(pos) -> "archer"
            S2.isVecInside(pos) -> "archer"
            S3.isVecInside(pos) -> "mage"
            S4.isVecInside(pos) -> "healer"
            else -> "tank"
        } ?: return false
        val playerName = findPlayerByClassName(className) ?: let {
            Utilities.sendToChat("Couldn't find a player with the class \"$className\"")
            return false
        }
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
        if (appendFastLeap())
                (Minecraft.getMinecraft() as AccessorMinecraft).rightClickMouse_hqol()
    }
}