package de.steingaming.fqol.listeners

import de.steingaming.fqol.HypixelQol
import net.minecraft.block.Block
import net.minecraft.block.material.Material
import net.minecraft.client.Minecraft
import net.minecraft.item.Item
import net.minecraft.util.BlockPos
import net.minecraft.util.ChatComponentText
import net.minecraft.util.MovingObjectPosition
import net.minecraft.util.MovingObjectPosition.MovingObjectType
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent
import kotlin.random.Random

class MiscellaneousListener {

    private val terminatorTicks: Long?
        get() = HypixelQol.config.miscConfig.terminatorCPS.let {
            if (it == 0L) null
            else (20 / it) + randomDelay
        }

    private var randomDelay: Long = Random.nextLong(0, 2)

    private var ticks: Long = 0

    private var pressed = false

    @SubscribeEvent
    fun termTickListener(e: TickEvent.ClientTickEvent) {
        if (terminatorTicks == null) return
        if (e.phase != TickEvent.Phase.START || Minecraft.getMinecraft()?.thePlayer == null || Minecraft.getMinecraft().isGamePaused || Minecraft.getMinecraft().currentScreen != null) return


        // This is to ensure that this doesn't start in the tick where the key is held down
        // and stops immediately when it is released
        when (pressed) {
            true -> if (!Minecraft.getMinecraft().gameSettings.keyBindAttack.isKeyDown) {
                pressed = false
                return
            }

            false -> {
                if (Minecraft.getMinecraft().gameSettings.keyBindAttack.isKeyDown) pressed = true
                return
            }
        }
        if (Minecraft.getMinecraft().thePlayer?.heldItem?.let {
                it.displayName.contains("Terminator") && it.item == Item.getItemById(261)
            } != true || ticks++ < (terminatorTicks ?: return)) return

        if (Minecraft.getMinecraft().objectMouseOver.typeOfHit == MovingObjectType.BLOCK && Block.getIdFromBlock(Minecraft.getMinecraft().theWorld.getBlockState(Minecraft.getMinecraft().objectMouseOver?.blockPos ?: return).block) !in listOf(
                0, 8, 9, 10, 11
            )
        ) return

        ticks = 0
        randomDelay = Random.nextLong(0, 2)
        Minecraft.getMinecraft().thePlayer.swingItem()


        when (Minecraft.getMinecraft().objectMouseOver.typeOfHit) {

            MovingObjectType.ENTITY -> {
                Minecraft.getMinecraft().playerController.attackEntity(
                    Minecraft.getMinecraft().thePlayer, Minecraft.getMinecraft().objectMouseOver.entityHit
                )
            }

            MovingObjectType.BLOCK -> {
                val blockpos: BlockPos = Minecraft.getMinecraft().objectMouseOver.blockPos;
                if (Minecraft.getMinecraft().theWorld.getBlockState(blockpos).block.material != Material.air) {
                    Minecraft.getMinecraft().playerController.clickBlock(
                        blockpos, Minecraft.getMinecraft().objectMouseOver.sideHit
                    )
                }
            }

            else -> {}
        }

    }
}