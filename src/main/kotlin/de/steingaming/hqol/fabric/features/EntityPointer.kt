package de.steingaming.hqol.fabric.features

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.StringArgumentType
import de.steingaming.hqol.fabric.HypixelQolFabric
import de.steingaming.hqol.fabric.Utilities
import de.steingaming.hqol.fabric.Utilities.cleanupColorCodes
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
import net.minecraft.client.multiplayer.ClientLevel
import net.minecraft.client.player.LocalPlayer
import net.minecraft.commands.CommandBuildContext
import net.minecraft.core.Registry
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.resources.Identifier
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.EquipmentSlot
import net.minecraft.world.entity.MobCategory
import net.minecraft.world.item.Items
import kotlin.jvm.optionals.getOrNull

object EntityPointer : Feature {
    abstract class CustomEntityPointerType(val name: String) {
        abstract fun collectEntities(mc: Minecraft, player: LocalPlayer, level: ClientLevel): List<Entity>
    }
    val list = mutableSetOf<EntityType<*>>()
    val customTypeList = mutableSetOf<CustomEntityPointerType>()
    val customTypeEnabledList = mutableSetOf<CustomEntityPointerType>()
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
        val level = mc.level ?: return

        for (entityType in list) {
            val entities = level.getEntities(
                entityType, Utilities.getAABBEquidistant(
                    player.position(), distance
                )
            ) { true }
            for (entity in entities) {
                entitiesToDrawTo.add(entity)
            }
        }
        for (customType in customTypeEnabledList) {
            entitiesToDrawTo.addAll(customType.collectEntities(mc, player, level))
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

    val RARITIES = listOf("Trackable", "Untrackable", "Undetected", "Endangered", "Elusive")

    override fun init(mc: Minecraft) {
        customTypeList.add(object: CustomEntityPointerType("trapper") {
            override fun collectEntities(mc: Minecraft, player: LocalPlayer, level: ClientLevel): List<Entity> {
                val entities = level.getEntities(
                    //? if >= 26.2 {
                    net.minecraft.world.entity.EntityTypes.ARMOR_STAND
                    //? } else
                    //EntityType.ARMOR_STAND
                    , Utilities.getAABBEquidistant(
                player.position(), distance
                )) {
                    val name = it.displayName.string.cleanupColorCodes()
                    RARITIES.any { rarity ->
                        name.contains(rarity)
                    }
                }
                return entities.mapNotNull { armorstandEntity ->
                    var nearestAnimal: Pair<Entity, Double>? = null
                    level.getEntities().get(
                        Utilities.getAABBEquidistant(armorstandEntity.position(), 5.0)
                    ) consumer@{ animalEntity ->
                        if (animalEntity.type.category != MobCategory.CREATURE)
                            return@consumer
                        val distance = animalEntity.distanceToSqr(armorstandEntity)
                        if (nearestAnimal != null && distance > nearestAnimal!!.second)
                            return@consumer
                        nearestAnimal = animalEntity to animalEntity.distanceToSqr(armorstandEntity)
                    }
                    nearestAnimal?.first
                }
            }
        })
        customTypeList.add(object: CustomEntityPointerType("corpse") {
            override fun collectEntities(mc: Minecraft, player: LocalPlayer, level: ClientLevel): List<Entity> {
                return level.getEntities(
                    //? if >= 26.2 {
                    net.minecraft.world.entity.EntityTypes.ARMOR_STAND
                    //? } else
                    //EntityType.ARMOR_STAND
                    , Utilities.getAABBEquidistant(
                        player.position(), distance
                    )) {
                    it.showArms()
                            && it.getItemBySlot(EquipmentSlot.CHEST).item == Items.LEATHER_CHESTPLATE
                }
            }
        })
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
                    .suggests { s, builder ->
                        customTypeList.forEach {
                            if (it !in customTypeEnabledList && it.name.contains(builder.remaining.lowercase()))
                                builder.suggest(it.name)
                        }
                        getAvailableEntityTypes().forEach {
                            val name = entityTypeToString(it)
                            if (it !in list && name.contains(builder.remaining.lowercase()))
                                builder.suggest(entityTypeToString(it))
                        }
                        customTypeList.forEach {
                            if (it !in customTypeEnabledList && it.name.contains(builder.remaining.lowercase()))
                            builder.suggest(it.name)
                        }
                        builder.buildFuture()
                    }
                    .executes { source ->
                        val input = source.getArgument("entity_type", String::class.java)
                        val entityType = stringToEntityTypes(input)
                        if (entityType != null) {
                            list.add(entityType)
                            ChatHelper.sendToChat("Added $entityType!")
                        } else customTypeList.find { it.name.equals(input, true) }?.let {
                            customTypeEnabledList.add(it)
                            ChatHelper.sendToChat("Added (custom type) ${it.name}!")
                        } ?: let {
                            ChatHelper.sendToChat("Couldn't find entity type \"$input\"")
                        }
                        1
                    }
            )
        ).then(literal("remove").then(
            ClientCommands.argument("entity_type", StringArgumentType.greedyString())
                .suggests { _, builder ->
                    customTypeEnabledList.forEach {
                        if (it.name.contains(builder.remaining.lowercase()))
                            builder.suggest(it.name)
                    }
                    list.forEach {
                        val name = entityTypeToString(it)
                        if (name.contains(builder.remaining.lowercase()))
                            builder.suggest(name)
                    }
                    customTypeEnabledList.forEach {
                        if (it.name.contains(builder.remaining.lowercase()))
                            builder.suggest(it.name)
                    }
                    builder.buildFuture()
                }.executes { source ->
                    val input = source.getArgument("entity_type", String::class.java)
                    val entityType = stringToEntityTypes(input)
                    if (entityType != null) {
                        list.remove(entityType)
                        ChatHelper.sendToChat("Removed $entityType!")
                    } else customTypeEnabledList.find { it.name.equals(input, true) }?.let {
                        customTypeEnabledList.remove(it)
                        ChatHelper.sendToChat("Removed (custom type) ${it.name}!")
                    } ?: let {
                        ChatHelper.sendToChat("Couldn't find entity type \"$input\"")
                        return@executes 1
                    }
                    1
                }
        )))
        dispatcher.register(
            literal("ep").redirect(cmd)
        )
    }

    private fun getEntityTypesRegistry(): Registry<EntityType<*>> =
        BuiltInRegistries.ENTITY_TYPE

    private fun getAvailableEntityTypes(): List<EntityType<*>> {
        return getEntityTypesRegistry().toList()
    }

    private fun entityTypeToString(type: EntityType<*>): String {
        return getEntityTypesRegistry().getKey(type)?.toString() ?: "unknown"
    }

    private fun stringToEntityTypes(str: String): EntityType<*>? {
        return getEntityTypesRegistry().get(Identifier.parse(str)).getOrNull()?.value()
    }
}