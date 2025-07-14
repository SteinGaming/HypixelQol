package de.steingaming.hqol.brush

import com.google.gson.Gson
import de.steingaming.hqol.HypixelQol
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import net.minecraft.block.Block
import net.minecraft.client.Minecraft
import net.minecraft.util.BlockPos
import net.minecraft.world.chunk.Chunk

object Brush {
    private data class BrushConstruct(val brush: MutableList<BrushEntry>) {
        data class BrushEntry(val pos: BrushPosition, val data: BrushData) {
            data class BrushPosition(val x: Int, val y: Int, val z: Int) {
                fun toBlockPos(): BlockPos {
                    return BlockPos(x, y, z)
                }

                fun inChunk(chunk: Chunk?): Boolean {
                    if (chunk == null) return true
                    val minX = chunk.xPosition * 16
                    val maxX = (chunk.xPosition + 1) * 16 - 1
                    val minZ = chunk.zPosition * 16
                    val maxZ = (chunk.zPosition + 1) * 16 - 1
                    return (x in minX..maxX && z in minZ..maxZ)
                }
            }
            data class BrushData(val block: Int, val meta: Int)
        }
    }

    suspend fun applyM7(chunk: Chunk? = null) = coroutineScope {
        launch {
            val brushCfg = Gson().fromJson(
                (HypixelQol::class.java.classLoader.getResourceAsStream("brush-m7.json") ?: let {
                    println("BRUSH M7 RETURNED NULL")
                    return@launch
                }).readBytes().decodeToString(),
                BrushConstruct::class.java
            )

            for (entry in brushCfg.brush.filter {
                it.pos.inChunk(chunk)
            }) {
                Minecraft.getMinecraft().theWorld.setBlockState(
                    entry.pos.toBlockPos(), Block.getBlockById(entry.data.block).getStateFromMeta(entry.data.meta)
                )
            }
        }
    }
}