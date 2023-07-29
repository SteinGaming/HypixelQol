package de.steingaming.fqol

data class FishingQolConfig(
    val waterPreCatchDelay: FishingDelays = FishingDelays(200, 400),
    val lavaPreCatchDelay: FishingDelays = FishingDelays(150, 310),
    val castRodDelay: FishingDelays = FishingDelays(300, 600),
    val ignoreBlockList: MutableList<Int> = mutableListOf(77, 143, 54, 69, 397, 144),
    var enabled: Boolean = false
) {
    data class FishingDelays(var min: Long, var max: Long)
}
