package de.steingaming.fqol

import de.steingaming.fqol.commands.FishingQolProperties.properties
import de.steingaming.fqol.commands.FishingQolTimings.timings
import net.minecraft.command.ICommand
import net.minecraft.command.ICommandSender
import net.minecraft.util.BlockPos
import net.minecraft.util.ChatComponentText
import kotlin.math.max

class FishingQolCommand : ICommand {

    override fun compareTo(other: ICommand?): Int {
        return commandName.compareTo(other?.commandName ?: return 0)
    }

    override fun getCommandName(): String = "fqol"

    override fun getCommandUsage(sender: ICommandSender?): String = "/fqol"

    override fun getCommandAliases(): MutableList<String> = mutableListOf()


    override fun processCommand(sender: ICommandSender?, args: Array<out String>?) {
        if (sender == null) return
        if (args == null) return
        when (args.getOrNull(0)?.lowercase()) {
            null -> null
            "timings" -> timings(sender, args.drop(1).toTypedArray())
            "properties" -> properties(sender, args.drop(1).toTypedArray())
            else -> null
        } ?: let {
            sender.addChatMessage(
                ChatComponentText(
                    "§9To configure timings, use /fqol §ctimings\n" +
                            "§9To configure properties, use /fqol §cproperties"
                )
            )
        }
    }

    override fun addTabCompletionOptions(
        sender: ICommandSender?, args: Array<out String>?, pos: BlockPos?
    ): MutableList<String> {
        if (sender == null || args == null) return mutableListOf()

        fun options(
            options: List<String>,
            input: String?,
            position: Int,
            runner: (String) -> MutableList<String>
        ): MutableList<String> {
            return when {
                input == null || input == "" -> null

                input.lowercase() in options -> {
                    if (args.getOrNull(position + 1) != null)
                        runner(input)
                    else options.toMutableList()
                }

                else -> if (args.size < (position + 2)) options.filter {
                    it.startsWith(args[position], true)
                }.toMutableList() else mutableListOf()
            } ?: options.toMutableList()
        }

        return options(listOf("timings", "properties"), args.getOrNull(0), 0) {
            when (it.lowercase()) {
                "timings" -> {
                    options(listOf("water", "lava", "throw", "reset"), args.getOrNull(1), 1) { s ->
                        if (s.lowercase() == "reset") mutableListOf()
                        else options(listOf("min", "max"), args.getOrNull(2), 2) {
                            mutableListOf()
                        }
                    }
                }

                "properties" -> {
                    options(listOf("set", "reset"), args.getOrNull(1), 1) {
                        options(FishingQol.config.properties.namesList(), args.getOrNull(2), 2) {
                            mutableListOf()
                        }
                    }
                }

                else -> throw Exception("WTF")
            }
        }
    }


    override fun canCommandSenderUseCommand(sender: ICommandSender?): Boolean = true

    override fun isUsernameIndex(args: Array<out String>?, index: Int): Boolean = false


    companion object {

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

        fun table(names: Array<String>, values: Array<Array<String>>): String {
            val valuesRowed = values.rowed()

            val namesMax = names.maxOf { it.length + 1 } // +1 for a space
            val valuesMax = valuesRowed.map { it.maxOf { i -> i.length } }
            val valuesMin = valuesRowed.map { it.minOf { i -> i.length } }
            val max = namesMax + values.maxOf { it.sumOf { i -> i.length } }
            return buildString {
                operator fun String.unaryPlus() = appendLine("|$this${" ".repeat(max(1, max - length))}|")
                operator fun String.unaryMinus() = appendLine(this)
                -"-".repeat(max)
                var i = 0
                +names.joinToString("|", postfix = " ") { name ->
                    " $name${
                        " ".repeat(
                            max(
                                1, valuesMax[i++] - name.length
                            )
                        )
                    }"
                }
                -"| ${"=".repeat(max - 2)} |"
                for (column in values) {
                    -buildString {
                        append("|")
                        for ((index, value) in column.withIndex()) {
                            append(
                                " $value${if (index == 0 && valuesMin[index] == value.length) " " else ""}${
                                    " ".repeat(
                                        max(1, valuesMax[index] - value.length)
                                    )
                                }|"
                            )
                        }
                    }
                }
                -"-".repeat(max)
            }.replace("[-=|]".toRegex()) {
                "§a${it.value}§2"
            }
        }
    }


}