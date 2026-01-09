package de.steingaming.hqol.fabric.config.categories

import com.google.gson.annotations.Expose
import io.github.notenoughupdates.moulconfig.annotations.Accordion
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorSlider
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption

class Rift {
    @Expose
    @ConfigOption(name = "Twinclaw Auto-Ice", desc = "Auto-Ice when Twinclaws are detected")
    @Accordion
    var twinclawAutoIce = TwinclawAutoIce()


    class TwinclawAutoIce {
        @Expose
        @ConfigOption(name = "Enabled", desc = "Enable this feature")
        @ConfigEditorBoolean
        var enabled = false

        @Expose
        @ConfigOption(name = "When to trigger", desc = "At which point of the timer to activate (after the random delay, see below)")
        @ConfigEditorSlider(minValue = 0.1f, maxValue = 2.0f, minStep = 0.1f)
        var triggerPoint = 0.5f

        @Expose
        @ConfigOption(name = "Random delay", desc = "Max value of random delay in seconds after the activation point (to reduce detection probability)")
        @ConfigEditorSlider(minValue = 0F, maxValue = 2.0f, minStep = 0.05f)
        var randomDelay = 0.3f
    }

    @Expose
    @ConfigOption(name = "Auto melon", desc = "Auto-heal when health reaches specific level")
    @Accordion
    var autoMelon = AutoMelon()

    class AutoMelon {
        @Expose
        @ConfigOption(name = "Enabled", desc = "Enable this feature")
        @ConfigEditorBoolean
        var enabled = false

        @Expose
        @ConfigOption(name = "Health threshold", desc = "At what threshold to trigger (1 Health = 1 Half heart)")
        @ConfigEditorSlider(minValue = 0.5f, maxValue = 28.0f, minStep = 1f)
        var healthThreshold = 8.0f
    }
}