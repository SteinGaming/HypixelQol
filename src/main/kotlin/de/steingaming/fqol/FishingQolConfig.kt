package de.steingaming.fqol

data class FishingQolConfig(
    val waterPreCatchDelay: FishingDelays = FishingDelays(200, 400),
    val lavaPreCatchDelay: FishingDelays = FishingDelays(150, 310),
    val castRodDelay: FishingDelays = FishingDelays(300, 600),
    val properties: FishingProperties = FishingProperties(),
    var enabled: Boolean = false
) {
    data class FishingDelays(var min: Long, var max: Long)

    data class FishingProperties(var randomMovementTicks: Long = 0, var soundDistanceToRod: Double = 2.0, var inactivityDisableTicks: Long = 600) {
        class InvalidNameException : Exception()
        class InvalidValueException(val instead: String) : Exception()

        fun toTableArray(): Array<Array<String>> {
            return this::class.java.declaredFields.map {
                arrayOf(it.name, it.get(this).toString())
            }.toTypedArray()

        }

        fun set(name: String, value: String) {
            when (name.lowercase()) {
                "randomMovementTicks".lowercase() -> {
                    randomMovementTicks =
                        value.toLongOrNull() ?: throw InvalidValueException("Should be long (number) instead")
                }

                "inactivityDisableTicks".lowercase() -> {
                    inactivityDisableTicks =
                        value.toLongOrNull() ?: throw InvalidValueException("Should be long (number) instead")
                }

                "soundDistanceToRod".lowercase() -> {
                    soundDistanceToRod = value.toDoubleOrNull()
                        ?: throw InvalidValueException("Should be double (number with decibel points) instead")
                }

                else -> throw InvalidNameException()
            }
        }

        fun namesList(): List<String> =
            this::class.java.declaredFields.map {
                it.name
            }


        fun reset(name: String) {
            this::class.java.declaredFields.find {
                it.name.equals(name, true)
            }?.let {
                it.set(this, it.get(FishingProperties()))
                return
            }
            throw InvalidNameException()
        }
    }
}
