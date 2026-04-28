package de.steingaming.hqol.fabric.features

import de.steingaming.hqol.fabric.HypixelQolFabric
import de.steingaming.hqol.fabric.config.Config.Range
import de.steingaming.hqol.fabric.helper.ChatHelper
import de.steingaming.hqol.fabric.model.Feature
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.minecraft.client.Minecraft
import net.minecraft.client.resources.sounds.SoundInstance
import net.minecraft.client.sounds.WeighedSoundEvents
import net.minecraft.world.entity.EntityType
import net.minecraft.world.level.material.Fluids
import net.minecraft.world.phys.Vec3
import kotlin.time.Duration.Companion.milliseconds

object Fishing: Feature {

    private val COROUTINE_SCOPE = CoroutineScope(Dispatchers.Default)

    var fishMutex: Mutex = Mutex()
    var fishJob: Job? = null

    @JvmStatic
    fun launchFishJob(range: Range): Job = GlobalScope.launch {
        val config by HypixelQolFabric
        val mc = Minecraft.getInstance()

        delay(range.getRandomValue().milliseconds)
        mc.execute {
            mc.startUseItem()
        }

        delay(config.fishing.timings.castRodDelay.getRandomValue().milliseconds)
        mc.execute {
            mc.startUseItem()
        }

        // Post delay
        delay(500.milliseconds)
    }

    fun onSound(instance: SoundInstance, soundSet: WeighedSoundEvents, range: Float) = runBlocking onSound@{
        val soundPath = instance.sound?.location?.path ?: return@onSound
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
            Minecraft.getInstance().player?.fishing?.position()?.distanceTo(Vec3(instance.x, instance.y, instance.z))
                ?: return@onSound
        if (distance > legacyOptions.maximumSoundDistance) return@onSound

        fishMutex.withLock {
            if (fishJob?.isActive == true) return@withLock
            HypixelQolFabric.LOGGER.debug("Starting job using sound method")
            fishJob = launchFishJob(range)
        }
    }

    fun onTick(minecraftClient: Minecraft) = runBlocking {
        val config by HypixelQolFabric
        if (!config.fishing.enabled || config.fishing.useLegacyDetection) return@runBlocking
        val fishHook = minecraftClient.player?.fishing ?: return@runBlocking

        val catching = fishHook.level().getEntities(
            EntityType.ARMOR_STAND, fishHook.boundingBox.inflate(.0, 0.5, .0)
        ) {
            it.displayName?.string == "!!!"
        }.isNotEmpty()

        if (!catching) return@runBlocking

        var range: Range? = null

        for (i in -1..4) {
            val block = minecraftClient.level!!.getBlockState(fishHook.blockPosition().below(i))
            if (block.fluidState.type == Fluids.WATER || block.fluidState.type == Fluids.FLOWING_WATER) {
                range = config.fishing.timings.waterPreCatchDelay
                break
            } else if (block.fluidState.type == Fluids.LAVA || block.fluidState.type == Fluids.FLOWING_LAVA) {
                range = config.fishing.timings.lavaPreCatchDelay
                break
            }
        }

        range ?: let {
            ChatHelper.sendToChat("Could not find a fluid block!")
            return@runBlocking
        }

        fishMutex.withLock {
            if (fishJob?.isActive == true) return@withLock
            HypixelQolFabric.LOGGER.debug("Starting job using armor stand method")
            fishJob = launchFishJob(range)
        }
    }

    override fun init(mc: Minecraft) {
        Minecraft.getInstance().soundManager.addListener { a, b, c ->
            this.onSound(a, b, c)
        }
        ClientTickEvents.END_CLIENT_TICK.register(ClientTickEvents.EndTick {
            onTick(it)
        })
    }
}