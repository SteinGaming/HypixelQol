package de.steingaming.hqol.fabric.config.controller.range

import kotlin.random.Random


data class RangeValue(
    val lowerValue: Int,
    val upperValue: Int) {
    init {
        assert(upperValue > lowerValue)
    }
    companion object {
        @Transient
        val rand = Random(System.nanoTime())
    }

    fun getRandomValue(): Int {
        return rand.nextInt(lowerValue, upperValue)
    }
}