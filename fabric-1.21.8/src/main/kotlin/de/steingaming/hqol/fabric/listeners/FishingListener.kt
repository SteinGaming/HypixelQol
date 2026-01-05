package de.steingaming.hqol.fabric.listeners

import de.steingaming.hqol.fabric.HypixelQolFabric
import de.steingaming.hqol.fabric.config.Config.Range
import de.steingaming.hqol.fabric.mixins.MinecraftClientInvoker
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.minecraft.client.MinecraftClient
import net.minecraft.client.sound.SoundInstance
import net.minecraft.client.sound.WeightedSoundSet
import net.minecraft.entity.EntityType
import net.minecraft.fluid.Fluids
import net.minecraft.text.Text
import net.minecraft.util.math.Vec3d

class FishingListener {
    init {
        MinecraftClient.getInstance().soundManager.registerListener { a, b, c ->
            this.onSound(a, b, c)
        }
        ClientTickEvents.END_CLIENT_TICK.register(ClientTickEvents.EndTick {
            onTick(it)
        })
    }

    companion object {
        private val COROUTINE_SCOPE = CoroutineScope(Dispatchers.Default)

        var fishMutex: Mutex = Mutex()
        var fishJob: Job? = null

        @JvmStatic
        fun launchFishJob(range: Range): Job = GlobalScope.launch {
            val config by HypixelQolFabric
            val minecraftClientInvoker = MinecraftClient.getInstance() as MinecraftClientInvoker

            delay(range.getRandomValue())
            MinecraftClient.getInstance().execute { minecraftClientInvoker.doItemUse_hqol() }

            delay(config.fishing.timings.castRodDelay.getRandomValue())
            MinecraftClient.getInstance().execute { minecraftClientInvoker.doItemUse_hqol() }

            // Post delay
            delay(500)
        }
    }

    fun onSound(instance: SoundInstance, soundSet: WeightedSoundSet, range: Float) = runBlocking onSound@{
        val soundPath = instance.sound?.identifier?.path ?: return@onSound
        val config by HypixelQolFabric

        if (!config.fishing.enabled || !config.fishing.useLegacyDetection) return@onSound

        val legacyOptions = config.fishing.legacyOptions

        val range =
            when (soundPath) {
                legacyOptions.waterSoundPath -> config.fishing.timings.waterPreCatchDelay
                legacyOptions.lavaSoundPath -> config.fishing.timings.lavaPreCatchDelay
                else -> return@onSound
            }

        val distance =
            MinecraftClient.getInstance().player?.fishHook?.pos?.distanceTo(Vec3d(instance.x, instance.y, instance.z))
                ?: return@onSound
        if (distance > legacyOptions.maximumSoundDistance) return@onSound

        fishMutex.withLock {
            if (fishJob?.isActive == true) return@withLock
            HypixelQolFabric.LOGGER.debug("Starting job using sound method")
            fishJob = launchFishJob(range)
        }
    }

    fun onTick(minecraftClient: MinecraftClient) = runBlocking {
        val config by HypixelQolFabric
        if (!config.fishing.enabled || config.fishing.useLegacyDetection) return@runBlocking
        val fishHook = minecraftClient.player?.fishHook ?: return@runBlocking

        val catching = fishHook.world.getEntitiesByType(
            EntityType.ARMOR_STAND, fishHook.boundingBox.expand(.0, 0.5, .0)
        ) {
            it.displayName?.string == "!!!"
        }.isNotEmpty()

        if (!catching) return@runBlocking

        var range: Range? = null

        for (i in -1..4) {
            val block = minecraftClient.world!!.getBlockState(fishHook.blockPos.down(i))
            if (block.fluidState.fluid == Fluids.WATER || block.fluidState.fluid == Fluids.FLOWING_WATER) {
                range = config.fishing.timings.waterPreCatchDelay
                break
            } else if (block.fluidState.fluid == Fluids.LAVA || block.fluidState.fluid == Fluids.FLOWING_LAVA) {
                range = config.fishing.timings.lavaPreCatchDelay
                break
            }
        }

        range ?: let {
            minecraftClient.inGameHud.chatHud.addMessage(Text.of("Could not find a fluid block!"))
            return@runBlocking
        }

        fishMutex.withLock {
            if (fishJob?.isActive == true) return@withLock
            HypixelQolFabric.LOGGER.debug("Starting job using armor stand method")
            fishJob = launchFishJob(range)
        }
    }


}