import org.jetbrains.kotlin.gradle.utils.extendsFrom

plugins {
    idea
    java
    kotlin("jvm") version "1.8.20"
    id("gg.essential.loom") version "0.10.0.+"
    id("dev.architectury.architectury-pack200") version "0.1.3"
    id("com.github.johnrengelman.shadow") version "7.1.2"
    id("io.github.juuxel.loom-quiltflower") version "1.7.3"
}

loom {
    forge {
        pack200Provider.set(dev.architectury.pack200.java.Pack200Adapter())
    }
}

group = "eu.steingaming"
version = "1.1-SNAPSHOT"
//setVersionFromEnvironment("2.1")
repositories {
    mavenCentral()
    mavenLocal()
    maven("https://repo.spongepowered.org/maven/")
    maven("https://pkgs.dev.azure.com/djtheredstoner/DevAuth/_packaging/public/maven/v1")
    maven("https://jitpack.io")
}

val shadowImplementation by configurations.creating
val shadowApi by configurations.creating

configurations.compileClasspath.extendsFrom(shadowImplementation)
dependencies {
    minecraft("com.mojang:minecraft:1.8.9")
    mappings("de.oceanlabs.mcp:mcp_stable:22-1.8.9")
    forge("net.minecraftforge:forge:1.8.9-11.15.1.2318-1.8.9")

    shadowImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
}

val remapJar by tasks.named<net.fabricmc.loom.task.RemapJarTask>("remapJar") {
    archiveClassifier.set("dep")
    from(tasks.shadowJar)
    input.set(tasks.shadowJar.get().archiveFile)
    doLast {
        println("Jar name: ${archiveFile.get().asFile}")
        copy {
            from(archiveFile.get().asFile)
            into("$rootDir/output")
        }
    }
}

tasks.shadowJar {
    archiveClassifier.set("dep-dev")
    configurations = listOf(shadowImplementation, shadowApi)
    exclude("**/module-info.class", "LICENSE.txt")
    dependencies {
        exclude {
            it.moduleGroup.startsWith("org.apache.") || it.moduleName in
                    listOf("logback-classic", "commons-logging", "commons-codec", "logback-core")
        }
    }
    // fun relocate(name: String) = relocate(name, "io.github.moulberry.notenoughupdates.deps.$name")
}

tasks.assemble.get().dependsOn(remapJar)

kotlin {
    jvmToolchain(8)
}
java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(8))
    }
}