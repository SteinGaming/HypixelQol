package de.steingaming.fqol

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import net.minecraft.client.Minecraft
import net.minecraft.command.ICommand
import net.minecraft.command.ICommandSender
import net.minecraft.util.BlockPos
import net.minecraft.util.ChatComponentText
import net.minecraft.util.EnumParticleTypes
import kotlin.system.exitProcess

class SpassHaben: ICommand {
    override fun compareTo(other: ICommand?): Int {
        return commandName.compareTo(other?.commandName ?: return 0)
    }

    override fun getCommandName(): String = "spasshaben"

    override fun getCommandUsage(sender: ICommandSender?): String = "dont run this"

    override fun getCommandAliases(): MutableList<String> = mutableListOf()


    private var hm = false
    override fun processCommand(sender: ICommandSender?, args: Array<out String>?) {
        if (!hm) {
            hm = true
            sender?.addChatMessage(ChatComponentText("Don't."))
            return
        }
        GlobalScope.launch {
            while (true) {
                val (x, y, z) = Minecraft.getMinecraft().thePlayer.positionVector.let {
                    Triple(it.xCoord, it.yCoord, it.zCoord)
                }
                Minecraft.getMinecraft().theWorld.spawnParticle(
                    EnumParticleTypes.EXPLOSION_HUGE, x, y, z, .0, .0, .0
                )
            }
        }
    }

    override fun canCommandSenderUseCommand(sender: ICommandSender?): Boolean = true

    override fun addTabCompletionOptions(
        sender: ICommandSender?,
        args: Array<out String>?,
        pos: BlockPos?
    ): MutableList<String> = mutableListOf()

    override fun isUsernameIndex(args: Array<out String>?, index: Int): Boolean = false

}