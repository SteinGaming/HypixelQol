package de.steingaming.hqol.fabric

import com.terraformersmc.modmenu.api.ConfigScreenFactory
import com.terraformersmc.modmenu.api.ModMenuApi
import net.minecraft.client.gui.screen.Screen

class HypixelQolModMenu: ModMenuApi {
    override fun getModConfigScreenFactory(): ConfigScreenFactory<Screen> {
        return ConfigScreenFactory { parentScreen: Screen ->
            HypixelQolFabric.INSTANCE.configManager.config.displayConfigUI(parentScreen)
        }
    }
}