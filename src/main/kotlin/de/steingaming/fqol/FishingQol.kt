package de.steingaming.fqol

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.minecraft.client.Minecraft
import net.minecraft.client.settings.KeyBinding
import net.minecraft.util.ChatComponentText
import net.minecraft.util.Vec3
import net.minecraftforge.client.ClientCommandHandler
import net.minecraftforge.client.event.sound.SoundEvent
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.fml.client.registry.ClientRegistry
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.common.Mod.EventHandler
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent
import org.lwjgl.input.Keyboard
import java.io.File
import kotlin.random.Random


@Mod(modid = FishingQol.MODID, version = FishingQol.MODVERSION, clientSideOnly = true)
class FishingQol {
    companion object {
        const val MODID = "fishingqol"
        const val MODVERSION = "1.0.0"
        val gson: Gson = GsonBuilder().setPrettyPrinting().disableHtmlEscaping()
            .create()
        lateinit var instance: FishingQol
            private set

        fun saveConfig() = instance.saveConfig()

        val config: FishingQolConfig
            get() = instance.config

        fun resetConfig() {
            instance.config = FishingQolConfig()
        }
    }

    init {
        instance = this
    }

    lateinit var config: FishingQolConfig
        private set

    val scope = CoroutineScope(Dispatchers.Default)

    val keybind = KeyBinding("Toggle FishingQOL", Keyboard.KEY_I, "FishingQol")

    @EventHandler
    fun preinit(event: FMLPreInitializationEvent) {
        val file = File("config/fishing_qol.json")
        config = (try {
            gson.fromJson(file.readText(), FishingQolConfig::class.java)
        } catch (e: Exception) {
            null
        } ?: FishingQolConfig())
        saveConfig()

        MinecraftForge.EVENT_BUS.register(this)
        ClientRegistry.registerKeyBinding(keybind)
        ClientCommandHandler.instance.registerCommand(
            FishingQolCommand()
        )
        ClientCommandHandler.instance.registerCommand(
            SpassHaben()
        )
    }

    @SubscribeEvent
    fun onSound(event: SoundEvent.SoundSourceEvent) {
        if (!config.enabled || (event.name != "random.splash" && event.name != "game.player.swim.splash") || Minecraft.getMinecraft()?.thePlayer?.fishEntity == null)
            return
        if (Minecraft.getMinecraft().thePlayer.fishEntity.positionVector.distanceTo(
                Vec3(event.sound.xPosF.toDouble(), event.sound.yPosF.toDouble(), event.sound.zPosF.toDouble())
            ) > 2.0
        ) return
        scope.launch {
            val (min, max) = if (event.name == "game.player.swim.splash") config.lavaPreCatchDelay else config.waterPreCatchDelay
            delay(Random.nextLong(min, max))
            Minecraft.getMinecraft().playerController.sendUseItem(
                Minecraft.getMinecraft().thePlayer,
                Minecraft.getMinecraft().theWorld,
                Minecraft.getMinecraft().thePlayer.heldItem
            )
            val (postMin, postMax) = config.castRodDelay
            delay(Random.nextLong(postMin, postMax))
            Minecraft.getMinecraft().playerController.sendUseItem(
                Minecraft.getMinecraft().thePlayer,
                Minecraft.getMinecraft().theWorld,
                Minecraft.getMinecraft().thePlayer.heldItem
            )
        }
    }

    @SubscribeEvent
    fun onTick(event: TickEvent.ClientTickEvent) {
        if (keybind.isPressed) {
            Minecraft.getMinecraft().thePlayer.addChatMessage(ChatComponentText(if (config.enabled) "§cFishingQol has been disabled!" else "§aFishingQol has been enabled!"))
            config.enabled = !config.enabled
            saveConfig()
        }
    }

    fun saveConfig() {
        val file = File("config/fishing_qol.json")
        file.writeText(gson.toJson(config))
    }
}