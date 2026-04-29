package de.steingaming.hqol.fabric.features

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.StringArgumentType
import de.steingaming.hqol.fabric.HypixelQolFabric
import de.steingaming.hqol.fabric.config.categories.Misc
import de.steingaming.hqol.fabric.helper.ChatHelper
import de.steingaming.hqol.fabric.helper.CommandHelper.literal
import de.steingaming.hqol.fabric.helper.RenderHelper
import de.steingaming.hqol.fabric.model.Feature
import net.fabricmc.fabric.api.client.command.v2.ClientCommands
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
//? if >= 26.1 {
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents
//? } else {
/*import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents
import net.fabricmc.fabric.api.client.rendering.v1.world.LevelRenderContext
*///?}
import net.minecraft.client.Minecraft
import net.minecraft.commands.CommandBuildContext
import net.minecraft.core.Registry
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.resources.Identifier
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.EntityType
import net.minecraft.world.phys.AABB
import kotlin.jvm.optionals.getOrNull

object EntityPointer : Feature {
    val list = mutableSetOf<EntityType<*>>()
    @JvmStatic
    val entitiesToDrawTo = mutableListOf<Entity>()
    @JvmStatic
    val config: Misc.EntityPointer
        get() = HypixelQolFabric.INSTANCE.configManager.config.misc.ep
    val distance: Double
        get() {
            val mc = Minecraft.getInstance()
            return mc.options.simulationDistance().get() * 8.0
        }

    fun collectEntities(mc: Minecraft) {
        entitiesToDrawTo.clear()
        val player = mc.player ?: return
        val level = player.level()

        for (entityType in list) {
            val entities = level.getEntities(
                entityType, AABB(
                    player.position().subtract(distance), player.position().add(distance)
                )
            ) { true }
            for (entity in entities) {
                entitiesToDrawTo.add(entity)
            }
        }
        if (entitiesToDrawTo.isNotEmpty() && config.nearestOnly) {
            val nearest = entitiesToDrawTo.minBy {
                it.distanceTo(player)
            }
            entitiesToDrawTo.clear()
            entitiesToDrawTo.add(nearest)
        }
    }

    @JvmStatic
    fun render(context: LevelRenderContext) {
        if (!config.lines) return
        val color = config.linesColor.getEffectiveColourRGB()
        val lineWidth = config.lineWidth
        for (pos in entitiesToDrawTo) {
            RenderHelper.drawLineFromCrosshair(context, pos.position(), color, lineWidth)
        }
    }

    override fun init(mc: Minecraft) {
        //? if >= 26.1 {
        LevelRenderEvents.BEFORE_TRANSLUCENT_TERRAIN.register { render(it) }
        //?} else {
        /*WorldRenderEvents.END_MAIN.register {
            render(it)
        }
        *///?}

        ClientTickEvents.START_CLIENT_TICK.register { collectEntities(it) }
    }

    override fun registerCommands(
        dispatcher: CommandDispatcher<FabricClientCommandSource>,
        registryAccess: CommandBuildContext
    ) {
        val cmd = dispatcher.register(literal("entitypointer").then(
            literal("add").then(
                ClientCommands.argument("entity_type", StringArgumentType.greedyString())
                    .suggests { _, builder ->
                        getAvailableEntityTypes().forEach {
                            if (it !in list)
                                builder.suggest(entityTypeToString(it))
                        }
                        builder.buildFuture()
                    }
                    .executes { source ->
                        val input = source.getArgument("entity_type", String::class.java)
                        val entityType = stringToEntityType(input) ?: let {
                            ChatHelper.sendToChat("Couldn't find entity type \"$input\"")
                            return@executes 1
                        }
                        list.add(entityType)
                        ChatHelper.sendToChat("Added $entityType!")
                        1
                    }
            )
        ).then(literal("remove").then(
            ClientCommands.argument("entity_type", StringArgumentType.greedyString())
                .suggests { _, builder ->
                    list.forEach {
                        builder.suggest(entityTypeToString(it))
                    }
                    builder.buildFuture()
                }.executes { source ->
                    val input = source.getArgument("entity_type", String::class.java)
                    val entityType = stringToEntityType(input) ?: let {
                        ChatHelper.sendToChat("Couldn't find entity type \"$input\"")
                        return@executes 1
                    }
                    list.remove(entityType)
                    ChatHelper.sendToChat("Removed $entityType!")
                    1
                }
        )))
        dispatcher.register(
            literal("ep").redirect(cmd)
        )
    }

    private fun getEntityTypeRegistry(): Registry<EntityType<*>> =
        BuiltInRegistries.ENTITY_TYPE

    private fun getAvailableEntityTypes(): List<EntityType<*>> {
        return getEntityTypeRegistry().toList()
    }

    private fun entityTypeToString(type: EntityType<*>): String {
        return getEntityTypeRegistry().getKey(type)?.toString() ?: "unknown"
    }

    private fun stringToEntityType(str: String): EntityType<*>? {
        return getEntityTypeRegistry().get(Identifier.parse(str)).getOrNull()?.value()
    }
}