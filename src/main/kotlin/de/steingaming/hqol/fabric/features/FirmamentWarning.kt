package de.steingaming.hqol.fabric.features

import de.steingaming.hqol.fabric.FirmamentWarningFrame
import de.steingaming.hqol.fabric.HypixelQolFabric
import net.fabricmc.loader.impl.FabricLoaderImpl
import net.fabricmc.loader.impl.util.LoaderUtil
import net.fabricmc.loader.impl.util.UrlUtil
import java.awt.GraphicsEnvironment
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.exists
import kotlin.system.exitProcess

object FirmamentWarning {
    val classPath: Path = UrlUtil.getCodeSource(FirmamentWarningFrame::class.java)

    fun check() {
        val firmament = FabricLoaderImpl.INSTANCE.allMods.find {
            it.metadata.id == "firmament"
        }
        if (HypixelQolFabric.INSTANCE.configManager.config.misc.firmamentWarningShown || firmament == null) return
        val continueExecution: Boolean = if (GraphicsEnvironment.isHeadless()) {
            openForked()
        } else
            FirmamentWarningFrame.showWarningWindow()
        if (!continueExecution)
            exitProcess(0)

        HypixelQolFabric.INSTANCE.configManager.config.misc.firmamentWarningShown = true
        HypixelQolFabric.INSTANCE.configManager.save()
    }

    // Inspired by https://github.com/FabricMC/fabric-loader/blob/master/src/main/java/net/fabricmc/loader/impl/gui/FabricGuiEntry.java
    fun openForked(): Boolean {
        val binDir = LoaderUtil.normalizePath(Paths.get(System.getProperty("java.home"), "bin"))

        var javaPath: Path? = null
        for (executable in listOf("javaw.exe", "java.exe", "javaw", "java")) {
            val executableFile = binDir.resolve(executable)
            if (executableFile.exists()) {
                javaPath = executableFile
                break
            }
        }
        javaPath!!
        val process = ProcessBuilder(javaPath.toString(), "-Xmx100M", "-cp", classPath.toString(),
            FirmamentWarningFrame::class.java.name)
            .inheritIO()
            .start()

        process.waitFor()

        return process.exitValue() == 0
    }
}