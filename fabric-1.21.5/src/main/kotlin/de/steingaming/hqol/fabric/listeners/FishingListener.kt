package de.steingaming.hqol.fabric.listeners

import de.steingaming.hqol.fabric.HypixelQolFabric
import de.steingaming.hqol.fabric.config.controller.range.RangeValue
import de.steingaming.hqol.fabric.mixins.FishingBobberEntityAccessor
import de.steingaming.hqol.fabric.mixins.MinecraftClientInvoker
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import net.minecraft.client.MinecraftClient
import net.minecraft.client.sound.SoundInstance
import net.minecraft.client.sound.WeightedSoundSet
import net.minecraft.fluid.Fluids
import net.minecraft.text.Text
import net.minecraft.util.math.Vec3d

class FishingListener {
    init {
        MinecraftClient.getInstance().soundManager.registerListener { a, b, c ->
            this.onSound(a, b, c)
        }
    }

    companion object {
        const val FISH_HOOKED_WATER = "random/splash"
        const val FISH_HOOKED_LAVA = "game/player/swim/splash"
        private val COROUTINE_SCOPE = CoroutineScope(Dispatchers.Default)

        var fishMutex: Mutex = Mutex()
        var fishJob: Job? = null

        @JvmStatic
        fun onDataValueFishCaught() = runBlocking {
            val config by HypixelQolFabric
            if (!config.fishing.enabled || config.fishing.useLegacyDetection) return@runBlocking

            val minecraftClient = MinecraftClient.getInstance()
            val fishHook = minecraftClient.player?.fishHook ?: return@runBlocking
            val fishHookAccessor = fishHook as FishingBobberEntityAccessor
            // Revalidate mixin's claim
            if (!fishHookAccessor.isFishingCaught_hqol) return@runBlocking

            var range: RangeValue? = null

            for (i in -1..4) {
                val block = minecraftClient.world!!.getBlockState(fishHook.blockPos.down(i))
                if (block.fluidState.fluid == Fluids.WATER || block.fluidState.fluid == Fluids.FLOWING_WATER) {
                    range = config.fishing.waterHookDelayRange
                    break
                } else if (block.fluidState.fluid == Fluids.LAVA || block.fluidState.fluid == Fluids.FLOWING_LAVA) {
                    range = config.fishing.lavaHookDelayRange
                    break
                }
            }
            range ?: let {
                minecraftClient.inGameHud.chatHud.addMessage(Text.of("Could not find a fluid block!"))
                return@runBlocking
            }

            fishMutex.withLock {
                if (fishJob?.isActive == true) return@withLock
                fishJob = launchFishJob(range)
            }

        }

        @JvmStatic
        fun launchFishJob(range: RangeValue): Job = COROUTINE_SCOPE.launch {
            val config by HypixelQolFabric
            val minecraftClientInvoker = MinecraftClient.getInstance() as MinecraftClientInvoker

            delay(range.getRandomValue().toLong())
            minecraftClientInvoker.doItemUse_hqol()

            delay(config.fishing.rethrowHookDelay.getRandomValue().toLong())
            minecraftClientInvoker.doItemUse_hqol()
        }
    }

    fun onSound(instance: SoundInstance, soundSet: WeightedSoundSet, range: Float) {
        val soundPath = instance.sound.identifier.path
        if (soundPath != FISH_HOOKED_WATER && soundPath != FISH_HOOKED_LAVA) return

        val config by HypixelQolFabric
        if (!config.fishing.enabled || !config.fishing.useLegacyDetection) return

        val distance =
            MinecraftClient.getInstance().player?.fishHook?.pos?.distanceTo(Vec3d(instance.x, instance.y, instance.z))
                ?: return
        if (distance > config.fishing.maximumSoundDistance) return

        val range =
            if (soundPath == FISH_HOOKED_WATER) config.fishing.waterHookDelayRange
            else config.fishing.lavaHookDelayRange
        launchFishJob(range)
    }


}