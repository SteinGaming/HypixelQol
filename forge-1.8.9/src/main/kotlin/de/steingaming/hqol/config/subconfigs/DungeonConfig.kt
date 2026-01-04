package de.steingaming.hqol.config.subconfigs

import com.google.gson.annotations.Expose
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption

class DungeonConfig {
    @Expose
    @ConfigOption(name = "Relic Triggerbot", desc = "Automatically right click when a relic spawns")
    @ConfigEditorBoolean
    var relicTriggerBot: Boolean = false
}