package de.steingaming.fqol.listeners

import de.steingaming.fqol.HypixelQol
import de.steingaming.fqol.HypixelQol.Companion.config
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.minecraft.block.Block
import net.minecraft.block.material.Material
import net.minecraft.client.Minecraft
import net.minecraft.item.Item
import net.minecraft.util.BlockPos
import net.minecraft.util.EnumFacing
import net.minecraftforge.event.entity.player.PlayerInteractEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

class GhostListener {
    @SubscribeEvent
    fun onPunch(event: PlayerInteractEvent) {
        if ((!HypixelQol.instance.ghostEnabled && Item.getIdFromItem(
                Minecraft.getMinecraft().thePlayer?.heldItem?.item ?: return
            ) != 285) ||
            event.action != PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK || event.entityPlayer != Minecraft.getMinecraft().thePlayer
        ) return

        if (Block.getIdFromBlock(
                Minecraft.getMinecraft().theWorld?.getBlockState(event.pos)?.block ?: return
            ) in config.ghostConfig.ignoreBlockList
        )
            return

        event.isCanceled = true
        HypixelQol.scope.launch {
            delay(70)
            if (!Minecraft.getMinecraft().thePlayer.isSneaking)
                Minecraft.getMinecraft().theWorld.setBlockToAir(event.pos)
            else {
                val direction = Minecraft.getMinecraft().thePlayer.horizontalFacing
                for (i in 0..16) {
                    val block = event.pos.direction(direction, i) ?: return@launch
                    if (Block.getIdFromBlock(
                            Minecraft.getMinecraft().theWorld?.getBlockState(block)?.block ?: return@launch
                        ) in config.ghostConfig.ignoreBlockList)
                    // Check if block exists, else break
                    if (Minecraft.getMinecraft().theWorld.getBlockState(block).block.material == Material.air) break
                    Minecraft.getMinecraft().theWorld.setBlockToAir(block)
                }
            }
        }
    }
}

private fun BlockPos.direction(direction: EnumFacing, i: Int): BlockPos? {
    return when (direction) {
        EnumFacing.NORTH -> this.north(i)
        EnumFacing.SOUTH -> this.south(i)
        EnumFacing.EAST -> this.east(i)
        EnumFacing.WEST -> this.west(i)
        else -> {
            println("IMPOSSIBLE")
            null
        }
    }
}


private fun BlockPos.reverseDirection(direction: EnumFacing, i: Int): BlockPos? {
    return when (direction) {
        EnumFacing.NORTH -> this.south(i)
        EnumFacing.SOUTH -> this.north(i)
        EnumFacing.EAST -> this.west(i)
        EnumFacing.WEST -> this.east(i)
        else -> {
            println("IMPOSSIBLE")
            null
        }
    }
}
