import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar.Companion.shadowJar
import net.fabricmc.loom.task.RemapJarTask
plugins {
    val loom_version = "1.11-SNAPSHOT"
    id("fabric-loom").version(loom_version)
    id("com.gradleup.shadow") version "9.3.0"
}

repositories {
    maven("https://maven.isxander.dev/releases") {
        name = "Xander Maven"
    }
    maven("https://maven.terraformersmc.com/") {
        name = "Terraformers"
    }
    maven("https://repo.steingaming.de/releases/") {
        name = "NEU-custom"
    }
}
val shadowModImpl by configurations.creating {
    configurations.modImplementation.get().extendsFrom(this)
}
dependencies {
    minecraft("com.mojang:minecraft:${project.property("minecraft_version")}")
    mappings("net.fabricmc:yarn:${project.property("yarn_mappings")}:v2")

    modImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:${project.property("coroutines_version")}")
    modImplementation("net.fabricmc:fabric-loader:${project.property("loader_version")}")
    modImplementation("net.fabricmc.fabric-api:fabric-api:${project.property("fabric_api_version")}")
    //modImplementation("net.fabricmc.fabric-api:fabric-sound-api-v1:${project.property("fabric_version")}")

    //modImplementation("dev.isxander:yet-another-config-lib:${project.property("yacl_version")}")
    shadowModImpl("org.notenoughupdates.moulconfig:modern-1.21.10:4.2.0-beta-custom")
    modImplementation("com.terraformersmc:modmenu:${project.property("modmenu_version")}")
}

kotlin.jvmToolchain(21)
java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

tasks {
    processResources {
        filesMatching("fabric.mod.json") {
            expand(
                "version" to project.version
            )
        }
    }

    shadowJar {
        configurations = listOf(shadowModImpl)
        relocate("io.github.notenoughupdates.moulconfig", "de.steingaming.hqol.deps.moulconfig")
    }

    named<RemapJarTask>("remapJar") {
        dependsOn(project.tasks.shadowJar)
        archiveFileName.set("HypixelQol-${project.name}-v${project.version}.jar")
        inputFile.set(shadowJar.get().archiveFile)
        doLast {
            println("Jar name: ${archiveFile.get().asFile}")
            copy {
                from(archiveFile.get().asFile)
                into("$rootDir/output")
            }
        }
    }

    java {
        withSourcesJar()
    }
}