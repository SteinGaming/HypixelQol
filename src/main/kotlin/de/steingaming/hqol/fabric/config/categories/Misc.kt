package de.steingaming.hqol.fabric.config.categories

import com.google.gson.annotations.Expose
import io.github.notenoughupdates.moulconfig.ChromaColour
import io.github.notenoughupdates.moulconfig.annotations.Accordion
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorColour
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorInfoText
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorSlider
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption

class Misc {
    @Expose
    @ConfigOption(name = "Entity pointer", desc = "Points at entities... duh")
    @Accordion
    var ep = EntityPointer()

    class EntityPointer {
        @Expose
        @ConfigOption(name = "How to use!", desc = "Use the \"/entitypointer <add/remove>\" command followed by the mob type\nShorthand version \"/ep\" also exists")
        @ConfigEditorInfoText(infoTitle = "C")
        val text0 = ""

        @Expose
        @ConfigOption(name = "Use lines", desc = "Uses lines to point to the entities")
        @ConfigEditorBoolean
        var lines = true

        @Expose
        @ConfigOption(name = "Line color", desc = "Only applies when above option is active!")
        @ConfigEditorColour
        var linesColor = ChromaColour.fromStaticRGB(255, 10, 255, 255)

        @Expose
        @ConfigOption(name = "Line width", desc = "Sets the width of the line")
        @ConfigEditorSlider(minValue = 0.1f, maxValue = 10.0f, minStep = 0.5f)
        var lineWidth = 3.0f

        @Expose
        @ConfigOption(name = "Use glow", desc = "Uses a glowing effect to point to the entities")
        @ConfigEditorBoolean
        var glow = true

        @Expose
        @ConfigOption(name = "Glow color", desc = "Only applies when above option is active!")
        @ConfigEditorColour
        var glowColor = ChromaColour.fromStaticRGB(255, 10, 255, 255)

        @Expose
        @ConfigOption(name = "Nearest entity only", desc = "Only draw to the nearest entity instead of all")
        @ConfigEditorBoolean
        var nearestOnly = false
    }

    @Expose
    @ConfigOption(name = "Structure scanner", desc = "Find structures and add waypoints (currently Crystal Hollow only)")
    @Accordion
    var structureScanner = StructureScanner()

    class StructureScanner {
        @Expose
        @ConfigOption(name = "Enabled", desc = "Master toggle for this feature\nCurrently finds: Mines of Divan, Corleone")
        @ConfigEditorBoolean
        var enabled = false

        @Expose
        @ConfigOption(name = "Automatically add to Skyblocker", desc = "Instead of asking, automatically add the structure to Skyblocker when found")
        @ConfigEditorBoolean
        var automaticallyAdd = false
    }
    @Expose
    var firmamentWarningShown = false
}
