package de.steingaming.hqol.listeners

import de.steingaming.hqol.HypixelQol
import de.steingaming.hqol.HypixelQol.Companion.saveConfig
import de.steingaming.hqol.HypixelQol.Companion.scope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.minecraft.client.Minecraft
import net.minecraft.util.ChatComponentText
import net.minecraft.util.Vec3
import net.minecraftforge.client.event.sound.SoundEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent
import kotlin.random.Random

class FishingListener {
    private var ticksNotFishing = 0
    private var ticksFishing    = 0

    @SubscribeEvent
    fun onSound(event: SoundEvent.SoundSourceEvent) {
        val config = HypixelQol.config.fishingConfig
        if (!config.enabled || (event.name != "random.splash" && event.name != "game.player.swim.splash") || Minecraft.getMinecraft()?.thePlayer?.fishEntity == null)
            return
        if (Minecraft.getMinecraft().thePlayer.fishEntity.positionVector.distanceTo(
                Vec3(event.sound.xPosF.toDouble(), event.sound.yPosF.toDouble(), event.sound.zPosF.toDouble())
            ) > config.soundDistanceToRod
        ) return
        if (ticksFishing < config.minWaitTimeTicks)
            return
        ticksFishing = 0
        scope.launch {
            val timings = config.timings
            val (min, max) = if (event.name == "game.player.swim.splash") timings.lavaPreCatchDelay else timings.waterPreCatchDelay
            delay(Random.nextLong(min, max))
            Minecraft.getMinecraft().playerController.sendUseItem(
                Minecraft.getMinecraft().thePlayer,
                Minecraft.getMinecraft().theWorld,
                Minecraft.getMinecraft().thePlayer.heldItem
            )
            val (postMin, postMax) = timings.castRodDelay
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
        if (event.phase != TickEvent.Phase.START || Minecraft.getMinecraft().thePlayer == null) return
        val config = HypixelQol.config.fishingConfig

        if (!config.enabled)
            return
        if (config.inactivityDisableTicks != 0.0f)
            if (Minecraft.getMinecraft().thePlayer.fishEntity == null && ticksNotFishing++ > config.inactivityDisableTicks) {
                ticksNotFishing = 0
                Minecraft.getMinecraft().thePlayer.addChatMessage(
                    ChatComponentText(
                        "§cFishingQol has been automatically disabled due to you not fishing for ${config.inactivityDisableTicks / 20} seconds"
                    )
                )
                config.enabled = false
                saveConfig()
            } else if (Minecraft.getMinecraft().thePlayer.fishEntity != null && ticksNotFishing != 0)
                ticksNotFishing = 0

        if (Minecraft.getMinecraft().thePlayer.fishEntity != null)
            ticksFishing++
    }

}