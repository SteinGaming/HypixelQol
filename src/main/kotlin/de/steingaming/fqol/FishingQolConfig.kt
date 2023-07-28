package de.steingaming.fqol

data class FishingQolConfig(
    val waterPreCatchDelay: FishingDelays = FishingDelays(200, 400),
    val lavaPreCatchDelay: FishingDelays = FishingDelays(150, 310),
    val castRodDelay: FishingDelays = FishingDelays(300, 600),
    var enabled: Boolean = false
) {
    data class FishingDelays(var min: Long, var max: Long)
}
