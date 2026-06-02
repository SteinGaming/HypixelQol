package de.steingaming.hqol.fabric.features

import de.steingaming.hqol.fabric.HypixelQolFabric
import de.steingaming.hqol.fabric.Utilities
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
import net.minecraft.world.entity.player.Player
import net.minecraft.world.entity.projectile.FishingHook
import net.minecraft.world.item.Items
import net.minecraft.world.level.material.Fluids
import net.minecraft.world.phys.Vec3
import kotlin.time.Duration.Companion.milliseconds

object Fishing: Feature {

    private val COROUTINE_SCOPE = CoroutineScope(Dispatchers.Default)

    val config
        get() = HypixelQolFabric.INSTANCE.configManager.config.fishing
    var fishMutex: Mutex = Mutex()
    var fishJob: Job? = null

    @JvmStatic
    fun launchFishJob(range: Range): Job = COROUTINE_SCOPE.launch {
        val mc = Minecraft.getInstance()

        delay(range.getRandomValue().milliseconds)
        mc.execute {
            if (mc.player?.mainHandItem?.item != Items.FISHING_ROD)
                return@execute
            mc.startUseItem()
        }

        delay(config.timings.castRodDelay.getRandomValue().milliseconds)
        mc.execute {
            if (mc.player?.mainHandItem?.item != Items.FISHING_ROD)
                return@execute
            mc.startUseItem()
        }

        // Post delay
        delay(500.milliseconds)

        checkRodStatusCorrect(mc, range)
    }

    private suspend fun checkRodStatusCorrect(mc: Minecraft, range: Range) {
        repeat(10) repeat@{
            val player = mc.player ?: return
            if (player.fishing != null &&
                player.fishing!!.currentState == FishingHook.FishHookState.HOOKED_IN_ENTITY
                && player.fishing!!.hookedIn?.type != EntityType.ARMOR_STAND
                && config.recovery.repairMobHooked) {
                ChatHelper.sendToChat("${player.fishing!!.hookedIn?.type?.toShortString()} Mob hooked! Recasting...")
                recoveryRecast(range)
                return
            }
            if (player.fishing != null || player.mainHandItem.item != Items.FISHING_ROD) return
            if (config.recovery.repairDisassociatedRod) {
                val ownedEntities = mc.level?.getEntities(
                    EntityType.FISHING_BOBBER, Utilities.getAABBEquidistant(
                        player.position(), 34.0
                    )
                ) { entity ->
                    entity.owner is Player && entity.owner?.uuid == player.uuid
                }
                if (!ownedEntities.isNullOrEmpty()) {
                    ChatHelper.sendToChat("Disassociated Bobber found - Rod bugged. Rethrowing again after random delay...")
                    recoveryRecast(range)
                    return
                }
            }
            delay(50.milliseconds)
        }
        if (config.recovery.repairMissingBobber) {
            ChatHelper.sendToChat("Rod may be bugged, couldn't find bobber after 1s. Rethrowing again after random delay...")
            recoveryRecast(range)
        }
    }

    private fun recoveryRecast(range: Range) {
        COROUTINE_SCOPE.launch {
            delay(500.milliseconds)
            fishMutex.withLock {
                if (fishJob?.isActive == false)
                    fishJob = launchFishJob(range)
            }
        }
    }


    fun onSound(instance: SoundInstance, soundSet: WeighedSoundEvents, range: Float) = runBlocking onSound@{
        val soundPath = instance.sound?.location?.path ?: return@onSound

        var legacy = config.legacy
        if (!config.enabled || !legacy.legacyEnabled) return@onSound

        val legacyOptions = config.legacy.legacyOptions

        val range =
            when (soundPath) {
                legacyOptions.waterSoundPath -> config.timings.waterPreCatchDelay
                legacyOptions.lavaSoundPath -> config.timings.lavaPreCatchDelay
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
        if (!config.enabled || config.legacy.legacyEnabled) return@runBlocking
        val fishHook = minecraftClient.player?.fishing ?: return@runBlocking

        val catching = fishHook.level().getEntities(
            EntityType.ARMOR_STAND, fishHook.boundingBox.inflate(.0, 0.5, .0)
        ) {
            it.displayName!!.string == "!!!"
        }.isNotEmpty()

        if (!catching) return@runBlocking

        var range: Range? = null

        for (i in -1..4) {
            val block = minecraftClient.level!!.getBlockState(fishHook.blockPosition().below(i))
            if (block.fluidState.type == Fluids.WATER || block.fluidState.type == Fluids.FLOWING_WATER) {
                range = config.timings.waterPreCatchDelay
                break
            } else if (block.fluidState.type == Fluids.LAVA || block.fluidState.type == Fluids.FLOWING_LAVA) {
                range = config.timings.lavaPreCatchDelay
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