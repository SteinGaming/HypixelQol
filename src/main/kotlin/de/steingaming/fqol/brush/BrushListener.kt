package de.steingaming.fqol.brush

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import net.minecraft.client.Minecraft
import net.minecraftforge.event.world.ChunkEvent
import net.minecraftforge.fml.common.eventhandler.EventPriority
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

class BrushListener {
    val dungeons7Regex = ".*\\([MF]7\\)".toRegex()

    @SubscribeEvent(priority = EventPriority.LOWEST)
    fun onChunkRender(event: ChunkEvent.Load) {
        if (!Minecraft.getMinecraft().theWorld.scoreboard.teams.map { "${it.colorPrefix} ${it.colorSuffix}" }.any {
                dungeons7Regex.matches(it)
            }) return
        GlobalScope.launch {
            Brush.applyM7(event.chunk)
        }
    }
}