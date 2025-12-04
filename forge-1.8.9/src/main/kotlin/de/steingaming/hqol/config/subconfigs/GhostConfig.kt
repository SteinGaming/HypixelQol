package de.steingaming.hqol.config.subconfigs

import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorText
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption

class GhostConfig {
    @ConfigOption(
        name = "Block list to ignore",
        desc = "Put in the ID's of the blocks separated by commas to ignore (this setting will be improved later on)"
    )
    @ConfigEditorText
    var ignoreBlockList: String = "77, 143, 54, 69, 397, 144"
}