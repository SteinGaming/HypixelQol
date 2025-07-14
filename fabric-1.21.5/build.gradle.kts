import net.fabricmc.loom.task.RemapJarTask

plugins {
    val loom_version = "1.11-SNAPSHOT"
    id("fabric-loom").version(loom_version)
}

repositories {
    maven("https://maven.isxander.dev/releases") {
        name = "Xander Maven"
    }
    maven("https://maven.terraformersmc.com/") {
        name = "Terraformers"
    }
}

dependencies {
    minecraft("com.mojang:minecraft:${project.property("minecraft_version")}")
    mappings("net.fabricmc:yarn:${project.property("yarn_mappings")}:v2")

    modImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:${project.property("coroutines_version")}")
    modImplementation("net.fabricmc:fabric-loader:${project.property("loader_version")}")
    modImplementation("net.fabricmc.fabric-api:fabric-api:${project.property("fabric_version")}")
    //modImplementation("net.fabricmc.fabric-api:fabric-sound-api-v1:${project.property("fabric_version")}")

    modImplementation("dev.isxander:yet-another-config-lib:${project.property("yacl_version")}")
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
                "version" to project.version,
                "yacl_version" to project.property("yacl_version")
            )
        }
    }

    named<RemapJarTask>("remapJar") {
        archiveFileName.set("HypixelQol-${project.name}-v${project.version}.jar")
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