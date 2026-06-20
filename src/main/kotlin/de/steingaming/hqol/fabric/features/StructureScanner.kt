package de.steingaming.hqol.fabric.features

import com.mojang.brigadier.Command
import com.mojang.brigadier.CommandDispatcher
import de.steingaming.hqol.fabric.HypixelQolFabric
import de.steingaming.hqol.fabric.events.IslandChangedEvent
import de.steingaming.hqol.fabric.helper.ChatHelper
import de.steingaming.hqol.fabric.model.Feature
import net.fabricmc.fabric.api.client.command.v2.ClientCommands
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLevelEvents
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.hypixel.data.type.GameType
import net.minecraft.client.Minecraft
import net.minecraft.client.multiplayer.ClientLevel
import net.minecraft.commands.CommandBuildContext
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.network.chat.ClickEvent
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.Style
import net.minecraft.world.level.ChunkPos
import net.minecraft.world.level.block.*
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.state.properties.Half
import net.minecraft.world.level.block.state.properties.SlabType
import net.minecraft.world.level.block.state.properties.StairsShape
import net.minecraft.world.level.material.FlowingFluid
import net.minecraft.world.level.material.Fluids


object StructureScanner: Feature {
    data class StructureData(
        val skyblockerStructureName: String,
        val originBlock: Pair<(state: BlockState) -> Boolean, (pos: BlockPos, state: BlockState) -> Boolean>,
        val relativeBlocks: List<Pair<BlockPos, (level: ClientLevel, pos: BlockPos, state: BlockState) -> Boolean>>,
        val relativeCenterBlock: BlockPos
    )

    var inCrystalHollows = false
    var initialStructures = mapOf<String, StructureData>()
    var unfoundStructures = mutableMapOf<String, StructureData>()

    override fun init(mc: Minecraft) {
        IslandChangedEvent.EVENT.register { location ->
            inCrystalHollows = location.serverType is GameType && location.serverType == GameType.SKYBLOCK && location.map == "Crystal Hollows"
        }
        initialStructures = getStructures()
        unfoundStructures = initialStructures.toMutableMap()
        ClientTickEvents.START_LEVEL_TICK.register(::onTick)
        ClientLevelEvents.AFTER_CLIENT_LEVEL_CHANGE.register(::onWorldChange)
    }

    override fun registerCommands(
        dispatcher: CommandDispatcher<FabricClientCommandSource>,
        registryAccess: CommandBuildContext
    ) {
        dispatcher.register(ClientCommands.literal("hqolforcess").executes {
            ChatHelper.sendToChat("Forcibly enabling Structure Scanner for this level...")
            inCrystalHollows = true
            return@executes Command.SINGLE_SUCCESS
        })
    }

    val lookedChunks = mutableListOf<ChunkPos>()

    fun onTick(level: ClientLevel) {
        if (!inCrystalHollows) return
        val config = HypixelQolFabric.INSTANCE.configManager.config.misc.structureScanner
        if (!config.enabled) return
        val viewRange = level.chunkSource.storage.viewRange
        val chunks = level.chunkSource.storage.chunks
        for (i in 0..<(viewRange * viewRange)) {
            val chunk = chunks.get(i) ?: continue
            if (chunk.pos in lookedChunks) continue
            var unloadedBlocksRequired = false
            for ((structName, structure) in unfoundStructures.toMap().entries) {
                chunk.findBlocks({ originState ->
                    structure.originBlock.first(originState)
                }, { originPos, originState ->
                    if (!structure.originBlock.second(originPos, originState)) return@findBlocks
                    val rotation = structure.relativeBlocks.first().let { (offset, func) ->
                        for (i in 0..3)
                            if (relativeBlockMatches(level, originPos, offset, i, func) ?: run {
                                    unloadedBlocksRequired = true
                                    false
                                })
                                return@let i
                        null
                    } ?: return@findBlocks
                    for ((i, relativeBlock) in structure.relativeBlocks.withIndex()) {
                        if (i == 0) continue
                        val (offset, func) = relativeBlock
                        if (!(relativeBlockMatches(level, originPos, offset, rotation, func) ?: run {
                                unloadedBlocksRequired = true
                                false
                            }))
                            return@findBlocks
                    }
                    unfoundStructures.remove(structName)
                    val centerPos = originPos.offset(structure.relativeCenterBlock.rotate(Rotation.entries[rotation]))
                    if (config.automaticallyAdd) {
                        ChatHelper.sendToChat("Found structure \"$structName\"! Added automatically using Skyblocker.")
                        ClientCommands.getActiveDispatcher()?.execute(
                            "skyblocker crystalWaypoints add ${centerPos.x} ${centerPos.y} ${centerPos.z} ${structure.skyblockerStructureName}",
                            Minecraft.getInstance().connection!!.suggestionsProvider as FabricClientCommandSource
                        )
                    } else
                        ChatHelper.sendToChat(
                            Component.literal("Found structure \"${structName}\" at ${centerPos.x}, ${centerPos.y}, ${centerPos.z}! Click this message to add it to Skyblocker.")
                                .withStyle(Style.EMPTY.withClickEvent(ClickEvent.RunCommand("/skyblocker crystalWaypoints add ${centerPos.x} ${centerPos.y} ${centerPos.z} ${structure.skyblockerStructureName}")))
                        )
                })
            }
            if (!unloadedBlocksRequired)
                lookedChunks += chunk.pos
        }
    }

