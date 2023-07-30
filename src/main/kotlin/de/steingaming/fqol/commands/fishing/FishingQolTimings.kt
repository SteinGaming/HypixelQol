package de.steingaming.fqol.commands.fishing

import de.steingaming.fqol.HypixelQol
import de.steingaming.fqol.commands.FishingQolCommand.Companion.table
import de.steingaming.fqol.config.subconfigs.FishingConfig
import net.minecraft.client.Minecraft
import net.minecraft.command.ICommandSender
import net.minecraft.util.ChatComponentText

object FishingQolTimings {
     val config: FishingConfig
         get() = HypixelQol.config.fishingConfig
     fun timings(sender: ICommandSender, args: Array<out String>) {
        fun error(msg: String) {
            sender.addChatMessage(ChatComponentText("§cError: $msg"))
        }
        when (args.getOrNull(0)?.lowercase()) {
            null -> null
            "reset" -> {
                val newConfig = FishingConfig()
                config.castRodDelay.min = newConfig.castRodDelay.min // ik this seems bad, and it is
                config.castRodDelay.max = newConfig.castRodDelay.max
                config.lavaPreCatchDelay.min = newConfig.lavaPreCatchDelay.min
                config.lavaPreCatchDelay.max = newConfig.lavaPreCatchDelay.max
                config.waterPreCatchDelay.min = newConfig.waterPreCatchDelay.min
                config.waterPreCatchDelay.max = newConfig.waterPreCatchDelay.max
                HypixelQol.saveConfig()
                sender.addChatMessage(
                    ChatComponentText(
                        "§1Done resetting the timings!"
                    )
                )
            }

            "toggle" -> {
                Minecraft.getMinecraft().thePlayer.addChatMessage(ChatComponentText(if (config.enabled) "§cFishingQol has been disabled!" else "§aFishingQol has been enabled!"))
                config.enabled = !config.enabled
                HypixelQol.saveConfig()
            }

            "lava", "water", "throw" -> let {
                val type = args.getOrNull(1)?.lowercase()?.takeIf { it == "min" || it == "max" }
                    ?: return@let error("Invalid Argument. Either use §2\"min\" §cor §2\"max\"")
                val new = args.getOrNull(2)?.toLongOrNull() ?: return@let error("No or invalid Number given")
                val oldVal: Long
                config.run {
                    when (args[0].lowercase()) {
                        "lava" -> lavaPreCatchDelay
                        "water" -> waterPreCatchDelay
                        "throw" -> castRodDelay
                        else -> throw Exception("AYO?")
                    }
                }.run {
                    when (type) {
                        "min" -> {
                            oldVal = min
                            min = new
                        }

                        "max" -> {
                            oldVal = max
                            max = new
                        }

                        else -> throw Exception("AYO?")
                    }
                }
                sender.addChatMessage(ChatComponentText("§aSet §2$type §afor §2${args[0].lowercase()} §afrom §c$oldVal §ato §9$new"))
                HypixelQol.saveConfig()
            }

            else -> null
        } ?: let {
            fun String.format(): String = if (length < 4) "${"0".repeat(3 - length)}$this" else this
            // Print Help
            sender.addChatMessage(
                ChatComponentText(
                    "§9Current Config:\n" + table(
                        arrayOf("Type", "Min", "Max"), arrayOf(
                            arrayOf(
                                "Water",
                                config.waterPreCatchDelay.min.toString().format(),
                                config.waterPreCatchDelay.max.toString().format()
                            ), arrayOf(
                                "Lava",
                                config.lavaPreCatchDelay.min.toString().format(),
                                config.lavaPreCatchDelay.max.toString().format()
                            ), arrayOf(
                                "Throw", // Thank you for trimming the chat mojang :)
                                config.castRodDelay.min.toString().format(), config.castRodDelay.max.toString().format()
                            )
                        )
                    ) + "\n" + "§5How to set a new value:\n" + "  §d/fqol timings <water/lava/post> <min/max> <new value>\n" + "§5How to reset the whole config:\n" + "  §d/fqol timings reset"

                )
            )
        }
    }
}