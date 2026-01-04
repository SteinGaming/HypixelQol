package de.steingaming.hqol.listeners

import de.steingaming.hqol.HypixelQol
import de.steingaming.hqol.Utilities
import net.minecraft.client.Minecraft
import net.minecraft.entity.item.EntityArmorStand
import net.minecraft.util.Vec3
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent

class DungeonListener {
    companion object {
        val relicBoxes = listOf<RelicBox>(
            RelicBox(90, 54, 94, 58),
            RelicBox(18, 57, 22, 61),
        )
    }
    class RelicBox(val minX: Int, val minZ: Int, val maxX: Int, val maxZ: Int) {
        fun insideBox(pos: Vec3): Boolean =
            pos.xCoord >= minX && pos.xCoord <= maxX &&
            pos.zCoord >= minZ && pos.zCoord <= maxZ
    }

    var lastClicked: Long = 0
    @SubscribeEvent
    fun relicTriggerBot(e: TickEvent.ClientTickEvent) {
        if (e.phase != TickEvent.Phase.START) return

        if (!HypixelQol.config.dungeonConfig.relicTriggerBot) return
        if (lastClicked + 1000 > System.currentTimeMillis()) return
        if (Minecraft.getMinecraft()?.objectMouseOver?.entityHit !is EntityArmorStand) return
        val player = Minecraft.getMinecraft().thePlayer
        if (!relicBoxes.any { it.insideBox(player.positionVector) }) return

        lastClicked = System.currentTimeMillis()
        Utilities.sendRightClick()
    }
}