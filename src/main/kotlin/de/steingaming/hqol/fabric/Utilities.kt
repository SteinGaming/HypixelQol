package de.steingaming.hqol.fabric

import com.mojang.blaze3d.systems.RenderSystem
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.minecraft.client.Minecraft
import net.minecraft.network.chat.Component

object Utilities {

}

fun String.cleanupColorCodes(): String {
    return this.replace("[\u00a7&][0-9a-fk-or]".toRegex(), "")
}