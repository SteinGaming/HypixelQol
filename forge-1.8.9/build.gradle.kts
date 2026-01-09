import com.github.jengelman.gradle.plugins.shadow.ShadowJavaPlugin.Companion.shadowJar
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.jetbrains.kotlin.gradle.utils.extendsFrom
import java.net.URI

plugins {
    id("gg.essential.loom") version "1.9.29"
    id("dev.architectury.architectury-pack200") version "0.1.3"
    id("com.gradleup.shadow") version "9.0.0-beta13"
    id("io.github.juuxel.loom-quiltflower") version "1.7.3"
}


loom {
    forge {
        pack200Provider.set(dev.architectury.pack200.java.Pack200Adapter())
        mixinConfig("mixins.hypixelqol.json")
    }
    mixin {
        defaultRefmapName.set("mixins.hypixelqol.refmap.json")
        useLegacyMixinAp.set(true)
    }
}
repositories {
    mavenCentral()
    mavenLocal()
    maven {
        url = URI("https://maven.architectury.dev/")
        isAllowInsecureProtocol = true
    }
    maven("https://repo.spongepowered.org/maven/")
    maven("https://pkgs.dev.azure.com/djtheredstoner/DevAuth/_packaging/public/maven/v1")
    maven("https://jitpack.io")
    maven("https://repo.hypixel.net/repository/Hypixel/")
    maven("https://maven.notenoughupdates.org/releases/")
}

val shadowImplementation by configurations.creating
val shadowApi by configurations.creating

configurations.compileClasspath.extendsFrom(configurations.named("shadowImplementation"))
configurations.runtimeClasspath.extendsFrom(configurations.named("shadowImplementation"))
dependencies {
    minecraft("com.mojang:minecraft:1.8.9")
    mappings("de.oceanlabs.mcp:mcp_stable:22-1.8.9")
    forge("net.minecraftforge:forge:1.8.9-11.15.1.2318-1.8.9")

    implementation("net.hypixel:mod-api:1.0.1")
    shadowImplementation("org.spongepowered:mixin:0.7.11-SNAPSHOT")
    annotationProcessor("org.spongepowered:mixin:0.8.5-SNAPSHOT")
    annotationProcessor("com.google.code.gson:gson:2.10.1")
    annotationProcessor("com.google.guava:guava:17.0")

    shadowImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
    shadowImplementation("org.notenoughupdates.moulconfig:legacy:4.2.0-beta")
}
tasks.withType(org.gradle.jvm.tasks.Jar::class) {
    manifest.attributes.run {
        this["MixinConfigs"] = "mixins.hypixelqol.json"
        this["TweakClass"] = "org.spongepowered.asm.launch.MixinTweaker"
        this["FMLCorePluginContainsFMLMod"] = "true"
        this["ForceLoadAsMod"] = "true"
        this["Manifest-Version"] = "1.0"
    }
}
val remapJar by tasks.named<net.fabricmc.loom.task.RemapJarTask>("remapJar") {
    dependsOn(project.tasks.shadowJar)
    archiveFileName.set("HypixelQol-${project.name}-v${project.version}.jar")
    println(tasks.shadowJar.get().archiveFile)
    inputFile.set(project.tasks.shadowJar.get().archiveFile)
    doLast {
        println("Jar name: ${archiveFile.get().asFile}")
        copy {
            from(archiveFile.get().asFile)
            into("$rootDir/output")
        }
    }
}

tasks.named<ShadowJar>("shadowJar") {
    archiveClassifier.set("dep-dev")
    configurations.set(mutableListOf(project.configurations.getByName("shadowImplementation"), project.configurations.getByName("shadowApi")))
    relocate("io.github.notenoughupdates.moulconfig", "de.steingaming.hqol.deps.moulconfig")
}
tasks.processResources {
    filesMatching("mcmod.info") {
        expand("version" to project.version)
    }
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