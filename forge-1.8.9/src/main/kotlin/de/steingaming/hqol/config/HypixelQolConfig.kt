package de.steingaming.hqol.config

import com.google.gson.annotations.Expose
import de.steingaming.hqol.HypixelQol
import de.steingaming.hqol.config.subconfigs.FishingConfig
import de.steingaming.hqol.config.subconfigs.GhostConfig
import de.steingaming.hqol.config.subconfigs.MiscellaneousConfig
import io.github.notenoughupdates.moulconfig.Config
import io.github.notenoughupdates.moulconfig.annotations.Category
import io.github.notenoughupdates.moulconfig.common.IMinecraft
import io.github.notenoughupdates.moulconfig.gui.MoulConfigEditor
import io.github.notenoughupdates.moulconfig.processor.ConfigProcessorDriver
import io.github.notenoughupdates.moulconfig.processor.MoulConfigProcessor
import net.minecraft.client.Minecraft

class HypixelQolConfig : Config() {
    init {
        saveRunnables.add {
            HypixelQol.saveConfig()
        }
    }

    class Range(val min: Long, val max: Long) {
        override fun toString(): String = "$min..$max"
        operator fun component1(): Long = min
        operator fun component2(): Long = max
    }

    fun openConfigScreen() {
        val processor = MoulConfigProcessor.withDefaults<HypixelQolConfig>(this@HypixelQolConfig)
        val driver = ConfigProcessorDriver(processor)
        //driver.checkExpose = true
        driver.processConfig(this@HypixelQolConfig)

        val editor = MoulConfigEditor(processor)
        Minecraft.getMinecraft().displayGuiScreen(null)
        IMinecraft.INSTANCE.openWrappedScreen(editor)
    }


    @Expose
    @Category(name = "Fishing", desc = "Set settings for the fishing feature")
    var fishingConfig: FishingConfig = FishingConfig()

    @Expose
    @Category(name = "Ghost Blocks", desc = "Modify the ghost block feature")
    var ghostConfig: GhostConfig = GhostConfig()

    @Expose
    @Category(name = "Miscellaneous", desc = "Other properties (minor features most likely)")
    var miscConfig: MiscellaneousConfig = MiscellaneousConfig()

}