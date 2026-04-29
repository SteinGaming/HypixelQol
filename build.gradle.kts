
plugins {
    idea
    java
    kotlin("jvm") version "2.3.20"
    id("dev.kikugie.loom-back-compat")
    id("com.gradleup.shadow") version "9.3.1"
}
group = "de.steingaming"
version = "${property("mod.version")}+${sc.current.version}"
project.base.archivesName = property("mod.id") as String

val requiredJava = when {
    sc.current.parsed >= "26.1" -> JavaVersion.VERSION_25
    sc.current.parsed >= "1.20.5" -> JavaVersion.VERSION_21
    sc.current.parsed >= "1.18" -> JavaVersion.VERSION_17
    sc.current.parsed >= "1.17" -> JavaVersion.VERSION_16
    else -> JavaVersion.VERSION_1_8
}
val minecraft = sc.current.version
val accesswidener = when {
    stonecutter.eval(minecraft, ">=26.1") -> "26.1.accesswidener"
    stonecutter.eval(minecraft, ">=1.21.11") -> "1.21.11.accesswidener"
    stonecutter.eval(minecraft, ">=1.20.10") -> "1.21.10.accesswidener"
    else -> null
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
    //maven("https://maven.notenoughupdates.org/releases/")
}
val shadowModImpl by configurations.creating {
    configurations.modImplementation.get().extendsFrom(this)
}

dependencies {
    /**
     * Fetches only the required Fabric API modules to not waste time downloading all of them for each version.
     * @see <a href="https://github.com/FabricMC/fabric">List of Fabric API modules</a>
     */
    fun fapi(vararg modules: String) {
        for (it in modules) modImplementation(fabricApi.module(it, property("deps.fabric_api") as String))
    }

    minecraft("com.mojang:minecraft:${sc.current.version}")
    loomx.applyMojangMappings()
    modImplementation("net.fabricmc:fabric-loader:${property("deps.fabric_loader")}")
    modImplementation("net.fabricmc:fabric-language-kotlin:1.13.9+kotlin.2.3.10")
    modImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
    shadowModImpl("org.notenoughupdates.moulconfig:modern-${sc.current.version}:${project.property("moulconfig.version")}")
    modImplementation("com.terraformersmc:modmenu:${project.property("modmenu_version")}")

    //modImplementation("net.fabricmc.fabric-api:fabric-api:${property("deps.fabric_api")}")
    fapi("fabric-lifecycle-events-v1", "fabric-resource-loader-v0", "fabric-content-registries-v0", "fabric-command-api-v2", "fabric-sound-api-v1", "fabric-renderer-api-v1", "fabric-rendering-v1", "fabric-content-registries-v0", "fabric-registry-sync-v0")
}

loom {
    fabricModJsonPath = rootProject.file("src/main/resources/fabric.mod.json") // Useful for interface injection
    if (accesswidener != null)
        accessWidenerPath = rootProject.file("src/main/resources/hqol-$accesswidener")

    decompilerOptions.named("vineflower") {
        options.put("mark-corresponding-synthetics", "1") // Adds names to lambdas - useful for mixins
    }

    runConfigs.all {
        ideConfigGenerated(true)
        vmArgs("-Dmixin.debug.export=true") // Exports transformed classes for debugging
        runDir = "../../run" // Shares the run directory between versions
    }
}
java {
    withSourcesJar()
    targetCompatibility = requiredJava
    sourceCompatibility = requiredJava
}

kotlin {
    jvmToolchain(requiredJava.majorVersion.toInt())
}
tasks {
    processResources {
        inputs.property("id", project.property("mod.id"))
        inputs.property("name", project.property("mod.name"))
        inputs.property("version", project.property("mod.version"))
        inputs.property("minecraft", project.property("mod.mc_dep"))
        inputs.property("java_version", requiredJava.majorVersion)

        val props = mutableMapOf(
            "id" to project.property("mod.id"),
            "name" to project.property("mod.name"),
            "version" to project.property("mod.version"),
            "minecraft" to project.property("mod.mc_dep"),
            "java_version" to requiredJava.majorVersion,
        )
        if (accesswidener != null)
            props["aw_file"] = accesswidener

        filesMatching("fabric.mod.json") { expand(props) }

        val mixinJava = "JAVA_${requiredJava.majorVersion}"
        filesMatching("*.mixins.json") { expand("java" to mixinJava) }
    }

    if (sc.eval(minecraft, "<=1.21.11")) {
        named<net.fabricmc.loom.task.RemapJarTask>("remapJar") {
            archiveClassifier.set("")
            dependsOn(shadowJar)
            inputFile.set(shadowJar.get().archiveFile)
            destinationDirectory.set(rootProject.layout.buildDirectory.dir("libs"))
            copy {
                from(archiveFile)
                into(rootProject.layout.buildDirectory.file("libs/${project.property("mod.version")}"))
            }
        }
    } else {
        build.get().dependsOn("shadowJar")
    }
    shadowJar {
        archiveClassifier.set(
            if (sc.current.parsed < "26.1") "dev" else ""
        )
        mergeServiceFiles()
        relocate("io.github.notenoughupdates.moulconfig", "de.steingaming.hqol.deps.moulconfig")
        configurations = listOf(shadowModImpl)
        if (sc.current.parsed >= "26.1") copy {
                from(archiveFile)
                into(rootProject.layout.buildDirectory.file("libs/${project.property("mod.version")}"))
            }
    }

    register<Copy>("buildAndCollect") {
        group = "build"
        dependsOn("build")
        from(
            if (sc.current.parsed < "26.1") named<org.gradle.jvm.tasks.Jar>("remapJar").map { it.archiveFile }
            else shadowJar.map { it.archiveFile }
        )
        into(rootProject.layout.buildDirectory.file("output/"))
    }
}