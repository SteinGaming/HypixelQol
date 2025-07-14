package de.steingaming.hqol.commands

import de.steingaming.hqol.brush.Brush
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import net.minecraft.command.ICommand
import net.minecraft.command.ICommandSender
import net.minecraft.util.BlockPos

class ApplyBrushConfig: ICommand {
    override fun compareTo(other: ICommand?): Int {
        return commandName.compareTo(other?.commandName ?: "")
    }

    override fun getCommandName(): String = "applybrushcfg"

    override fun getCommandUsage(sender: ICommandSender?): String = "/applybrushcfg"

    override fun getCommandAliases(): MutableList<String> = mutableListOf()

    override fun processCommand(sender: ICommandSender?, args: Array<out String>?) {
        GlobalScope.launch {
            Brush.applyM7()
        }
    }

    override fun canCommandSenderUseCommand(sender: ICommandSender?): Boolean = true

    override fun addTabCompletionOptions(
        sender: ICommandSender?,
        args: Array<out String>?,
        pos: BlockPos?
    ): MutableList<String> {
        return mutableListOf()
    }

    override fun isUsernameIndex(args: Array<out String>?, index: Int): Boolean {
        return false
    }
}