package de.steingaming.hqol.fabric.helper

import com.mojang.brigadier.builder.LiteralArgumentBuilder
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource

object CommandHelper {
    fun literal(name: String): LiteralArgumentBuilder<FabricClientCommandSource> {
        return LiteralArgumentBuilder.literal(name)
    }
}