package de.steingaming.hqol.commands

import de.steingaming.hqol.HypixelQol
import de.steingaming.hqol.helpers.DelayedExecutor
import net.minecraft.command.ICommand
import net.minecraft.command.ICommandSender
import net.minecraft.util.BlockPos

class HypixelQolCommand : ICommand {
    override fun getCommandName(): String = "hypixelqol"

    override fun getCommandUsage(sender: ICommandSender?): String? = null

    override fun getCommandAliases(): List<String> = listOf("hqol")

    override fun processCommand(
        sender: ICommandSender?,
        args: Array<out String?>?
    ) {
        DelayedExecutor(5) {
            HypixelQol.config.openConfigScreen()
        }
    }

    override fun canCommandSenderUseCommand(sender: ICommandSender?): Boolean = true

    override fun addTabCompletionOptions(
        sender: ICommandSender?,
        args: Array<out String?>?,
        pos: BlockPos?
    ): List<String> = listOf()

    override fun isUsernameIndex(args: Array<out String?>?, index: Int): Boolean = false

    override fun compareTo(other: ICommand?): Int = commandName.compareTo(other?.commandName ?: "")
}