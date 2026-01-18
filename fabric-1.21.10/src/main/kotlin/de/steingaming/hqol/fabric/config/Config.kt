package de.steingaming.hqol.fabric.config

import com.google.gson.annotations.Expose
import de.steingaming.hqol.fabric.HypixelQolFabric
import de.steingaming.hqol.fabric.config.categories.Fishing
import de.steingaming.hqol.fabric.config.categories.Rift
import io.github.notenoughupdates.moulconfig.annotations.Category
import io.github.notenoughupdates.moulconfig.gui.GuiContext
import io.github.notenoughupdates.moulconfig.gui.GuiElementComponent
import io.github.notenoughupdates.moulconfig.gui.MoulConfigEditor
import io.github.notenoughupdates.moulconfig.platform.MoulConfigScreenComponent
import io.github.notenoughupdates.moulconfig.processor.ConfigProcessorDriver
import io.github.notenoughupdates.moulconfig.processor.MoulConfigProcessor
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.util.InputUtil
import net.minecraft.text.Text
import io.github.notenoughupdates.moulconfig.Config as MoulConfig


class Config: MoulConfig() {
    @Transient
    var prevTickScreen: Screen? = null
    init {
        saveRunnables.add {
            HypixelQolFabric.INSTANCE.configManager.save()
        }
        ClientTickEvents.START_CLIENT_TICK.register {
            val isPressed = InputUtil.isKeyPressed(MinecraftClient.getInstance().window, 256)
            if (isPressed && prevTickScreen is MoulConfigScreenComponent) {
                val moulScreen = prevTickScreen as MoulConfigScreenComponent
                val editor = (moulScreen.guiContext.root as GuiElementComponent).element as MoulConfigEditor<*>
                if (editor.configObject is Config) {
                    editor.configObject.saveNow()
                }
            }
            prevTickScreen = MinecraftClient.getInstance().currentScreen
        }
    }

    @Expose
    @Category(name = "Fishing", desc = "Fishing configuration")
    val fishing: Fishing = Fishing()

    @Expose
    @Category(name = "Rift", desc = "Various features concerning the Rift")
    val rift: Rift = Rift()

    fun displayConfigUI(previousScreen: Screen?): Screen {
        val processor = MoulConfigProcessor.withDefaults<Config>(this@Config)
        val driver = ConfigProcessorDriver(processor)
        //driver.checkExpose = true
        driver.processConfig(this@Config)

        val editor = MoulConfigEditor(processor)
        editor.setWide(true)
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