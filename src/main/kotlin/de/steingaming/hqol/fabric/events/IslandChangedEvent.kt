package de.steingaming.hqol.fabric.events

import net.fabricmc.fabric.api.event.Event
import net.fabricmc.fabric.api.event.EventFactory
import net.hypixel.data.type.ServerType
import net.hypixel.modapi.packet.impl.clientbound.event.ClientboundLocationPacket
import kotlin.jvm.optionals.getOrNull

fun interface IslandChangedEvent {
    companion object {
        val EVENT: Event<IslandChangedEvent> = EventFactory.createArrayBacked(IslandChangedEvent::class.java
        ) run@{ callbacks ->
            return@run IslandChangedEvent {
                for (callback in callbacks) {
                    callback.onIslandChanged(it)
                }
            }
        }
    }
    fun onIslandChanged(location: Location)

    data class Location(val serverName: String, val serverType: ServerType?, val lobbyName: String?, val mode: String?, val map: String?,) {
        constructor(packet: ClientboundLocationPacket): this(packet.serverName, packet.serverType.getOrNull(), packet.lobbyName.getOrNull(), packet.mode.getOrNull(), packet.map.getOrNull())
    }
}