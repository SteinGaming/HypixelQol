package de.steingaming.hqol.fabric.listeners

import de.steingaming.hqol.fabric.HypixelQolFabric
import de.steingaming.hqol.fabric.Utilities.cleanupColorCodes
import de.steingaming.hqol.fabric.config.categories.Rift
import de.steingaming.hqol.fabric.mixins.MinecraftClientInvoker
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.minecraft.client.MinecraftClient
import net.minecraft.entity.EntityType
import net.minecraft.item.Item
import net.minecraft.item.Items
import net.minecraft.util.math.Box

class RiftListener {
    companion object {
        const val COOLDOWN_MS = 700
    }
    object TwinclawConstants {
        const val ENTITY_MAX_DISTANCE = 20.0
        val STATUS_BAR_MATCHER = Regex(".*TWINCLAWS (?<timer>[0-9]\\.[0-9]).*")
            .toPattern()
        val ITEM = Items.DIAMOND
    }
    object MelonConstants {
        val ITEM = Items.MELON_SLICE
    }
    init {
        ClientTickEvents.END_CLIENT_TICK.register(ClientTickEvents.EndTick {
            onTick(it)
        })
    }
    val hotbarMutex = Mutex()
    val scope = CoroutineScope(Dispatchers.Default)

    var lastActionTime = 0L
    fun onTick(client: MinecraftClient) = runBlocking {
        if (client.currentScreen != null || client.isPaused) return@runBlocking
        if (hotbarMutex.isLocked) return@runBlocking
        if (System.currentTimeMillis() - lastActionTime - COOLDOWN_MS < 0) return@runBlocking
        val config by HypixelQolFabric
        val riftConfig = config.rift

        if (riftConfig.twinclawAutoIce.enabled) {
            twinClawAutoIce(client, riftConfig.twinclawAutoIce)
        }
        if (!hotbarMutex.isLocked && riftConfig.autoMelon.enabled) {
            autoMelon(client, riftConfig.autoMelon)
        }
    }

    fun autoMelon(
        client: MinecraftClient,
        autoMelon: Rift.AutoMelon
    ) {
        val player = client.player ?: return
        val health = player.health
        if (health >= autoMelon.healthThreshold) return
        val slot = findItemType(client, MelonConstants.ITEM) ?: return
        clickSlotAndReturnCoroutine(client, slot)
    }

    private fun twinClawAutoIce(client: MinecraftClient, twinclawAutoIce: Rift.TwinclawAutoIce) {
        val playerPos = client.player?.pos ?: return
        val box = Box(
            playerPos.x - TwinclawConstants.ENTITY_MAX_DISTANCE,
            playerPos.y - TwinclawConstants.ENTITY_MAX_DISTANCE,
            playerPos.z - TwinclawConstants.ENTITY_MAX_DISTANCE,
            playerPos.x + TwinclawConstants.ENTITY_MAX_DISTANCE,
            playerPos.y + TwinclawConstants.ENTITY_MAX_DISTANCE,
            playerPos.z + TwinclawConstants.ENTITY_MAX_DISTANCE,
        )

        val entities = client.world?.getEntitiesByType(
            EntityType.ARMOR_STAND, box
        ) { true } ?: return

        val playerName = client.player!!.gameProfile.name
        val spawnedByRegex = Regex("Spawned by: $playerName")
        val spawnedByArmorStand = entities.find {
            val name = it.displayName ?: it.customName
            val nameString = name?.literalString ?: name?.string ?: return@find false
            spawnedByRegex.matches(nameString.cleanupColorCodes())
        } ?: return

        val timer = entities.mapNotNull {
            val name = it.displayName ?: it.customName
            val nameString = name?.literalString ?: name?.string ?: return@mapNotNull null
            if (it.pos.distanceTo(spawnedByArmorStand.pos) > 1.0) return@mapNotNull null
            val matcher = TwinclawConstants.STATUS_BAR_MATCHER.matcher(nameString.cleanupColorCodes())
            if (!matcher.find()) return@mapNotNull null
            matcher.group("timer").toFloatOrNull()
        }.singleOrNull() ?: return

        if (timer > twinclawAutoIce.triggerPoint || hotbarMutex.isLocked) return

        val slot = findItemType(client, TwinclawConstants.ITEM) ?: return
        clickSlotAndReturnCoroutine(client, slot)
    }

    fun findItemType(client: MinecraftClient, item: Item): Int? {
        for (i in 0..<9) {
            if (client.player?.inventory?.getStack(i)?.item == item)
                return i
        }
        return null
    }

    fun clickSlotAndReturnCoroutine(client: MinecraftClient, slot: Int) {
        if (!hotbarMutex.tryLock()) return
        val currentSlot = client.player!!.inventory.selectedSlot
        scope.launch {
            changeSlot(client, slot)
            actionDelay()
            client.execute { (client as MinecraftClientInvoker).doItemUse_hqol() }
            actionDelay()
            changeSlot(client, currentSlot)
            lastActionTime = System.currentTimeMillis()
            hotbarMutex.unlock()
        }
    }

    private suspend fun actionDelay() {
        val random = HypixelQolFabric.RANDOM
        delay(random.nextLong(50, 100))
    }

    private fun changeSlot(client: MinecraftClient, slot: Int) {
        client.execute {
            if (client.currentScreen != null || client.isPaused) return@execute
            client.player!!.inventory.selectedSlot = slot
        }
    }
}