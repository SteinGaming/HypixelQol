package de.steingaming.hqol.fabric

import kotlinx.coroutines.runBlocking
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.screens.Screen
import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.Vec3

object Utilities {
    fun String.cleanupColorCodes(): String {
        return this.replace("[\u00a7&][0-9a-fk-or]".toRegex(), "")
    }

    infix fun <A, B, C> Pair<A, B>.to(c: C): Triple<A, B, C> =
        Triple(this.first, this.second, c)

    fun getAABBEquidistant(position: Vec3, distance: Double): AABB {
        return AABB(
            position.subtract(distance), position.add(distance)
        )
    }

    inline fun <T, R> T.runCatchingBlocking(crossinline block: suspend T.() -> R): Result<R> {
        return try {
            runBlocking { Result.success(block()) }
        } catch (e: Throwable) {
            Result.failure(e)
        }
    }
    fun Minecraft.screen(): Screen? =
            //? >= 26.2 {
            this.gui.screen()
            //? } else
            //this.screen

}
