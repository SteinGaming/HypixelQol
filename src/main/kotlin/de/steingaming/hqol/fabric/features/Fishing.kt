@file:Suppress("UnusedImport")

package de.steingaming.hqol.fabric.features

import de.steingaming.hqol.fabric.HypixelQolFabric
import de.steingaming.hqol.fabric.Utilities
import de.steingaming.hqol.fabric.Utilities.cleanupColorCodes
import de.steingaming.hqol.fabric.config.Config.Range
import de.steingaming.hqol.fabric.helper.ChatHelper
import de.steingaming.hqol.fabric.helper.ChatHelper.launchWithSafeguard
import de.steingaming.hqol.fabric.helper.ChatHelper.plus
import de.steingaming.hqol.fabric.model.Feature
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.minecraft.client.Minecraft
import net.minecraft.client.resources.sounds.SoundInstance
import net.minecraft.client.sounds.WeighedSoundEvents
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.player.Player
import net.minecraft.world.entity.projectile.FishingHook
import net.minecraft.world.item.Items
import net.minecraft.world.level.material.Fluids
import net.minecraft.world.phys.Vec3
import kotlin.math.roundToInt
import kotlin.math.roundToLong
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

object Fishing : Feature {

    private val COROUTINE_SCOPE = CoroutineScope(Dispatchers.Default)

    val config
        get() = HypixelQolFabric.INSTANCE.configManager.config.fishing
    var fishMutex: Mutex = Mutex()
    var fishJob: Job? = null

    var lastFishing = Duration.ZERO

    @JvmStatic
    fun launchFishJob(range: Range?): Job = COROUTINE_SCOPE.launchWithSafeguard {
        val mc = Minecraft.getInstance()

        if (range != null) {
            delay(range.getRandomValue().milliseconds)
            if (mc.player?.mainHandItem?.item != Items.FISHING_ROD)
                return@launchWithSafeguard
            mc.execute {
                mc.startUseItem()
            }
        }

        delay(spread(config.timings.castAverage).getRandomValue().milliseconds)
        if (mc.player?.mainHandItem?.item != Items.FISHING_ROD)
            return@launchWithSafeguard
        mc.execute {
            mc.startUseItem()
        }
        lastFishing = System.currentTimeMillis().milliseconds
        // Post delay
        delay(200.milliseconds)

        checkRodStatusCorrect(mc, range)
    }

    private suspend fun checkRodStatusCorrect(mc: Minecraft, range: Range?) {
        val player = mc.player ?: return
        for (index in 0..<32) {
            if (player.fishing != null)
                return
            delay(50.milliseconds)
        }
        if (player.mainHandItem.item == Items.FISHING_ROD && config.recovery.repairMissingBobber) {
            ChatHelper.sendToChat("Rod may be bugged, couldn't find bobber after 1.8s. Throwing again after random delay...")
            recoveryRecast(null)
        }
    }

    private fun recoveryRecast(range: Range?) {
        COROUTINE_SCOPE.launchWithSafeguard {
            fishMutex.withLock {
                if (fishJob?.isActive == false)
                    fishJob = launchFishJob(range)
            }
        }
    }

    @JvmStatic
    fun onMobHooked(hook: FishingHook?, hookedIn: Entity?) = COROUTINE_SCOPE.launchWithSafeguard {
        hook ?: return@launchWithSafeguard
        hookedIn ?: return@launchWithSafeguard
        val player = Minecraft.getInstance().player ?: return@launchWithSafeguard
        if (!config.recovery.repairMobHooked || hook.playerOwner?.uuid != player.uuid || (System.currentTimeMillis().milliseconds - lastFishing).inWholeSeconds > 30)
            return@launchWithSafeguard
        if (hookedIn.type == //? if >= 26.2 {
            net.minecraft.world.entity.EntityTypes.ARMOR_STAND
        //? } else
        //EntityType.ARMOR_STAND
        ) return@launchWithSafeguard
        delay(Range(500, 700).getRandomValue().milliseconds)
        if (player.fishing?.hookedIn != hookedIn || !hookedIn.isAlive) return@launchWithSafeguard
        ChatHelper.sendToChat(hookedIn.type.description + " hooked! Recasting...")
        recoveryRecast(getFluidFromHook(hook)?.getRange() ?: Range(100, 300))
    }

