package de.steingaming.hqol.fabric.config

import de.steingaming.hqol.fabric.HypixelQolFabric
import de.steingaming.hqol.fabric.config.categories.Fishing
import dev.isxander.yacl3.api.YetAnotherConfigLib
import net.minecraft.client.gui.screen.Screen
import net.minecraft.text.Text

data class Config(
    val fishing: Fishing = Fishing()
) {
    fun displayConfigUI(parentScreen: Screen?): Screen {
        return YetAnotherConfigLib.createBuilder()
            .title(Text.literal("HypixelQol"))
            .category(fishing.generateSubcategoryUI())
            .save {
                val configManager = HypixelQolFabric.Companion.INSTANCE.configManager
                configManager.save()
                for (listener in configManager.updateListeners) {
                    try {
                        listener.invoke(this)
                    } catch (e: Exception) {
                        println("Error while running config update listener")
                        e.printStackTrace()
                    }
                }
            }
            .build().generateScreen(parentScreen)
    }
}