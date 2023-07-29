package de.steingaming.fqol.commands

import de.steingaming.fqol.FishingQol
import de.steingaming.fqol.FishingQolCommand.Companion.table
import de.steingaming.fqol.FishingQolConfig
import net.minecraft.command.ICommandSender
import net.minecraft.util.ChatComponentText

object FishingQolProperties {
    internal fun properties(sender: ICommandSender, args: Array<out String>) {
        fun error(msg: String) {
            sender.addChatMessage(ChatComponentText("§cError: $msg"))
        }
        when (args.getOrNull(0)?.lowercase()) {
            null -> null
            "set" -> let {
                try {
                    FishingQol.config.properties.set(
                        args.getOrNull(1) ?: return@let error("No name given"),
                        args.getOrNull(2) ?: return@let error("No value given")
                    )
                } catch (e: FishingQolConfig.FishingProperties.InvalidNameException) {
                    return@let error("Name does not exist, valid properties: §a${
                        FishingQol.config.properties::class.java.declaredFields.joinToString(", ") {
                            it.name
                        }
                    }")
                } catch (e: FishingQolConfig.FishingProperties.InvalidValueException) {
                    return@let error("Value type invalid: \n§4${e.instead}")
                }
                FishingQol.saveConfig()
                sender.addChatMessage(
                    ChatComponentText(
                        "§aSuccessfully set ${args[1]} to ${args[2]}"
                    )
                )
            }
            "reset" -> let {
                try {
                    FishingQol.config.properties.reset(args.getOrNull(1) ?: return@let error("No name given"))
                    sender.addChatMessage(
                        ChatComponentText(
                            "§aSuccessfully reset ${args[1]} to §9default"
                        )
                    )
                } catch (e: FishingQolConfig.FishingProperties.InvalidNameException) {
                    return@let error("Name does not exist, valid properties: §a${
                        FishingQol.config.properties::class.java.declaredFields.joinToString(", ") {
                            it.name
                        }
                    }")
                }
            }
            else -> null
        } ?: let {
            sender.addChatMessage(
                ChatComponentText(
                    table(
                        arrayOf("Name", "Value"), FishingQol.config.properties.toTableArray()
                    ) + "\n" + "§9To modify the value, use:\n" + "  §a/fqol properties set <name> <value>\n" +
                            "§9To reset a value, use:\n" +
                            "  §a/fqol properties reset <name>"
                )
            )
        }
    }
}