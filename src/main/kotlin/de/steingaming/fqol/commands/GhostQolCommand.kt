package de.steingaming.fqol.commands

import de.steingaming.fqol.HypixelQol
import net.minecraft.block.Block
import net.minecraft.command.ICommand
import net.minecraft.command.ICommandSender
import net.minecraft.util.BlockPos
import net.minecraft.util.ChatComponentText
import de.steingaming.fqol.HypixelQol.Companion.saveConfig
import de.steingaming.fqol.config.subconfigs.GhostConfig

class GhostQolCommand : ICommand {
    override fun compareTo(other: ICommand?): Int {
        return commandName.compareTo(other?.commandName ?: return 0)
    }

    override fun getCommandName(): String = "ghostqol"

    override fun getCommandUsage(sender: ICommandSender?): String = "/ghostqol [add/remove] [material]"

    override fun getCommandAliases(): MutableList<String> = mutableListOf("gqol")

    override fun processCommand(sender: ICommandSender?, args: Array<out String>?) {
        fun error(msg: String) {
            sender?.addChatMessage(ChatComponentText("§cError: $msg"))
        }
        val config = HypixelQol.config.ghostConfig
        if (sender == null) return
        if (args == null) return
        when (args.getOrNull(0)?.lowercase()) {
            null -> null
            "add" -> let {
                val input = args.getOrNull(1)?.lowercase() ?: return@let error("No input given")

                config.ignoreBlockList.add(
                    Block.getIdFromBlock(
                        Block.getBlockFromName(input) ?: return@let error("Invalid block")
                    )
                )
                saveConfig()

                sender.addChatMessage(
                    ChatComponentText("§aSuccessfully added specified block!")
                )
            }
            "remove" -> let {
                val input = args.getOrNull(1)?.lowercase() ?: return@let error("No input given")

                config.ignoreBlockList.remove(
                    Block.getIdFromBlock(
                        Block.getBlockFromName(input) ?: return@let error("Invalid block")
                    )
                )
                saveConfig()

                sender.addChatMessage(
                    ChatComponentText("§aSuccessfully removed specified block!")
                )
            }
            "reset" -> let {
                config.ignoreBlockList.clear()
                config.ignoreBlockList.addAll(GhostConfig().ignoreBlockList)
                saveConfig()
                sender.addChatMessage(
                    ChatComponentText("§aSuccessfully reset ignored blocks to default!")
                )
            }

            else -> null
        } ?: let {
            sender.addChatMessage(
                ChatComponentText(
                "§9Currently blocked blocks:\n" +
                            config.ignoreBlockList.joinToString("\n") {
                                " §2" + Block.getBlockById(it).registryName.uppercase()
                            } + "\n" +
                      "§9Command Usage:\n" +
                      "   §a/fqolghost <add/remove/reset> [block]"
                )
            )
        }
    }

    override fun canCommandSenderUseCommand(sender: ICommandSender?): Boolean = true

    override fun addTabCompletionOptions(
        sender: ICommandSender?,
        args: Array<out String>?,
        pos: BlockPos?
    ): MutableList<String> {
        if ((args?.size ?: 0) > 2) return mutableListOf()
        val config = HypixelQol.config.ghostConfig

        val input = args?.getOrNull(1)
        return when (args?.getOrNull(0)?.lowercase()) {
            "add" -> {
                if (args.getOrNull(1) == null) return mutableListOf()
                Block.blockRegistry.mapNotNull {
                    if (Block.getIdFromBlock(it) in config.ignoreBlockList) null
                    else it.registryName.lowercase()
                }.let {
                    if (input != null)
                        it.filter { s ->
                            s.startsWith(input, true)
                        }
                    else it
                }.toMutableList()
            }

            "remove" -> {
                if (args.getOrNull(1) == null) return mutableListOf()
                config.ignoreBlockList.map {
                    Block.getBlockById(it).registryName.lowercase()
                }.let {
                    if (input != null)
                        it.filter { s ->
                            s.startsWith(input, true)
                        }
                    else it
                }.toMutableList().toMutableList()
            }

            "", null -> mutableListOf("add", "remove", "reset")
            else -> mutableListOf("add", "remove", "reset").filter {
                it.startsWith(args[0], true)
            }.toMutableList()
        }
    }

    override fun isUsernameIndex(args: Array<out String>?, index: Int): Boolean = false
}