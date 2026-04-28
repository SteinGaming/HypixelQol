package de.steingaming.hqol.fabric

import com.mojang.brigadier.Command
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import de.steingaming.hqol.fabric.config.Config
import de.steingaming.hqol.fabric.config.ConfigManager
import de.steingaming.hqol.fabric.helper.ChatHelper
import de.steingaming.hqol.fabric.features.Fastleap
import de.steingaming.hqol.fabric.features.Fishing
import de.steingaming.hqol.fabric.features.Rift
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.client.Minecraft
import org.apache.logging.log4j.LogManager
import kotlin.random.Random
import kotlin.reflect.KProperty

class HypixelQolFabric: ModInitializer {

    companion object {
        const val MOD_ID = "hypixelqol"
        val LOGGER = LogManager.getLogger(HypixelQolFabric::class.java)!!

        lateinit var INSTANCE: HypixelQolFabric

        operator fun getValue(thisRef: Any?, property: KProperty<*>): Config {
            return INSTANCE.configManager.config
        }
        val RANDOM = Random(System.currentTimeMillis())

        val FEATURE_LIST = listOf(Fastleap, Fishing, Rift)
    }

    lateinit var configManager: ConfigManager

    @Suppress("UnusedExpression")
    override fun onInitialize() {
        INSTANCE = this
        LOGGER.info("Initializing HypixelQolFabric...")

        // Initialize Helpers
        ChatHelper.init()

        val fabricLoader = FabricLoader.getInstance()
        val configPath = fabricLoader.configDir.resolve("hypixelqol.json")

        configManager = ConfigManager(configPath)

        ClientLifecycleEvents.CLIENT_STARTED.register { client: Minecraft ->
            for (feature in FEATURE_LIST) {
                feature.init(client)
            }
        }

        ClientCommandRegistrationCallback.EVENT.register(ClientCommandRegistrationCallback { source, registryAccess ->
            for (feature in FEATURE_LIST) {
                for (command in feature.registerCommands()) {
                    source.register(command)
                }
            }
            source.register(LiteralArgumentBuilder.literal<FabricClientCommandSource>("hqol").executes {
                it.source.client.apply {
                    setScreenAndShow(configManager.config.displayConfigUI(screen))
                }

                Command.SINGLE_SUCCESS
            })
        })
    }
}