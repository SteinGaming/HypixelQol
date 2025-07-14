package de.steingaming.hqol.config.subconfigs

import com.google.gson.annotations.Expose
import de.steingaming.hqol.annotations.Hidden
import de.steingaming.hqol.config.HypixelQolConfig
import de.steingaming.hqol.config.HypixelQolConfig.Range

data class FishingConfig(
    val waterPreCatchDelay: FishingDelays = FishingDelays(200, 400),
    val lavaPreCatchDelay: FishingDelays = FishingDelays(150, 310),
    val castRodDelay: FishingDelays = FishingDelays(300, 600),
    val properties: FishingProperties = FishingProperties(),
    var enabled: Boolean = false
) {
    data class FishingDelays(var min: Long, var max: Long)

    data class FishingProperties(
        var randomMovementTicks: Range = Range(0, 0),
        var soundDistanceToRod: Double = 2.0,
        var inactivityDisableTicks: Long = 600,
        var minWaitTimeTicks: Long = 0
    ) {
        @Hidden
        @Transient
        @Expose(serialize = false, deserialize = false)
        val properties = HypixelQolConfig.Properties({ this }, { FishingProperties() })
    }
}