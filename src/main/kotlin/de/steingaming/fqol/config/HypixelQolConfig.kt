package de.steingaming.fqol.config

import com.google.gson.annotations.Expose
import de.steingaming.fqol.annotations.Hidden
import de.steingaming.fqol.config.subconfigs.FishingConfig
import de.steingaming.fqol.config.subconfigs.GhostConfig
import de.steingaming.fqol.config.subconfigs.MiscellaneousConfig
import java.lang.reflect.Field

data class HypixelQolConfig(
    val fishingConfig: FishingConfig = FishingConfig(),
    val ghostConfig: GhostConfig = GhostConfig(),
    val miscConfig: MiscellaneousConfig = MiscellaneousConfig()
) {
    class Range(val min: Long, val max: Long) {
        override fun toString(): String = "$min..$max"
    }

    class Properties<T>(
        @Hidden @Transient val instance: () -> T,
        @Hidden @Transient val newInstance: () -> T,
    ) {

        private val nameToField: Map<String, Field> =
            instance()!!::class.java.declaredFields.filter { it.type != Properties::class.java }.associate { it.name.lowercase() to it.also { f -> f.isAccessible = true } }

        class InvalidNameException : Exception()
        class InvalidValueException(val instead: String) : Exception()

        fun toTableArray(): Array<Array<String>> {
            return nameToField.map {
                arrayOf(it.key, it.value.get(instance()).toString())
            }.toTypedArray()
        }

        fun namesList(): Set<String> =
            nameToField.keys


        fun keyValueSet(): Map<String, Any> =
            nameToField.mapValues {
                it.value.get(instance())
            }

        fun reset(name: String) {
            nameToField[name.lowercase()]?.let {
                it.set(instance(), it.get(newInstance()))
                return
            }
            throw InvalidNameException()
        }

        private val defaultParser: Map<Class<*>, (String) -> Any> = mapOf(
            Boolean::class.java to {
                it.lowercase().toBooleanStrictOrNull() ?:
                    throw InvalidValueException("Should be \"true\" or \"false\"")
            },
            Int::class.java to {
                it.toIntOrNull() ?:
                    throw InvalidValueException("Should be an integer (number) instead")
            },
            Long::class.java to {
                it.toLongOrNull() ?:
                throw InvalidValueException("Should be an float (number) instead")
            },
            Double::class.java to {
                it.toDoubleOrNull() ?:
                throw InvalidValueException("Should be an double (number with decibel points) instead")
            },
            Range::class.java to {
                val (min, max) = it.split("..").takeIf { l -> l.size == 2 }?.let { l ->
                    (l.getOrNull(0)?.toLongOrNull() ?: return@let null) to
                            (l.getOrNull(1)?.toLongOrNull() ?: return@let null)
                } ?: throw InvalidValueException("Should be number range, example: \"min..max\"")
                Range(min, max)
            }
        )
        /**
         * Uses type to detect how to parse the value
         * For custom classes, override this function and call super.set() afterward.
         */
        fun set(name: String, value: String) {
            nameToField[name.lowercase()]?.let {
                it.set(instance(), (defaultParser[it.type] ?: throw InternalError("No valid parser found.")).invoke(value))
                return
            }
            throw InvalidNameException()
        }
    }
}