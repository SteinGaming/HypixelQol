package de.steingaming.hqol.fabric.model

import com.mojang.brigadier.CommandDispatcher
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource
import net.minecraft.client.Minecraft
import net.minecraft.commands.CommandBuildContext

interface Feature {
    fun init(mc: Minecraft) {}
    fun registerCommands(dispatcher: CommandDispatcher<FabricClientCommandSource>, registryAccess: CommandBuildContext) {}
}