package de.steingaming.hqol.fabric.helper

import com.mojang.blaze3d.systems.RenderSystem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.minecraft.client.Minecraft
import net.minecraft.network.chat.Component
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

object ChatHelper {
    val messageDeque = ArrayDeque<Component>()

    fun sendToChat(msg: Component) {
        if (!RenderSystem.isOnRenderThread()) {
            messageDeque.add(msg)
            return
        }
        //? if >= 26.2 {
        Minecraft.getInstance().gui.hud.chat.addServerSystemMessage(
        //? } else if >= 26.1 {
        //Minecraft.getInstance().gui.chat.addServerSystemMessage(
        //?} else
        //Minecraft.getInstance().gui.chat.addMessage(
            msg
        )
    }

    fun sendToChat(msg: String) =
        sendToChat(Component.nullToEmpty(msg))

    fun init() {
        ClientTickEvents.START_CLIENT_TICK.register {
            while (messageDeque.isNotEmpty()) {
                val msg = messageDeque.removeFirst()
                sendToChat(msg)
            }
        }
    }

    operator fun Component?.plus(other: String): Component {
        return Component.empty().let {
            it.append(this ?: Component.literal("null"))
            it.append(other)
        }
    }

    fun CoroutineScope.launchWithSafeguard(context: CoroutineContext = EmptyCoroutineContext,
                                           start: CoroutineStart = CoroutineStart.DEFAULT,
                                           block: suspend CoroutineScope.() -> Unit): Job =
        this.launch(context, start) {
            runCatching {
                block()
            }.onFailure {
                it.printStackTrace()
                sendToChat("§cThere was an error when running an coroutine in HypixelQol! Check logs and report to https://github.com/SteinGaming/HypixelQol if persists/stuff breaks.")
            }
        }

}