package de.steingaming.hqol.fabric.config.categories

import com.google.gson.annotations.Expose
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorKeybind
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption

class Controls {
    @Expose
    @ConfigOption(name = "Toggle auto sneak", desc = "Press once to sneak indefinitely, press again to stop")
    @ConfigEditorKeybind(defaultKey = 0)
    var autoSneakKey = 0
}
