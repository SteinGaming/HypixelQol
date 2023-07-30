package de.steingaming.fqol.config.subconfigs

data class GhostConfig(
    val ignoreBlockList: MutableList<Int> = mutableListOf(77, 143, 54, 69, 397, 144)
)