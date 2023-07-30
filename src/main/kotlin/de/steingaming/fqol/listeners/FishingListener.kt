package de.steingaming.fqol.listeners

import de.steingaming.fqol.HypixelQol
import de.steingaming.fqol.HypixelQol.Companion.saveConfig
import de.steingaming.fqol.HypixelQol.Companion.scope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.minecraft.client.Minecraft
import net.minecraft.client.settings.KeyBinding
import net.minecraft.util.ChatComponentText
import net.minecraft.util.Vec3
import net.minecraftforge.client.event.sound.SoundEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent
import kotlin.random.Random

class FishingListener {
    private var ticks           = 0
    private var ticksNotFishing = 0
    private var ticksFishing    = 0

    private var lastMovement: Pair<Float, Float>? = null
    private var post = false



    @SubscribeEvent
    fun onSound(event: SoundEvent.SoundSourceEvent) {
        val config = HypixelQol.config.fishingConfig
        if (!config.enabled || (event.name != "random.splash" && event.name != "game.player.swim.splash") || Minecraft.getMinecraft()?.thePlayer?.fishEntity == null)
            return
        if (Minecraft.getMinecraft().thePlayer.fishEntity.positionVector.distanceTo(
                Vec3(event.sound.xPosF.toDouble(), event.sound.yPosF.toDouble(), event.sound.zPosF.toDouble())
            ) > config.properties.soundDistanceToRod
        ) return
        if (ticksFishing < config.properties.minWaitTimeTicks)
            return
        ticksFishing = 0
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
        if (event.phase != TickEvent.Phase.START || Minecraft.getMinecraft().thePlayer == null) return
        val config = HypixelQol.config.fishingConfig

        if (!config.enabled)
            return
        if (config.properties.inactivityDisableTicks != 0L)
            if (Minecraft.getMinecraft().thePlayer.fishEntity == null && ticksNotFishing++ > config.properties.inactivityDisableTicks) {
                ticksNotFishing = 0
                Minecraft.getMinecraft().thePlayer.addChatMessage(
                    ChatComponentText(
                        "§cFishingQol has been automatically disabled due to you not fishing for ${config.properties.inactivityDisableTicks / 20} seconds"
                    )
                )
                config.enabled = false
                saveConfig()
            } else if (Minecraft.getMinecraft().thePlayer.fishEntity != null && ticksNotFishing != 0)
                ticksNotFishing = 0

        if (Minecraft.getMinecraft().thePlayer.fishEntity != null)
            ticksFishing++

        if (post) {
            post = false
            KeyBinding.setKeyBindState(
                Minecraft.getMinecraft().gameSettings.keyBindJump.keyCode, false
            )
        }

        if (Minecraft.getMinecraft().thePlayer.fishEntity != null && config.properties.randomMovementTicks.min != 0L && ticks++ > config.properties.randomMovementTicks.let {
                Random.nextLong(
                    it.min,
                    it.max
                )
            }) {
            ticks = 0
            if (!Minecraft.getMinecraft().thePlayer.onGround) return
            KeyBinding.setKeyBindState(
                Minecraft.getMinecraft().gameSettings.keyBindJump.keyCode, true
            )
            post = true
            val (strafe, forward) = lastMovement?.let {
                lastMovement = null
                -it.first to -it.second
            } ?: (Random.nextDouble(-0.6, 0.6).toFloat() to
                    Random.nextDouble(-0.6, 0.6).toFloat()).also {
                lastMovement = it
            }
            Minecraft.getMinecraft().thePlayer.moveEntityWithHeading(
                strafe, forward
            )
        }
    }

}