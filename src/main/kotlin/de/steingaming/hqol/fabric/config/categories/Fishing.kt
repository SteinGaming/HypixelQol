package de.steingaming.hqol.fabric.config.categories

import com.google.gson.annotations.Expose
import de.steingaming.hqol.fabric.config.Config
import io.github.notenoughupdates.moulconfig.annotations.Accordion
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorSlider
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorText
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption

class Fishing {
    @Expose
    @ConfigOption(name = "Enabled", desc = "Master switch to enable this feature")
    @ConfigEditorBoolean
    var enabled: Boolean = false

    @Expose
    @ConfigOption(name = "Timings", desc = "Changes the delays from catching and throwing")
    @Accordion
    var timings = FishingTimings()

    // TODO move to common module maybe?
    class FishingTimings {
        @Expose
        @ConfigOption(
            name = "Water pre catch delay minimum",
            desc = "How long to wait at a minimum to reel the rod back in when fishing in water\n§cDO NOT SET THIS TO NEAR 0, THIS WILL HEIGHTEN YOUR CHANCES OF BEING BANNED!"
        )
        @ConfigEditorSlider(minValue = 0.0f, maxValue = 1000.0f, minStep = 1.0f)
        var waterPreCatchDelayMin: Float = 150f
        @Expose
        @ConfigOption(
            name = "Water pre catch delay maximum",
            desc = "How long to wait at a maximum to reel the rod back in when fishing in water"
        )
        @ConfigEditorSlider(minValue = 0.0f, maxValue = 1000.0f, minStep = 1.0f)
        var waterPreCatchDelayMax: Float = 250f
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
        var lavaPreCatchDelayMax: Float = 250f
        @Expose
        @ConfigOption(
            name = "Rod cast delay minimum",
            desc = "How long to wait at a minimum to reel the rod back in when fishing in water\n§cDO NOT SET THIS TO NEAR 0, THIS WILL HEIGHTEN YOUR CHANCES OF BEING BANNED!"
        )
        @ConfigEditorSlider(minValue = 0.0f, maxValue = 1000.0f, minStep = 1.0f)
        var castRodDelayMin: Float = 175f
        @Expose
        @ConfigOption(
            name = "Rod cast delay maximum",
            desc = "How long to wait at a maximum to reel the rod back in when fishing in water"
        )
        @ConfigEditorSlider(minValue = 0.0f, maxValue = 1000.0f, minStep = 1.0f)
        var castRodDelayMax: Float = 225f

        val waterPreCatchDelay: Config.Range
            get() = Config.Range(waterPreCatchDelayMin.toLong(), waterPreCatchDelayMax.toLong())

        val lavaPreCatchDelay: Config.Range
            get() = Config.Range(lavaPreCatchDelayMin.toLong(), lavaPreCatchDelayMax.toLong())

        val castRodDelay: Config.Range
            get() = Config.Range(castRodDelayMin.toLong(), castRodDelayMax.toLong())
    }

    @Expose
    @ConfigOption(name = "Recovery", desc = "Recast when something bad happens")
    @Accordion
    var recovery: FishingRecovery = FishingRecovery()

    class FishingRecovery {
        @Expose
        @ConfigOption(name = "Repair missing bobber", desc = "Automatically recast rod when bobber is not found 1s after casting (not recommended)")
        @ConfigEditorBoolean
        var repairMissingBobber: Boolean = false

        @Expose
        @ConfigOption(name = "Repair mob hooked", desc = "Automatically recast rod when bobber is stuck inside of an entity")
        @ConfigEditorBoolean
        var repairMobHooked: Boolean = false
    }

    @Expose
    @ConfigOption(name = "Legacy mode - §cEXPERTS ONLY, NO SUPPORT CURRENTLY", desc = "Sound method previously availible on 1.8.9")
    @Accordion
    var legacy: FishingLegacy = FishingLegacy()

    class FishingLegacy {
        @Expose
        @ConfigOption(name = "Use Legacy Detection", desc = "Whether to use the legacy sound method.\n§cCURRENTLY ONLY WORKS OUTSIDE OF HYPIXEL, DO NOT ENABLE UNLESS YOU KNOW WHAT YOU ARE DOING!")
        @ConfigEditorBoolean
        var legacyEnabled: Boolean = false

        @Expose
        @ConfigOption(name = "Legacy options", desc = "Only useful when enabling the former toggle")
        @Accordion
        var legacyOptions = LegacyOptions()


        class LegacyOptions {
            @Expose
            @ConfigOption(name = "Maximum sound distance to rod", desc = "Which radius should sounds be acknowledged, relative to the bobber?")
            @ConfigEditorSlider(minValue = .1f, maxValue = 10f, minStep = .1f)
            var maximumSoundDistance: Double = 0.1
            @Expose
            @ConfigOption(name = "Water sound path", desc = "What sound should be used for water fishing?")
            @ConfigEditorText
            var waterSoundPath: String = "random/splash"
            @Expose
            @ConfigOption(name = "Lava sound path", desc = "What sound should be used for lava fishing?")
            @ConfigEditorText
            var lavaSoundPath: String = "game/player/swim/splash"
        }
    }

}