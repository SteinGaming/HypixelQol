package de.steingaming.fqol.listeners

import de.steingaming.fqol.HypixelQol
import de.steingaming.fqol.HypixelQol.Companion.config
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.minecraft.block.Block
import net.minecraft.client.Minecraft
import net.minecraft.item.Item
import net.minecraftforge.event.entity.player.PlayerInteractEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

class GhostListener {
    @SubscribeEvent
    fun onPunch(event: PlayerInteractEvent) {
        if ((!HypixelQol.instance.ghostEnabled && Item.getIdFromItem(Minecraft.getMinecraft().thePlayer?.heldItem?.item ?: return) != 285) ||
            event.action != PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK || event.entityPlayer != Minecraft.getMinecraft().thePlayer) return

        if (Block.getIdFromBlock(Minecraft.getMinecraft().theWorld?.getBlockState(event.pos)?.block ?: return) in config.ghostConfig.ignoreBlockList)
            return

        event.isCanceled = true
        HypixelQol.scope.launch {
            delay(100)
            Minecraft.getMinecraft().theWorld.setBlockToAir(event.pos)
        }
    }
}