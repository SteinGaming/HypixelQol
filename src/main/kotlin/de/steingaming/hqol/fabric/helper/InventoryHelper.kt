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
import net.minecraft.world.inventory.ContainerInput
import net.minecraft.world.inventory.Slot
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import java.util.function.Predicate

object InventoryHelper {
    /**
     * Same as {@link net.minecraft.client.multiplayer.MultiPlayerGameMode#handleContainerInput} but without the sync check.
     */

    fun clickSlotUnchecked(containerId: Int, slotNum: Int, buttonNum: Int, containerInput: ContainerInput, player: Player) {
        val containerMenu = player.containerMenu
        val connection = Minecraft.getInstance().connection!!

        val slots: NonNullList<Slot> = containerMenu.slots
        val slotCount = slots.size
        val itemsBeforeClick: MutableList<ItemStack?> = Lists.newArrayListWithCapacity<ItemStack?>(slotCount)

        for (slot in slots) {
            itemsBeforeClick.add(slot.getItem().copy())
        }

        containerMenu.clicked(slotNum, buttonNum, containerInput, player)
        val changedSlots: Int2ObjectMap<HashedStack> = Int2ObjectOpenHashMap<HashedStack>()

        for (i in 0..<slotCount) {
            val before = itemsBeforeClick.get(i) as ItemStack
            val after = slots.get(i).getItem()
            if (!ItemStack.matches(before, after)) {
                changedSlots.put(i, HashedStack.create(after, connection.decoratedHashOpsGenenerator()))
            }
        }

        val carriedItem = HashedStack.create(containerMenu.getCarried(), connection.decoratedHashOpsGenenerator())
        connection.send(
                ServerboundContainerClickPacket(
                    containerId,
                    containerMenu.getStateId(),
                    Shorts.checkedCast(slotNum.toLong()),
                    SignedBytes.checkedCast(buttonNum.toLong()),
                    containerInput,
                    changedSlots,
                    carriedItem
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
