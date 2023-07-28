package de.steingaming.fqol

import de.steingaming.fqol.FishingQol.Companion.config
import de.steingaming.fqol.FishingQol.Companion.resetConfig
import de.steingaming.fqol.FishingQol.Companion.saveConfig
import net.minecraft.client.Minecraft
import net.minecraft.command.ICommand
import net.minecraft.command.ICommandSender
import net.minecraft.util.BlockPos
import net.minecraft.util.ChatComponentText
import java.text.DecimalFormat
import java.text.NumberFormat
import kotlin.math.max

class FishingQolCommand : ICommand {

    override fun compareTo(other: ICommand?): Int {
        return commandName.compareTo(other?.commandName ?: return 0)
    }

    override fun getCommandName(): String = "fqol"

    override fun getCommandUsage(sender: ICommandSender?): String = "/fqol"

    override fun getCommandAliases(): MutableList<String> = mutableListOf()

    override fun processCommand(sender: ICommandSender?, args: Array<out String>?) {
        fun error(msg: String) {
            sender?.addChatMessage(ChatComponentText("§cError: $msg"))
        }
        if (sender == null) return
        if (args == null) return
        when (args.getOrNull(0)?.lowercase()) {
            null -> null
            "reset" -> {
                resetConfig()
                saveConfig ()
                sender.addChatMessage(
                    ChatComponentText(
                        "§1Done resetting the config!"
                    )
                )
            }
            "toggle" -> {
                Minecraft.getMinecraft().thePlayer.addChatMessage(ChatComponentText(if (config.enabled) "§cFishingQol has been disabled!" else "§aFishingQol has been enabled!"))
                config.enabled = !config.enabled
                saveConfig()
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
                saveConfig()
            }

            else -> null
        } ?: let {
            fun String.format(): String = if (length < 4) "${"0".repeat(3 - length)}$this" else this
            // Print Help
            sender.addChatMessage(
                ChatComponentText(
                    "§9Current Config:\n" +
                            table(
                                arrayOf("Type", "Min", "Max"),
                                arrayOf(
                                    arrayOf(
                                        "Water",
                                        config.waterPreCatchDelay.min.toString().format(),
                                        config.waterPreCatchDelay.max.toString().format()
                                    ),
                                    arrayOf(
                                        "Lava",
                                        config.lavaPreCatchDelay.min.toString().format(),
                                        config.lavaPreCatchDelay.max.toString().format()
                                    ),
                                    arrayOf(
                                        "Throw", // Thank you for trimming the chat mojang :)
                                        config.castRodDelay.min.toString().format(),
                                        config.castRodDelay.max.toString().format()
                                    )
                                )
                            ) +
                            "\n" +
                            "§5How to set a new value:\n" +
                            "  §d/fqol <water/lava/post> <min/max> <new value>\n" +
                            "§5How to reset the whole config:\n" +
                            "  §d/fqol reset"

                )
            )
        }
    }

    override fun canCommandSenderUseCommand(sender: ICommandSender?): Boolean = true

    override fun addTabCompletionOptions(
        sender: ICommandSender?,
        args: Array<out String>?,
        pos: BlockPos?
    ): MutableList<String> = mutableListOf()

    override fun isUsernameIndex(args: Array<out String>?, index: Int): Boolean = false


    fun <T> Array<Array<T>>.getRow(row: Int): List<T> {
        val list = mutableListOf<T>()
        var i = 0
        while (true) {
            list.add(getOrNull(i++)?.get(row) ?: return list)
        }
    }

    fun <T> Array<Array<T>>.rowed(): List<List<T>> {
        val list = mutableListOf<List<T>>()
        var row = 0
        while (true) {
            list.add(
                try {
                    getRow(row++)
                } catch (e: Exception) {
                    return list
                }
            )
        }
    }

    private fun table(names: Array<String>, values: Array<Array<String>>): String {
        val valuesRowed = values.rowed()

        val namesMax = names.maxOf { it.length + 1 } // +1 for a space
        val valuesMax = valuesRowed.map { it.maxOf { i -> i.length } }
        val valuesMin = valuesRowed.map { it.minOf { i -> i.length } }
        val max = namesMax + values.maxOf { it.sumOf { i -> i.length } }
        println("Names Max: $namesMax")
        println("Values Max: $valuesMax")
        println("Max: $max")
        return buildString {
            operator fun String.unaryPlus () = appendLine("|$this${" ".repeat(max(1, max - length))}|")
            operator fun String.unaryMinus() = appendLine(this)
            -"-".repeat(max)
            var i = 0
            +names.joinToString("|", postfix = " ") { name -> " $name${" ".repeat(max(1, valuesMax[i++] - name.length))}" }
            -"| ${"=".repeat(max - 2)} |"
            for ((columnIndex, column) in values.withIndex()) {
                -buildString {
                    append("|")
                    for ((index, value) in column.withIndex()) {
                        append(" $value${if (index == 0 && valuesMin[index] == value.length) " " else ""}${" ".repeat(max(1, valuesMax[index] - value.length))}|")
                    }
                }
            }
            -"-".repeat(max)
        }.replace("[-=|]".toRegex()) {
            "§a${it.value}§2"
        }
    }


}