package de.steingaming.fqol

object Utilities {
    fun options(args: Array<out String>): (options: Collection<String>,
                                       input: String?,
                                       position: Int,
                                       runner: (String) -> MutableList<String>) -> MutableList<String> {
        return e@{ options, input, position, runner ->
            return@e when {
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
    }
}