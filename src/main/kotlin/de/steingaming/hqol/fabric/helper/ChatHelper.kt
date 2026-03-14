package de.steingaming.hqol.fabric.helper

import com.mojang.blaze3d.systems.RenderSystem
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.minecraft.client.Minecraft
import net.minecraft.network.chat.Component

object ChatHelper {
    val messageDeque = ArrayDeque<String>()

    fun sendToChat(msg: String) {
        if (!RenderSystem.isOnRenderThread()) {
            messageDeque.add(msg)
            return
        }
        Minecraft.getInstance().gui.chat.addMessage(
            Component.nullToEmpty(msg)
        )
    }

    fun init() {
        ClientTickEvents.START_CLIENT_TICK.register {
            while (messageDeque.isNotEmpty()) {
                val msg = messageDeque.removeFirst()
                it.gui.chat.addMessage(Component.nullToEmpty(msg))
            }
        }
    }
}