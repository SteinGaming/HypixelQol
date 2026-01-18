package de.steingaming.hqol.fabric.config

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import de.steingaming.hqol.fabric.HypixelQolFabric
import java.lang.reflect.Modifier
import java.nio.file.Path
import kotlin.io.path.*


class ConfigManager(val configPath: Path) {
    companion object {
        val GSON: Gson = GsonBuilder().setPrettyPrinting().disableHtmlEscaping()
            .excludeFieldsWithModifiers(Modifier.TRANSIENT).create()
    }
    val updateListeners: MutableList<(Config) -> Unit> = mutableListOf()

    var config: Config
        private set


    init {
        val fileContents = readContents()
        config = if (fileContents.isNullOrEmpty()) {
            Config()
        } else parseConfig(fileContents)
        if (!configPath.exists()) {
            configPath.createFile()
            save()
        }
    }

    fun reload() {
        val fileContents = readContents()
        config = if (fileContents.isNullOrEmpty()) {
            Config()
        } else parseConfig(fileContents)
    }

    fun save() {
        configPath.writeText(
            GSON.toJson(config)
        )
    }

    private fun readContents(): String? {
        return configPath.takeIf { it.exists() && it.isRegularFile() && it.isReadable() }?.readText()
    }

    private fun parseConfig(str: String): Config {
        return try {
            GSON.fromJson<Config>(str, Config::class.java)
        } catch (e: Exception) {
            HypixelQolFabric.LOGGER.error("Failed to parse config from $configPath")
            e.printStackTrace()

            configPath.moveTo(Path.of(configPath.absolutePathString() + ".bak"))
            Config()
        }
    }
}