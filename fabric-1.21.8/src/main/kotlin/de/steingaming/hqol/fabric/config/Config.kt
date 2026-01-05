package de.steingaming.hqol.fabric.config

import com.google.gson.annotations.Expose
import de.steingaming.hqol.fabric.HypixelQolFabric
import de.steingaming.hqol.fabric.config.categories.Fishing
import io.github.notenoughupdates.moulconfig.annotations.Category
import io.github.notenoughupdates.moulconfig.common.IMinecraft
import io.github.notenoughupdates.moulconfig.gui.GuiContext
import io.github.notenoughupdates.moulconfig.gui.GuiElementComponent
import io.github.notenoughupdates.moulconfig.gui.MoulConfigEditor
import io.github.notenoughupdates.moulconfig.platform.MoulConfigScreenComponent
import io.github.notenoughupdates.moulconfig.processor.ConfigProcessorDriver
import io.github.notenoughupdates.moulconfig.processor.MoulConfigProcessor
import net.minecraft.client.gui.screen.Screen
import net.minecraft.text.Text
import io.github.notenoughupdates.moulconfig.Config as MoulConfig


class Config: MoulConfig() {
    init {
        saveRunnables.add {
            HypixelQolFabric.INSTANCE.configManager.save()
        }
    }

    @Expose
    @Category(name = "Fishing", desc = "Fishing configuration")
    val fishing: Fishing = Fishing()

    fun displayConfigUI(previousScreen: Screen?): Screen {
        val processor = MoulConfigProcessor.withDefaults<Config>(this@Config)
        val driver = ConfigProcessorDriver(processor)
        //driver.checkExpose = true
        driver.processConfig(this@Config)

        val editor = MoulConfigEditor(processor)

        return MoulConfigScreenComponent(
            Text.empty(), GuiContext(GuiElementComponent(editor)), previousScreen
        )
    }

    class Range(val min: Long, val max: Long) {
        override fun toString(): String = "$min..$max"
        operator fun component1(): Long = min
        operator fun component2(): Long = max

        fun getRandomValue(): Long =
            HypixelQolFabric.RANDOM.nextLong(min, max)
    }
}