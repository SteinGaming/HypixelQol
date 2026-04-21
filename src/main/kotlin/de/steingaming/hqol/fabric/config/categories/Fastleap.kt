package de.steingaming.hqol.fabric.config.categories

import com.google.gson.annotations.Expose
import io.github.notenoughupdates.moulconfig.annotations.Accordion
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorDropdown
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorInfoText
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorSlider
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorText
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption

class Fastleap {
    @Expose
    @ConfigOption(name = "Enabled", desc = "Master switch for this feature")
    @ConfigEditorBoolean
    var enabled: Boolean = false

    @Expose
    @ConfigOption(name = "Timeout in seconds (optional)", desc = "Just in case some weird desync happens\nSet to 0 to disable")
    @ConfigEditorSlider(minValue = 0.0f, maxValue = 3.0f, minStep = 0.1f)
    var timeout: Float = 0.0f

    @Expose
    @ConfigOption(name = "Section to classes", desc = "Specify when to leap to a specific class")
    @Accordion
    var classes: FastLeapClasses = FastLeapClasses()

    @Expose
    @ConfigOption(name = "Section to names", desc = "Specify when to leap to a specific username")
    @Accordion
    var names: FastLeapNames = FastLeapNames()


    @Expose
    @ConfigOption(name = "Timings", desc = "How long to wait (randomly) until actually leaping")
    @Accordion
    var timings: FastLeapTimings = FastLeapTimings()

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
        @ConfigOption(name = "S1", desc = "Which class to leap to when in S1")
        @ConfigEditorDropdown(values = ["archer", "healer", "berserk", "mage", "tank", "none"])
        var S1: String = ""

        @Expose
        @ConfigOption(name = "S2", desc = "Which class to leap to when in S2")
        @ConfigEditorDropdown(values = ["archer", "healer", "berserk", "mage", "tank", "none"])
        var S2: String = ""
        @Expose
        @ConfigOption(name = "S3", desc = "Which class to leap to when in S3")
        @ConfigEditorDropdown(values = ["archer", "healer", "berserk", "mage", "tank", "none"])
        var S3: String = ""
        @Expose
        @ConfigOption(name = "S4", desc = "Which class to leap to when in S4")
        @ConfigEditorDropdown(values = ["archer", "healer", "berserk", "mage", "tank", "none"])
        var S4: String = ""
        @Expose
        @ConfigOption(name = "Default", desc = "Which class to leap to when in no section")
        @ConfigEditorDropdown(values = ["archer", "healer", "berserk", "mage", "tank", "none"])
        var default: String = ""
    }


    class FastLeapNames {
        @Expose
        @ConfigOption(name = "", desc = "Leave empty to fallback to classes")
        @ConfigEditorInfoText(infoTitle = "")
        val _text = ""

        @Expose
        @ConfigOption(name = "S1", desc = "Which username to leap to when in S1")
        @ConfigEditorText
        var S1: String = ""
        @Expose
        @ConfigOption(name = "S2", desc = "Which username to leap to when in S2")
        @ConfigEditorText
        var S2: String = ""
        @Expose
        @ConfigOption(name = "S3", desc = "Which username to leap to when in S3")
        @ConfigEditorText
        var S3: String = ""
        @Expose
        @ConfigOption(name = "S4", desc = "Which username to leap to when in S4")
        @ConfigEditorText
        var S4: String = ""
        @Expose
        @ConfigOption(name = "Default", desc = "Which username to leap to when in no section")
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

    @Expose
    @ConfigOption(name = "Debug", desc = "Easily find out issues (probably) (fuck you sof)")
    @ConfigEditorBoolean
    var debug: Boolean = false
}
