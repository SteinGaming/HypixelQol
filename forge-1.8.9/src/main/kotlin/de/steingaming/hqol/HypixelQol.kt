package de.steingaming.hqol

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import de.steingaming.hqol.commands.HypixelQolCommand
import de.steingaming.hqol.commands.RiftQolCommand
import de.steingaming.hqol.config.HypixelQolConfig
import de.steingaming.hqol.listeners.FishingListener
import de.steingaming.hqol.listeners.GhostListener
import de.steingaming.hqol.listeners.MiscellaneousListener
import de.steingaming.hqol.listeners.RiftListener
import kotlinx.coroutines.*
import net.hypixel.modapi.handler.ClientboundPacketHandler
import net.hypixel.modapi.packet.impl.clientbound.event.ClientboundLocationPacket
import net.minecraft.client.Minecraft
import net.minecraft.client.settings.KeyBinding
import net.minecraft.util.ChatComponentText
import net.minecraftforge.client.ClientCommandHandler
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.fml.client.registry.ClientRegistry
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.common.Mod.EventHandler
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent
import org.lwjgl.input.Keyboard
import java.io.File
import java.lang.reflect.Modifier
import kotlin.jvm.optionals.getOrNull


@Mod(modid = HypixelQol.MODID, version = HypixelQol.MODVERSION, clientSideOnly = true)
class HypixelQol {
    companion object {
        const val MODID = "hypixelqol"
        const val MODVERSION = "2.1.2"
        val gson: Gson = GsonBuilder().setPrettyPrinting().disableHtmlEscaping()
            .excludeFieldsWithModifiers(Modifier.TRANSIENT).create()
        lateinit var instance: HypixelQol
            private set

        fun saveConfig() = instance.saveConfig()

        val config: HypixelQolConfig
            get() = instance.config

        val scope = CoroutineScope(Dispatchers.Default)
    }

    var ghostEnabled = false

    init {
        instance = this
    }


    val config: HypixelQolConfig
        get() {
            return actualConfig ?:
                runBlocking {
                    configJob?.join()
                    actualConfig!!
                }
        }

    private var actualConfig: HypixelQolConfig? = null


    private val fishing = KeyBinding("Toggle Fishing QoL", Keyboard.KEY_I, "HypixelQol")
    private val ghost = KeyBinding("Toggle Ghost Blocking", Keyboard.KEY_I, "HypixelQol")

    private var configJob: Job? = null
    @EventHandler
    fun preinit(event: FMLPreInitializationEvent) {
        // since having this mod, others config had been reset
        // this should ensure that it doesn't conflict with any other mod anymore
        configJob = scope.launch {
            withContext(Dispatchers.IO) {
                val file = File("config/hypixel_qol.json")
                actualConfig = (try {
                    gson.fromJson(file.readText(), HypixelQolConfig::class.java)
                } catch (e: Exception) {
                    null
                } ?: HypixelQolConfig())
                launch {
                    withContext(Dispatchers.IO) {
                        delay(3000)
                        saveConfig()
                    }
                }
            }
        }

        MinecraftForge.EVENT_BUS.register(this)
        MinecraftForge.EVENT_BUS.register(FishingListener())
        MinecraftForge.EVENT_BUS.register(GhostListener())
        MinecraftForge.EVENT_BUS.register(MiscellaneousListener())

        net.hypixel.modapi.HypixelModAPI.getInstance().createHandler<ClientboundLocationPacket>(
            ClientboundLocationPacket::class.java, ClientboundPacketHandler { packet ->
                if (packet.mode.getOrNull() == "rift") {
                    MinecraftForge.EVENT_BUS.register(RiftListener)
                } else
                    MinecraftForge.EVENT_BUS.unregister(RiftListener)
            })

        ClientRegistry.registerKeyBinding(fishing)
        ClientRegistry.registerKeyBinding(ghost)

        ClientCommandHandler.instance.registerCommand(
            HypixelQolCommand()
        )
        ClientCommandHandler.instance.registerCommand(
            SpassHaben()
        )
        ClientCommandHandler.instance.registerCommand(
            RiftQolCommand()
        )
    }



    @SubscribeEvent
    fun onTick(event: TickEvent.ClientTickEvent) {
        if (event.phase != TickEvent.Phase.START || Minecraft.getMinecraft().thePlayer == null) return
        val config = this.config.fishingConfig
        if (fishing.isPressed) {
            Minecraft.getMinecraft().thePlayer.addChatMessage(ChatComponentText(if (config.enabled) "§cFishingQol has been disabled!" else "§aFishingQol has been enabled!"))
            config.enabled = !config.enabled
            saveConfig()
        }
        if (ghost.isPressed) {
            Minecraft.getMinecraft().thePlayer.addChatMessage(ChatComponentText(if (ghostEnabled) "§8Ghost-Blocks §chave been disabled!" else "§8Ghost-Blocks §ahave been enabled!"))
            ghostEnabled = !ghostEnabled
        }
    }

    fun saveConfig() {
        val file = File("config/hypixel_qol.json")
        file.writeText(gson.toJson(config, object: TypeToken<HypixelQolConfig>() {}.type))
    }
}