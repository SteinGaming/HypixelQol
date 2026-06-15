package de.steingaming.hqol.fabric.events

import net.fabricmc.fabric.api.event.Event
import net.fabricmc.fabric.api.event.EventFactory
import net.minecraft.world.entity.Entity

fun interface CustomNameUpdateEvent {
    companion object {
        @JvmStatic
        val EVENT: Event<CustomNameUpdateEvent> = EventFactory.createArrayBacked(CustomNameUpdateEvent::class.java) run@{ callbacks ->
                return@run CustomNameUpdateEvent { entity ->
                    for (callback in callbacks) {
                        callback.onEntityNameUpdate(entity)
                    }
                }
            }
    }

    fun onEntityNameUpdate(entity: Entity)
}