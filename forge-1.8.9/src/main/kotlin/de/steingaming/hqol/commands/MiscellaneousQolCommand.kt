package de.steingaming.hqol.commands

import de.steingaming.hqol.HypixelQol
import de.steingaming.hqol.Utilities
import de.steingaming.hqol.commands.fishing.FishingQolProperties
import de.steingaming.hqol.config.HypixelQolConfig.Properties.InvalidNameException
import de.steingaming.hqol.config.HypixelQolConfig.Properties.InvalidValueException
import de.steingaming.hqol.config.subconfigs.MiscellaneousConfig
import net.minecraft.command.ICommand
import net.minecraft.command.ICommandSender
import net.minecraft.util.BlockPos
import net.minecraft.util.ChatComponentText

class MiscellaneousQolCommand: ICommand {
    val config: MiscellaneousConfig
        get() = HypixelQol.config.miscConfig

    override fun compareTo(other: ICommand?): Int {
        return commandName.compareTo(other?.commandName ?: return 0)
    }

    override fun getCommandName(): String = "miscqol"

    override fun getCommandUsage(sender: ICommandSender?): String = "/miscqol"

    override fun getCommandAliases(): MutableList<String> = mutableListOf()

    override fun processCommand(sender: ICommandSender?, args: Array<out String>?) {
        if (sender == null || args == null) return
        fun error(msg: String) {
            sender.addChatMessage(ChatComponentText("§cError: $msg"))
        }
        when (args.getOrNull(0)) {
            null, "" -> null
            "set" -> let {
                try {
                    config.properties.set(
                        args.getOrNull(1) ?: return@let error("No name given"),
                        args.getOrNull(2) ?: return@let error("No value given")
                    )
                    HypixelQol.saveConfig()
                    sender.addChatMessage(
                        ChatComponentText(
                            "§aSuccessfully set ${args[1]} to ${args[2]}"
                        )
                    )
                } catch (e: InvalidNameException) {
                    return@let error("Name does not exist, valid properties: §a${
                        FishingQolProperties.config.properties::class.java.declaredFields.joinToString(", ") {
                            it.name
                        }
                    }")
                } catch (e: InvalidValueException) {
                    return@let error("Value type invalid: \n§4${e.instead}")
                }
            }
            "reset" -> let {
                try {
                    config.properties.reset(args.getOrNull(1) ?: return@let error("No name given"))
                    sender.addChatMessage(
                        ChatComponentText(
                            "§aSuccessfully reset ${args[1]} to §9default"
                        )
                    )
                } catch (e: InvalidNameException) {
                    return@let error("Name does not exist, valid properties: §a${
                        config.properties.namesList().joinToString(", ")
                    }")
                }
            }
            else -> null
        } ?: let {
            sender.addChatMessage(
                ChatComponentText(
                    "§9Current Values:" +
                            config.properties.keyValueSet().entries.joinToString("\n", "\n", "\n") {
                                "  §a${it.key}: ${it.value}"
                            } + "§9To modify/reset values use:\n  §a/miscqol <set> <name> <value>\n  §a/miscqol <reset> <name>"
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
        if (sender == null || args == null) return mutableListOf()

        val options = Utilities.options(args)

        return options(listOf("set", "reset"), args.getOrNull(0), 0) {
            options(config.properties.namesList(), args.getOrNull(1), 1) {
                mutableListOf()
            }
        }
    }

    override fun isUsernameIndex(args: Array<out String>?, index: Int): Boolean = false
}