    fun relativeBlockMatches(level: ClientLevel, origin: BlockPos, offset: BlockPos, rotation: Int, predicate: (level: ClientLevel, pos: BlockPos, state: BlockState) -> Boolean): Boolean? {
        val relativeBlockPos = origin.offset(offset.rotate(Rotation.entries[rotation]))
        if (!level.isLoaded(relativeBlockPos)) return null
        return predicate(level, relativeBlockPos, level.getBlockState(relativeBlockPos))
    }

    fun onWorldChange(mc: Minecraft, level: ClientLevel) {
        lookedChunks.clear()
        unfoundStructures = initialStructures.toMutableMap()
    }

    fun getStructures(): MutableMap<String, StructureData> {
        val map = mutableMapOf<String, StructureData>()
        map["Mines of Divan"] = StructureData(
            "Mines of Divan",
            { state: BlockState ->
                state.block == Blocks.QUARTZ_SLAB
            } to { _, state ->
                state.getValue(SlabBlock.TYPE) == SlabType.TOP
            },
            listOf(
                BlockPos(0,0,-1) to { _, _, state ->
                    state.block == Blocks.QUARTZ_PILLAR && state.getValue(RotatedPillarBlock.AXIS) == Direction.Axis.Y
                }, BlockPos(1,0,0) to { _, _, state ->
                    state.block == Blocks.WOOL.lime
                }, BlockPos(1,-1,0) to { _, _, state ->
                    state.block == Blocks.WOOL.lime
                }
            ),
            BlockPos(4, 16, 0)
        )
        map["Corleone Grave"] = StructureData(
            "Corleone",
            { state: BlockState ->
                state.block == Blocks.POLISHED_ANDESITE
            } to { _, _ -> true },
            listOf(
                BlockPos(0, 1, 1) to { _, _, state ->
                    state.block == Blocks.STONE_BRICK_STAIRS
                },
                BlockPos(0, 1, -1) to { _, _, state ->
                    state.block == Blocks.STONE_BRICK_STAIRS
                },
                BlockPos(0, 1, 0) to { _, _, state ->
                    state.block == Blocks.COBBLESTONE_WALL
                },
                BlockPos(0, 3, 0) to { _, _, state ->
                    state.block == Blocks.STONE_BRICK_SLAB && state.getValue(SlabBlock.TYPE) == SlabType.DOUBLE
                },
                BlockPos(0, 4, 0) to { _, _, state ->
                    state.block == Blocks.STONE_BRICK_SLAB && state.getValue(SlabBlock.TYPE) == SlabType.BOTTOM
                }
            ),
            BlockPos(-7, -1, 6)
        )
        map["Corleone Bridge"] = StructureData(
            "Corleone",
            { state: BlockState ->
                state.block == Blocks.FIRE
            } to { _, _ -> true },
            listOf(
                BlockPos(0, -1, 0) to { _, _, state ->
                    state.block == Blocks.STONE
                },
                BlockPos(0, -2, 0) to { _, _, state ->
                    state.block == Blocks.STONE_BRICK_STAIRS && state.getValue(StairBlock.HALF) == Half.TOP
                },
                BlockPos(-1, -1, 0) to { _, _, state ->
                    state.block == Blocks.STONE_BRICK_STAIRS && state.getValue(StairBlock.HALF) == Half.TOP
                },
                BlockPos(0, -1, -1) to { _, _, state ->
                    state.block == Blocks.STONE_BRICK_STAIRS && state.getValue(StairBlock.HALF) == Half.TOP
                },
                BlockPos(1, -1, 0) to { _, _, state ->
                    state.block == Blocks.STONE_BRICK_STAIRS && state.getValue(StairBlock.HALF) == Half.TOP
                },
                BlockPos(0, -1, 1) to { _, _, state ->
                    state.block == Blocks.STONE_BRICK_STAIRS && state.getValue(StairBlock.HALF) == Half.TOP
                },
                BlockPos(-2, -3, 0) to { _, _, state ->
                    state.block == Blocks.DYED_TERRACOTTA.cyan
                }
            ),
            BlockPos(-22, -2, 0)
        )
        map["Jungle Temple"] = StructureData(
            "Jungle Temple",
            { state: BlockState ->
                state.block == Blocks.STONE_BRICK_STAIRS &&
                        state.getValue(StairBlock.HALF) == Half.TOP &&
                        state.getValue(StairBlock.FACING) == Direction.NORTH &&
                        state.getValue(StairBlock.SHAPE) == StairsShape.OUTER_LEFT
            }
                    to { _, _ -> true },
            listOf(
                BlockPos(0, 1, 0) to { _, _, state ->
                    state.block == Blocks.STONE_BRICK_SLAB &&
                            state.getValue(SlabBlock.TYPE) == SlabType.BOTTOM
                },
                BlockPos(0, 0, -1) to { _, _, state ->
                    state.block == Blocks.STONE_BRICK_STAIRS &&
                            state.getValue(StairBlock.HALF) == Half.TOP &&
                            state.getValue(StairBlock.FACING) == Direction.WEST &&
                            state.getValue(StairBlock.SHAPE) == StairsShape.STRAIGHT
                },
                BlockPos(-1, 1, -1) to { _, _, state ->
                    state.block == Blocks.FIRE
                }
            ),
            BlockPos(4, 3, -8)
        )
        map["Khazad-d\u00FBm"] = StructureData(
            "Khazad-d\u00FBm",
            { state: BlockState ->
                state.fluidState.type == Fluids.FLOWING_LAVA &&
                        state.fluidState.getValue(FlowingFluid.FALLING)
            } to { _, _ -> true },
            listOf(
                BlockPos(1, 0, 1) to { _, _, state ->
                    state.fluidState.type == Fluids.FLOWING_LAVA &&
                            state.fluidState.getValue(FlowingFluid.FALLING)
                },
                BlockPos(2, 0, 2) to { _, _, state ->
                    state.fluidState.type == Fluids.FLOWING_LAVA &&
                            state.fluidState.getValue(FlowingFluid.FALLING)
                },
                BlockPos(0, 1, -2) to { _, _, state ->
                    state.block == Blocks.COBBLESTONE_WALL &&
                            state.getValue(WallBlock.UP)
                }
            ),
            BlockPos(-14, 13, -1)
        )
        map["King Yolkar"] = StructureData(
            "King Yolkar",
            { state: BlockState ->
                state.block == Blocks.DARK_OAK_STAIRS &&
                        state.getValue(StairBlock.HALF) == Half.BOTTOM &&
                        state.getValue(StairBlock.SHAPE) == StairsShape.STRAIGHT
            } to { _, _ -> true },
            listOf(
                BlockPos(1, -1, 0) to { _, _, state ->
                    state.block == Blocks.WOOL.red
                },
                BlockPos(2, -1, 0) to { _, _, state ->
                    state.block == Blocks.WOOL.red
                },
                BlockPos(2, -1, -1) to { _, _, state ->
                    state.block == Blocks.WOOL.red
                },
            ),
            BlockPos(2, 0, 0)
        )
        map["Goblin Queen's Den"] = StructureData(
            "Goblin Queen's Den",
            { state: BlockState ->
                state.block == Blocks.CAULDRON
            } to { _, _ -> true },
            listOf(
                BlockPos(0, 1, 0) to { _, _, state ->
                    state.block == Blocks.FIRE
                },
                BlockPos(-1, -3, -1) to { _, _, state ->
                    state.block == Blocks.SPRUCE_STAIRS
                },
            ),
            BlockPos(3, 3, 3)
        )
        map["Lost Precursor City"] = StructureData(
            "Lost Precursor City",
            { state: BlockState ->
                state.block == Blocks.FLOWER_POT
            } to { _, _ -> true },
            listOf(
                BlockPos(-1, 0, 0) to { _, _, state ->
                    state.block == Blocks.QUARTZ_STAIRS &&
                            state.getValue(StairBlock.FACING) == Direction.WEST &&
                            state.getValue(StairBlock.HALF) == Half.BOTTOM
                },
                BlockPos(5, 0, 5) to { _, _, state ->
                    state.block == Blocks.PLAYER_HEAD
                },
            ),
            BlockPos(0, 0, -1)
        )
        map["Key Guardian Tall"] = StructureData(
            "Key Guardian",
            { state: BlockState ->
                state.block == Blocks.HOPPER
            } to { _, _ -> true },
            listOf(
                BlockPos(-3, 0, 3) to { _, _, state ->
                    state.block == Blocks.HOPPER
                },
                BlockPos(-2, -3, 1) to { _, _, state ->
                    state.block == Blocks.STONE_BRICK_SLAB &&
                            state.getValue(SlabBlock.TYPE) == SlabType.BOTTOM
                }
            ),
            BlockPos(-2, -3, 1)
        )
        map["Key Guardian Temple"] = StructureData(
            "Key Guardian",
            { state: BlockState ->
                state.block == Blocks.JUNGLE_STAIRS &&
                        state.getValue(StairBlock.HALF) == Half.BOTTOM
            } to { _, _ -> true },
            listOf(
                BlockPos(-1, -1, 0) to { _, _, state ->
                    state.block == Blocks.GRANITE
                },
                BlockPos(-1, -2, 0) to { _, _, state ->
                    state.block == Blocks.POLISHED_GRANITE
                },
                BlockPos(-5, -1, 0) to { _, _, state ->
                    state.block == Blocks.GRANITE
                },
                BlockPos(-5, -2, 0) to { _, _, state ->
                    state.block == Blocks.POLISHED_GRANITE
                },
                BlockPos(0, -3, -1) to { _, _, state ->
                    state.block == Blocks.STONE_BRICK_STAIRS
                },
            ),
            BlockPos(-3, -1, 2)
        )
        return map
    }
}