package de.steingaming.hqol.config.subconfigs

import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorSlider
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption

class MiscellaneousConfig {
    @ConfigOption(name = "Terminator CPS", desc = "§cNO IDEA IF THIS STILL WORKS, PROCEED WITH CAUTION\nHolding a term should be enough")
    @ConfigEditorSlider(minValue = 0.0f, maxValue = 20.0f, minStep = 1f)
    var terminatorCPS: Float = 0f
}