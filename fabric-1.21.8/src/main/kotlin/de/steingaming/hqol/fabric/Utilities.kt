package de.steingaming.hqol.fabric

object Utilities {
    fun String.cleanupColorCodes(): String {
        return this.replace("[\u00a7&][0-9a-fk-or]".toRegex(), "")
    }
}