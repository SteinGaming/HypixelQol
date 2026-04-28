package de.steingaming.hqol.fabric.model

import com.mojang.brigadier.builder.LiteralArgumentBuilder
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource
import net.minecraft.client.Minecraft

interface Feature {
    fun init(mc: Minecraft) {}
    fun registerCommands(): List<LiteralArgumentBuilder<FabricClientCommandSource>> = emptyList()
}