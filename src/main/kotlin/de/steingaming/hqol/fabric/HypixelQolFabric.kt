package de.steingaming.hqol.fabric

import de.steingaming.hqol.fabric.features.Fastleap
import de.steingaming.hqol.fabric.features.Fishing
import de.steingaming.hqol.fabric.features.Rift
import com.mojang.brigadier.Command
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import de.steingaming.hqol.fabric.config.Config
import de.steingaming.hqol.fabric.config.ConfigManager
import de.steingaming.hqol.fabric.events.IslandChangedEvent
import de.steingaming.hqol.fabric.features.EntityPointer
import de.steingaming.hqol.fabric.features.StructureScanner
import de.steingaming.hqol.fabric.helper.ChatHelper
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents
import net.fabricmc.loader.api.FabricLoader
import net.hypixel.modapi.HypixelModAPI
import net.hypixel.modapi.packet.impl.clientbound.event.ClientboundLocationPacket
import net.minecraft.client.Minecraft
import org.apache.logging.log4j.LogManager
import kotlin.random.Random
import kotlin.reflect.KProperty

class HypixelQolFabric: ModInitializer {

    companion object {
        val LOGGER = LogManager.getLogger(HypixelQolFabric::class.java)!!

        lateinit var INSTANCE: HypixelQolFabric

        operator fun getValue(thisRef: Any?, property: KProperty<*>): Config {
            return INSTANCE.configManager.config
        }
        val RANDOM = Random(System.currentTimeMillis())

        val FEATURE_LIST = listOf(EntityPointer, Fastleap, Fishing, Rift, StructureScanner)
    }

    lateinit var configManager: ConfigManager

    override fun onInitialize() {
        INSTANCE = this
        LOGGER.info("Initializing HypixelQolFabric...")

        HypixelModAPI.getInstance().createHandler(ClientboundLocationPacket::class.java) {
            IslandChangedEvent.EVENT.invoker().onIslandChanged(IslandChangedEvent.Location(it))
        }

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

        ClientCommandRegistrationCallback.EVENT.register(ClientCommandRegistrationCallback { dispatcher, registryAccess ->
            for (feature in FEATURE_LIST) {
                feature.registerCommands(dispatcher, registryAccess)
            }
            dispatcher.register(LiteralArgumentBuilder.literal<FabricClientCommandSource>("hqol").executes {
                it.source.client.apply {
                    setScreenAndShow(configManager.config.displayConfigUI(screen))
                }

                Command.SINGLE_SUCCESS
            })
        })
    }
}