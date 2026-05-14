package de.steingaming.hqol.fabric

object Utilities {
    fun String.cleanupColorCodes(): String {
        return this.replace("[\u00a7&][0-9a-fk-or]".toRegex(), "")
    }

    infix fun <A, B, C> Pair<A, B>.to(c: C): Triple<A, B, C> =
        Triple(this.first, this.second, c)
}
