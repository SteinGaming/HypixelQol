package de.steingaming.hqol.fabric.features

import de.steingaming.hqol.fabric.HypixelQolFabric
import de.steingaming.hqol.fabric.Utilities.cleanupColorCodes
import de.steingaming.hqol.fabric.config.categories.Rift
import de.steingaming.hqol.fabric.helper.InventoryHelper.inventoryInteractDelay
import de.steingaming.hqol.fabric.helper.InventoryHelper.changeSlot
import de.steingaming.hqol.fabric.helper.InventoryHelper.findItemType
import de.steingaming.hqol.fabric.model.Feature
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.minecraft.client.Minecraft
import net.minecraft.world.entity.EntityType
import net.minecraft.world.item.Items
import net.minecraft.world.phys.AABB

object Rift: Feature {
    const val COOLDOWN_MS = 700
    object TwinclawConstants {
        const val ENTITY_MAX_DISTANCE = 20.0
        val STATUS_BAR_MATCHER = Regex(".*TWINCLAWS (?<timer>[0-9]\\.[0-9]).*")
            .toPattern()
        val ITEM = Items.DIAMOND
    }
    object MelonConstants {
        val ITEM = Items.MELON_SLICE
    }
    val hotbarMutex = Mutex()
    val scope = CoroutineScope(Dispatchers.Default)

    var lastActionTime = 0L
    fun onTick(client: Minecraft) = runBlocking {
        if (client.screen != null || client.isPaused) return@runBlocking
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
        client: Minecraft,
        autoMelon: Rift.AutoMelon
    ) {
        val player = client.player ?: return
        val health = player.health
        if (health >= autoMelon.healthThreshold) return
        val slot = findItemType(client, MelonConstants.ITEM) ?: return
        clickSlotAndReturnCoroutine(client, slot)
    }

    private fun twinClawAutoIce(client: Minecraft, twinclawAutoIce: Rift.TwinclawAutoIce) {
        val playerPos = client.player?.position() ?: return
        val box = AABB(
            playerPos.x - TwinclawConstants.ENTITY_MAX_DISTANCE,
            playerPos.y - TwinclawConstants.ENTITY_MAX_DISTANCE,
            playerPos.z - TwinclawConstants.ENTITY_MAX_DISTANCE,
            playerPos.x + TwinclawConstants.ENTITY_MAX_DISTANCE,
            playerPos.y + TwinclawConstants.ENTITY_MAX_DISTANCE,
            playerPos.z + TwinclawConstants.ENTITY_MAX_DISTANCE,
        )

        val entities = client.level?.getEntities(
            EntityType.ARMOR_STAND, box
        ) { true } ?: return

        val playerName = client.player!!.gameProfile.name
        val spawnedByRegex = Regex("Spawned by: $playerName")
        val spawnedByArmorStand = entities.find {
            val name = it.displayName ?: it.customName
            val nameString = name?.tryCollapseToString() ?: name?.string ?: return@find false
            spawnedByRegex.matches(nameString.cleanupColorCodes())
        } ?: return

        val timer = entities.mapNotNull {
            val name = it.displayName ?: it.customName
            val nameString = name?.tryCollapseToString() ?: name?.string ?: return@mapNotNull null
            if (it.position().distanceTo(spawnedByArmorStand.position()) > 1.0) return@mapNotNull null
            val matcher = TwinclawConstants.STATUS_BAR_MATCHER.matcher(nameString.cleanupColorCodes())
            if (!matcher.find()) return@mapNotNull null
            matcher.group("timer").toFloatOrNull()
        }.singleOrNull() ?: return

        if (timer > twinclawAutoIce.triggerPoint || hotbarMutex.isLocked) return

        val slot = findItemType(client, TwinclawConstants.ITEM) ?: return
        clickSlotAndReturnCoroutine(client, slot)
    }

    // TODO use InventoryHelper function instead
    fun clickSlotAndReturnCoroutine(client: Minecraft, slot: Int) {
        if (!hotbarMutex.tryLock()) return
        val currentSlot = client.player!!.inventory.selectedSlot
        scope.launch {
            changeSlot(client, slot)
            inventoryInteractDelay()
            client.execute { client.startUseItem() }
            inventoryInteractDelay()
            changeSlot(client, currentSlot)
            lastActionTime = System.currentTimeMillis()
            hotbarMutex.unlock()
        }
    }

    override fun init(mc: Minecraft) {
        ClientTickEvents.END_CLIENT_TICK.register(ClientTickEvents.EndTick {
            onTick(it)
        })
    }
}