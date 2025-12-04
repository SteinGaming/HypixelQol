package de.steingaming.hqol.helpers

import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent

class DelayedExecutor(
    var ticks: Long, val tickPhase: TickEvent.Phase = TickEvent.Phase.START, val runnable: () -> Unit,
) {
    init {
        MinecraftForge.EVENT_BUS.register(this)
    }

    @SubscribeEvent
    fun onTick(e: TickEvent.ClientTickEvent) {
        if (e.phase != tickPhase) return
        if (ticks-- >= 0) return

        MinecraftForge.EVENT_BUS.unregister(this)
        runnable()
    }
}