package de.steingaming.hqol.fabric

import com.mojang.brigadier.builder.LiteralArgumentBuilder
import de.steingaming.hqol.fabric.config.Config
import de.steingaming.hqol.fabric.config.ConfigManager
import de.steingaming.hqol.fabric.listeners.FishingListener
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.client.MinecraftClient
import org.apache.logging.log4j.LogManager
import kotlin.reflect.KProperty

class HypixelQolFabric: ModInitializer {

    companion object {
        const val MOD_ID = "hypixelqol"
        val LOGGER = LogManager.getLogger(HypixelQolFabric::class.java)!!

        lateinit var INSTANCE: HypixelQolFabric

        operator fun getValue(thisRef: Any?, property: KProperty<*>): Config {
            return INSTANCE.configManager.config
        }
    }

    lateinit var configManager: ConfigManager

    override fun onInitialize() {
        INSTANCE = this
        LOGGER.info("Initializing HypixelQolFabric...")

        val fabricLoader = FabricLoader.getInstance()
        val configPath = fabricLoader.configDir.resolve("hypixelqol.json")

        configManager = ConfigManager(configPath)

        ClientLifecycleEvents.CLIENT_STARTED.register { client: MinecraftClient? ->
            FishingListener()
        }

        ClientCommandRegistrationCallback.EVENT.register(ClientCommandRegistrationCallback { source, registryAccess ->
            source.register(LiteralArgumentBuilder.literal<FabricClientCommandSource>("hqol").executes {
                MinecraftClient.getInstance().setScreenAndRender(configManager.config.displayConfigUI(null))
                0
            })
        })
    }
}