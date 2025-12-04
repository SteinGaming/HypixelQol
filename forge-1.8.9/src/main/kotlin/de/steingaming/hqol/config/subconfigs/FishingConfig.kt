package de.steingaming.hqol.config.subconfigs

import com.google.gson.annotations.Expose
import de.steingaming.hqol.config.HypixelQolConfig
import io.github.notenoughupdates.moulconfig.annotations.Accordion
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorSlider
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption

class FishingConfig() {
    @Expose
    @Accordion
    @ConfigOption(name = "Timings", desc = "Fine-tune the reel and throw timings to your needs")
    val timings: FishingTimings = FishingTimings()

    @Expose
    @ConfigOption(name = "Enabled", desc = "Toggle the feature (keybind will be added later on)")
    @ConfigEditorBoolean
    var enabled: Boolean = false

    @Expose
    @ConfigOption(name = "Sound distance to rod", desc = "How far away the sounds can be for the rod to be reeled in\nFishing inside of other hooks will cause issues.")
    @ConfigEditorSlider(minValue = 0f, maxValue = 5f, minStep = 0.1f)
    var soundDistanceToRod: Float = 2.0f
    @Expose
    @ConfigOption(name = "Inactivity time to automatically disable", desc = "How many ticks should not be fished for this to be disabled\n20 ticks = 1 second; 0 to disable!")
    @ConfigEditorSlider(minValue = 0f, maxValue = 600f, minStep = 1f)
    var inactivityDisableTicks: Float = 600f
    @Expose
    @ConfigOption(name = "Minimum wait time", desc = "How long to wait from the last reel to reel in again\nThis is probably going to get removed in later releases.")
    @ConfigEditorSlider(minValue = 0f, maxValue = 200f, minStep = 1f)
    var minWaitTimeTicks: Float = 0f


    class FishingTimings {
        @Expose
        @ConfigOption(
            name = "Water pre catch delay minimum",
            desc = "How long to wait at a minimum to reel the rod back in when fishing in water\n§cDO NOT SET THIS TO NEAR 0, THIS WILL HEIGHTEN YOUR CHANCES OF BEING BANNED!"
        )
        @ConfigEditorSlider(minValue = 0.0f, maxValue = 1000.0f, minStep = 1.0f)
        var waterPreCatchDelayMin: Float = 200f
        @Expose
        @ConfigOption(
            name = "Water pre catch delay maximum",
            desc = "How long to wait at a maximum to reel the rod back in when fishing in water"
        )
        @ConfigEditorSlider(minValue = 0.0f, maxValue = 1000.0f, minStep = 1.0f)
        var waterPreCatchDelayMax: Float = 400f
        @Expose
        @ConfigOption(
            name = "Lava pre catch delay minimum",
            desc = "How long to wait at a minimum to reel the rod back in when fishing in water\n§cDO NOT SET THIS TO NEAR 0, THIS WILL HEIGHTEN YOUR CHANCES OF BEING BANNED!"
        )
        @ConfigEditorSlider(minValue = 0.0f, maxValue = 1000.0f, minStep = 1.0f)
        var lavaPreCatchDelayMin: Float = 150f
        @Expose
        @ConfigOption(
            name = "Lava pre catch delay maximum",
            desc = "How long to wait at a maximum to reel the rod back in when fishing in water"
        )
        @ConfigEditorSlider(minValue = 0.0f, maxValue = 1000.0f, minStep = 1.0f)
        var lavaPreCatchDelayMax: Float = 310f
        @Expose
        @ConfigOption(
            name = "Rod cast delay minimum",
            desc = "How long to wait at a minimum to reel the rod back in when fishing in water\n§cDO NOT SET THIS TO NEAR 0, THIS WILL HEIGHTEN YOUR CHANCES OF BEING BANNED!"
        )
        @ConfigEditorSlider(minValue = 0.0f, maxValue = 1000.0f, minStep = 1.0f)
        var castRodDelayMin: Float = 300f
        @Expose
        @ConfigOption(
            name = "Rod cast delay maximum",
            desc = "How long to wait at a maximum to reel the rod back in when fishing in water"
        )
        @ConfigEditorSlider(minValue = 0.0f, maxValue = 1000.0f, minStep = 1.0f)
        var castRodDelayMax: Float = 600f

        val waterPreCatchDelay: HypixelQolConfig.Range
            get() = HypixelQolConfig.Range(waterPreCatchDelayMin.toLong(), waterPreCatchDelayMax.toLong())

        val lavaPreCatchDelay: HypixelQolConfig.Range
            get() = HypixelQolConfig.Range(lavaPreCatchDelayMin.toLong(), lavaPreCatchDelayMax.toLong())

        val castRodDelay: HypixelQolConfig.Range
            get() = HypixelQolConfig.Range(castRodDelayMin.toLong(), castRodDelayMax.toLong())
    }
}