    fun onSound(instance: SoundInstance, soundSet: WeighedSoundEvents, range: Float) = runBlocking onSound@{
        val soundPath = instance.sound?.location?.path ?: return@onSound

        val legacy = config.legacy
        if (!config.enabled || !legacy.legacyEnabled) return@onSound

        val legacyOptions = config.legacy.legacyOptions

        val range =
            when (soundPath) {
                legacyOptions.waterSoundPath -> spread(config.timings.waterAverage)
                legacyOptions.lavaSoundPath -> spread(config.timings.lavaAverage)
                else -> getFluidFromHook(Minecraft.getInstance().player?.fishing ?: return@onSound)?.getRange()
                    ?: return@onSound
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

    val PET_REGEX = Regex(" \\[Lvl (?<level>[0-9]{1,3})\\] (?<name>.+)")
    fun onTick(minecraftClient: Minecraft) = runBlocking {
        checkDisassociatedBobber(minecraftClient)
        if (!config.enabled || config.legacy.legacyEnabled) return@runBlocking
        val fishHook = minecraftClient.player?.fishing ?: return@runBlocking

        val catching = fishHook.level().getEntities(
            //? if >= 26.2 {
            net.minecraft.world.entity.EntityTypes.ARMOR_STAND,
            //? } else
            //EntityType.ARMOR_STAND,
            fishHook.boundingBox.inflate(.0, 0.5, .0)
        ) {
            it.displayName!!.string == "!!!"
        }.isNotEmpty()

        if (!catching) return@runBlocking

        val fluid = getFluidFromHook(fishHook) ?: let {
            ChatHelper.sendToChat("Could not find a fluid block!")
            return@runBlocking
        }

        if (fluid == FluidType.LAVA && config.slugfish) {
            var delay = 400
            val (pet, level) = minecraftClient.player?.connection?.listedOnlinePlayers?.firstOrNull {
                PET_REGEX.matches(it.tabListDisplayName?.string?.cleanupColorCodes() ?: return@firstOrNull false)
            }?.let {
                val groups = PET_REGEX.find(it.tabListDisplayName!!.string.cleanupColorCodes())!!.groups
                groups["name"]!!.value to groups["level"]!!.value.toInt()
            } ?: let {
                ChatHelper.sendToChat("[HypixelQol] §cCould not find pet through tab list! This is required for slug fishing!")
                (null to 0)
            }
            if (pet == "Slug") {
                delay -= (delay * (0.005 * level)).roundToInt()
            }
            if (fishHook.tickCount < delay)
                return@runBlocking
        }


        fishMutex.withLock {
            if (fishJob?.isActive == true) return@withLock
            HypixelQolFabric.LOGGER.debug("Starting job using armor stand method")
            fishJob = launchFishJob(fluid.getRange())
        }
    }

    fun checkDisassociatedBobber(minecraftClient: Minecraft) {
        val player = minecraftClient.player ?: return
        if (player.mainHandItem.item != Items.FISHING_ROD)
            return
        if (player.fishing?.isAlive == true || (System.currentTimeMillis().milliseconds - lastFishing).inWholeSeconds > 5)
            return
        val ownedEntity = minecraftClient.level?.getEntities(
            //? >= 26.2 {
            net.minecraft.world.entity.EntityTypes.FISHING_BOBBER,
            //?} else
            //EntityType.FISHING_BOBBER,
            Utilities.getAABBEquidistant(
                player.position(), 34.0
            )
        ) { entity ->
            entity.owner is Player && entity.owner?.uuid == player.uuid
        }?.firstOrNull()
        if (ownedEntity != null) {
            ChatHelper.sendToChat("Disassociated bobber found - Rod bugged. Fixing...")
            player.fishing = ownedEntity
        }
    }

    enum class FluidType(val getRange: () -> Range) {
        WATER({ spread(config.timings.waterAverage) }), LAVA({ spread(config.timings.lavaAverage) })
    }
    private fun getFluidFromHook(fishHook: FishingHook): FluidType? {
        val minecraftClient = Minecraft.getInstance()
        for (i in -1..4) {
            val block = minecraftClient.level!!.getBlockState(fishHook.blockPosition().below(i))
            if (block.fluidState.type == Fluids.WATER || block.fluidState.type == Fluids.FLOWING_WATER) {
                return FluidType.WATER
            } else if (block.fluidState.type == Fluids.LAVA || block.fluidState.type == Fluids.FLOWING_LAVA) {
                return FluidType.LAVA
            }
        }
        return null
    }

    private fun spread(value: Float): Range {
        val distribution = config.timings.distribution
        return Range(
            (value - value * distribution).roundToLong(),
            (value + value * distribution).roundToLong()
        )
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