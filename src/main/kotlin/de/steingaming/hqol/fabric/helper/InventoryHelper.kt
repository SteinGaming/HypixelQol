package de.steingaming.hqol.fabric.helper

import com.google.common.collect.Lists
import com.google.common.primitives.Shorts
import com.google.common.primitives.SignedBytes
import de.steingaming.hqol.fabric.HypixelQolFabric
import it.unimi.dsi.fastutil.ints.Int2ObjectMap
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.minecraft.client.Minecraft
import net.minecraft.core.NonNullList
import net.minecraft.network.HashedStack
import net.minecraft.network.protocol.game.ServerboundContainerClickPacket
import net.minecraft.world.entity.player.Player
import net.minecraft.world.inventory.ClickType
import net.minecraft.world.inventory.Slot
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import java.util.function.Predicate

object InventoryHelper {
    /**
     * Same as {@link net.minecraft.client.multiplayer.MultiPlayerGameMode#handleInventoryMouseClick} but without the sync check.
     */
    fun clickSlotUnchecked(syncId: Int, slotId: Int, button: Int, clickType: ClickType, player: Player) {
        val abstractContainerMenu = player.containerMenu
        val nonNullList: NonNullList<Slot> = abstractContainerMenu.slots
        val l = nonNullList.size
        val list: MutableList<ItemStack> = Lists.newArrayListWithCapacity<ItemStack>(l)

        for (slot in nonNullList) {
            list.add(slot.getItem().copy())
        }

        abstractContainerMenu.clicked(slotId, button, clickType, player)
        val int2ObjectMap: Int2ObjectMap<HashedStack> = Int2ObjectOpenHashMap<HashedStack>()

        val connection = Minecraft.getInstance().connection!!
        for (m in 0..<l) {
            val itemStack = list.get(m)
            val itemStack2 = nonNullList.get(m).getItem()
            if (!ItemStack.matches(itemStack, itemStack2)) {
                int2ObjectMap.put(m, HashedStack.create(itemStack2, connection.decoratedHashOpsGenenerator()))
            }
        }

        val hashedStack =
            HashedStack.create(abstractContainerMenu.getCarried(), connection.decoratedHashOpsGenenerator())
        connection
            .send(
                ServerboundContainerClickPacket(
                    syncId,
                    abstractContainerMenu.getStateId(),
                    Shorts.checkedCast(slotId.toLong()),
                    SignedBytes.checkedCast(button.toLong()),
                    clickType,
                    int2ObjectMap,
                    hashedStack
                )
            )
    }
    fun findItemType(client: Minecraft, item: Item): Int? = findItemInHotbarByPredicate(client) {
        it?.item == item
    }

    fun findItemInHotbarByPredicate(client: Minecraft, predicate: Predicate<ItemStack?>): Int? {
        for (i in 0..<9) {
            if (predicate.test(client.player?.inventory?.getItem(i)))
                return i
        }
        return null
    }

    fun clickSlotAndReturnCoroutine(scope: CoroutineScope, client: Minecraft, slot: Int): Job {
        val currentSlot = client.player!!.inventory.selectedSlot
        return scope.launch {
            changeSlot(client, slot)
            inventoryInteractDelay()
            client.execute { client.startUseItem() }
            inventoryInteractDelay()
            changeSlot(client, currentSlot)
        }
    }

    suspend fun inventoryInteractDelay() {
        val random = HypixelQolFabric.RANDOM
        delay(random.nextLong(50, 100))
    }

    fun changeSlot(client: Minecraft, slot: Int) {
        client.execute {
            if (client.screen != null || client.isPaused) return@execute
            client.player!!.inventory.selectedSlot = slot
        }
    }


}
