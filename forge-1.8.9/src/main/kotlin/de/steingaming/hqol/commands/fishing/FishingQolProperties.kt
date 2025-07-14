package de.steingaming.hqol.commands.fishing

import de.steingaming.hqol.HypixelQol
import de.steingaming.hqol.commands.FishingQolCommand.Companion.table
import de.steingaming.hqol.config.HypixelQolConfig
import de.steingaming.hqol.config.HypixelQolConfig.Properties.InvalidNameException
import de.steingaming.hqol.config.HypixelQolConfig.Properties.InvalidValueException
import de.steingaming.hqol.config.subconfigs.FishingConfig
import net.minecraft.command.ICommandSender
import net.minecraft.util.ChatComponentText

object FishingQolProperties {
    val config: FishingConfig
        get() = HypixelQol.config.fishingConfig

    val properties: HypixelQolConfig.Properties<FishingConfig.FishingProperties>
        get() = config.properties.properties
    internal fun properties(sender: ICommandSender, args: Array<out String>) {
        fun error(msg: String) {
            sender.addChatMessage(ChatComponentText("§cError: $msg"))
        }
        when (args.getOrNull(0)?.lowercase()) {
            null -> null
            "set" -> let {
                try {
                    properties.set(
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
                        properties.namesList().joinToString(", ")
                    }")
                } catch (e: InvalidValueException) {
                    return@let error("Value type invalid: \n§4${e.instead}")
                }
            }
            "reset" -> let {
                try {
                    properties.reset(args.getOrNull(1) ?: return@let error("No name given"))
                    HypixelQol.saveConfig()
                    sender.addChatMessage(
                        ChatComponentText(
                            "§aSuccessfully reset ${args[1]} to §9default"
                        )
                    )
                } catch (e: InvalidNameException) {
                    return@let error("Name does not exist, valid properties: §a${
                        properties.namesList().joinToString(", ")
                    }")
                }
            }
            else -> null
        } ?: let {
            sender.addChatMessage(
                ChatComponentText(
                    table(
                        arrayOf("Name", "Value"), properties.toTableArray()
                    ) + "\n" + "§9To modify the value, use:\n" + "  §a/fqol properties set <name> <value>\n" +
                            "§9To reset a value, use:\n" +
                            "  §a/fqol properties reset <name>"
                )
            )
        }
    }
}