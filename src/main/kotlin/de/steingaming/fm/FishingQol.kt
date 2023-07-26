package de.steingaming.fm

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.minecraft.client.Minecraft
import net.minecraft.util.Vec3
import net.minecraftforge.client.event.sound.SoundEvent
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.common.Mod.EventHandler
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import kotlin.random.Random


@Mod(modid = FishingQol.MODID, version = FishingQol.MODVERSION, clientSideOnly = true)
class FishingQol {
    companion object {
        const val MODID = "fishingqol"
        const val MODVERSION = "1.0.0"
    }

    val scope = CoroutineScope(Dispatchers.Default)

    @EventHandler
    fun preinit(event: FMLPreInitializationEvent) {
        MinecraftForge.EVENT_BUS.register(this)
    }
    @SubscribeEvent
    fun onSound(event: SoundEvent.SoundSourceEvent) {
        if ((event.name == "random.splash" || event.name == "game.player.swim.splash") && Minecraft.getMinecraft()?.thePlayer?.fishEntity != null) {
            if (Minecraft.getMinecraft().thePlayer.fishEntity.positionVector.distanceTo(
                    Vec3(event.sound.xPosF.toDouble(), event.sound.yPosF.toDouble(), event.sound.zPosF.toDouble())
                ) > 2.0) return
            scope.launch {
                delay(Random.nextLong(200, 400))
                Minecraft.getMinecraft().playerController.sendUseItem(Minecraft.getMinecraft().thePlayer, Minecraft.getMinecraft().theWorld, Minecraft.getMinecraft().thePlayer.heldItem)
                delay(Random.nextLong(300, 600))
                Minecraft.getMinecraft().playerController.sendUseItem(Minecraft.getMinecraft().thePlayer, Minecraft.getMinecraft().theWorld, Minecraft.getMinecraft().thePlayer.heldItem)
            }
        }
    }
}