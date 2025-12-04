package de.steingaming.hqol.config.subconfigs

import com.google.gson.annotations.Expose
import io.github.notenoughupdates.moulconfig.annotations.Accordion
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorInfoText
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorSlider
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorText
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption

class FastLeapConfig {
    @Expose
    @ConfigOption(name = "Section to classes", desc = "Specify when to leap to a specific class")
    @Accordion
    var classes: FastLeapClasses = FastLeapClasses()

    @Expose
    @ConfigOption(name = "Timings", desc = "How long to wait (randomly) until actually leaping")
    @Accordion
    var timings: FastLeapTimings = FastLeapTimings()

    @Expose
    @ConfigOption(name = "Timeout for menu", desc = "How long to wait for the menu to appear\nPing dependent; Needed for cooldown occurrences")
    @ConfigEditorSlider(minValue = 0f, maxValue = 1000f, minStep = 1f)
    var timeout: Float = 300.0f

    @Expose
    @ConfigOption(name = "Hide UI", desc = "Hide the Spirit Leap menu from the user\n§cUSE AT OWN RISK")
    @ConfigEditorBoolean
    var hideUI: Boolean = false

    @Expose
    @ConfigOption(name = "Play pling when leaping", desc = "QoL feature")
    @ConfigEditorBoolean
    var playNoise: Boolean = true
    class FastLeapClasses {
        @Expose
        @ConfigEditorInfoText(infoTitle = "All class names in lowercase")
        @ConfigOption(name = "How to use", desc = "Leave empty to disable")
        val _text = null

        @Expose
        @ConfigOption(name = "S1", desc = "Which class to leap to when in S1")
        @ConfigEditorText
        var S1: String = ""
        @Expose
        @ConfigOption(name = "S2", desc = "Which class to leap to when in S2")
        @ConfigEditorText
        var S2: String = ""
        @Expose
        @ConfigOption(name = "S3", desc = "Which class to leap to when in S3")
        @ConfigEditorText
        var S3: String = ""
        @Expose
        @ConfigOption(name = "S4", desc = "Which class to leap to when in S4")
        @ConfigEditorText
        var S4: String = ""
        @Expose
        @ConfigOption(name = "Default", desc = "Which class to leap to when in no section")
        @ConfigEditorText
        var default: String = ""
    }

    class FastLeapTimings {
        @Expose
        @ConfigOption(name = "Lower limit (ms)", desc = "")
        @ConfigEditorSlider(minValue = 0f, maxValue = 1000f, minStep = 1f)
        var lower: Float = 50f
        @Expose
        @ConfigOption(name = "Upper limit (ms)", desc = "")
        @ConfigEditorSlider(minValue = 0f, maxValue = 1000f, minStep = 1f)
        var upper: Float = 100f
    }
}
