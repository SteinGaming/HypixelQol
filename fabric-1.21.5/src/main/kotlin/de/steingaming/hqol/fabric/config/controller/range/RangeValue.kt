package de.steingaming.hqol.fabric.config.controller.range

import kotlin.random.Random


data class RangeValue(
    val lowerValue: Int,
    val upperValue: Int) {
    init {
        assert(upperValue > lowerValue)
    }

    fun getRandomValue(): Int {
        return Random.nextInt(lowerValue, upperValue)
    }
}