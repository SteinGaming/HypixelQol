package de.steingaming.fqol.commands

import de.steingaming.fqol.listeners.RiftListener
import de.steingaming.fqol.mixins.transformers.AccessorEventBus
import net.minecraft.command.ICommand
import net.minecraft.command.ICommandSender
import net.minecraft.util.BlockPos
import net.minecraft.util.ChatComponentText
import net.minecraftforge.common.MinecraftForge

class RiftQolCommand: ICommand {
    override fun getCommandName(): String {
        return "togglerift"
    }

    override fun getCommandUsage(sender: ICommandSender?): String? = "TODO!"

    override fun getCommandAliases(): List<String?>? = listOf()

    override fun processCommand(
        sender: ICommandSender?,
        args: Array<out String?>?
    ) {
        if (args != null && args.isNotEmpty()) {
            when (args[0]) {
                "ice" -> {
                    RiftListener.iceEnabled = !RiftListener.iceEnabled
                    sender?.addChatMessage(
                        ChatComponentText("${if (RiftListener.iceEnabled) "§aEnabled" else "§cDisabled"} §6Auto-Ice!")
                    )
                }
                "melon" -> {
                    RiftListener.melonEnabled = !RiftListener.melonEnabled
                    sender?.addChatMessage(
                        ChatComponentText("${if (RiftListener.melonEnabled) "§aEnabled" else "§cDisabled"} §2Auto-Heal!")
                    )
                }
            }
            return
        }

        val listeners = (MinecraftForge.EVENT_BUS as AccessorEventBus)
            .listeners_hqol

        if (listeners.containsKey(RiftListener)) {
            MinecraftForge.EVENT_BUS.unregister(RiftListener)
            sender?.addChatMessage(
                ChatComponentText("§cRemoved §rlistener from the event bus!")
            )
        } else {
            MinecraftForge.EVENT_BUS.register(RiftListener)
            sender?.addChatMessage(
                ChatComponentText("§aAdded §rlistener to the event bus!")
            )
        }
    }

    override fun canCommandSenderUseCommand(sender: ICommandSender?): Boolean = true

    override fun addTabCompletionOptions(
        sender: ICommandSender?,
        args: Array<out String?>?,
        pos: BlockPos?
    ): List<String?>? = listOf()

    override fun isUsernameIndex(args: Array<out String?>?, index: Int): Boolean = false

    override fun compareTo(other: ICommand?): Int =
        other?.commandName?.compareTo(commandName) ?: 0
}