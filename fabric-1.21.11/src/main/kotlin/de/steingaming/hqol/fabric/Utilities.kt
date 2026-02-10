package de.steingaming.hqol.fabric

import com.google.common.collect.Lists
import com.google.common.primitives.Shorts
import com.google.common.primitives.SignedBytes
import it.unimi.dsi.fastutil.ints.Int2ObjectMap
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import net.minecraft.client.MinecraftClient
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.network.packet.c2s.play.ClickSlotC2SPacket
import net.minecraft.screen.ScreenHandler
import net.minecraft.screen.slot.Slot
import net.minecraft.screen.slot.SlotActionType
import net.minecraft.screen.sync.ItemStackHash
import net.minecraft.text.Text
import net.minecraft.util.collection.DefaultedList

object Utilities {
    fun String.cleanupColorCodes(): String {
        return this.replace("[\u00a7&][0-9a-fk-or]".toRegex(), "")
    }
    fun sendToChat(msg: String) {
        MinecraftClient.getInstance().inGameHud.chatHud.addMessage(
            Text.of(msg)
        )
    }

    /**
     * Same as {@link net.minecraft.client.network.ClientPlayerInteractionManager#clickSlot} but without the sync check.
     */
    fun clickSlotUnchecked(syncId: Int, slotId: Int, button: Int, actionType: SlotActionType, player: PlayerEntity) {
        val screenHandler: ScreenHandler = player.currentScreenHandler

        val defaultedList: DefaultedList<Slot> = screenHandler.slots
        val i = defaultedList.size
        val list: MutableList<ItemStack?> = Lists.newArrayListWithCapacity<ItemStack?>(i)

        for (slot in defaultedList) {
            list.add(slot.getStack().copy())
        }

        screenHandler.onSlotClick(slotId, button, actionType, player)
        val int2ObjectMap: Int2ObjectMap<ItemStackHash?> = Int2ObjectOpenHashMap<ItemStackHash?>()

        val networkHandler = MinecraftClient.getInstance().networkHandler!!
        for (j in 0..<i) {
            val itemStack = list.get(j)
            val itemStack2 = defaultedList.get(j).getStack()
            if (!ItemStack.areEqual(itemStack, itemStack2)) {
                int2ObjectMap.put(j, ItemStackHash.fromItemStack(itemStack2, networkHandler.getComponentHasher()))
            }
        }

        val itemStackHash =
            ItemStackHash.fromItemStack(screenHandler.getCursorStack(), networkHandler.getComponentHasher())
        networkHandler
            .sendPacket(
                ClickSlotC2SPacket(
                    syncId,
                    screenHandler.getRevision(),
                    Shorts.checkedCast(slotId.toLong()),
                    SignedBytes.checkedCast(button.toLong()),
                    actionType,
                    int2ObjectMap,
                    itemStackHash
                )
            )
    }
}