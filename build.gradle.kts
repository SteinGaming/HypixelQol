plugins {
    idea
    java
    kotlin("jvm") version "2.2.0"
    id("net.fabricmc.fabric-loom-remap")
    id("com.gradleup.shadow") version "9.3.1"
}
group = "de.steingaming"
version = "${property("mod.version")}+${sc.current.version}"
base.archivesName = property("mod.id") as String

val requiredJava = when {
    sc.current.parsed >= "1.20.5" -> JavaVersion.VERSION_21
    sc.current.parsed >= "1.18" -> JavaVersion.VERSION_17
    sc.current.parsed >= "1.17" -> JavaVersion.VERSION_16
    else -> JavaVersion.VERSION_1_8
}
val minecraft = sc.current.version
val accesswidener = when {
    stonecutter.eval(minecraft, ">=1.21.11") -> "1.21.11.accesswidener"
    stonecutter.eval(minecraft, ">=1.20.10") -> "1.21.10.accesswidener"
    else -> "1.19.accesswidener"
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
    mappings(loom.officialMojangMappings())
    modImplementation("net.fabricmc:fabric-loader:${property("deps.fabric_loader")}")
    modImplementation("net.fabricmc:fabric-language-kotlin:1.13.9+kotlin.2.3.10")
    modImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
    shadowModImpl("org.notenoughupdates.moulconfig:modern-${sc.current.version}:${project.property("moulconfig.version")}")
    modImplementation("com.terraformersmc:modmenu:${project.property("modmenu_version")}")

    //modImplementation("net.fabricmc.fabric-api:fabric-api:${property("deps.fabric_api")}")
    fapi("fabric-lifecycle-events-v1", "fabric-resource-loader-v0", "fabric-content-registries-v0", "fabric-command-api-v2", "fabric-sound-api-v1")
}

loom {
    fabricModJsonPath = rootProject.file("src/main/resources/fabric.mod.json") // Useful for interface injection
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

        val props = mapOf(
            "id" to project.property("mod.id"),
            "name" to project.property("mod.name"),
            "version" to project.property("mod.version"),
            "minecraft" to project.property("mod.mc_dep"),
            "aw_file" to accesswidener
        )

        filesMatching("fabric.mod.json") { expand(props) }

        val mixinJava = "JAVA_${requiredJava.majorVersion}"
        filesMatching("*.mixins.json") { expand("java" to mixinJava) }
    }

    // Builds the version into a shared folder in `build/libs/${mod version}/`
    register<Copy>("buildAndCollect") {
        group = "build"
        from(remapJar.map { it.archiveFile }, remapSourcesJar.map { it.archiveFile })
        into(rootProject.layout.buildDirectory.file("libs/${project.property("mod.version")}"))
        dependsOn("build")
    }
    val remapJar = named<net.fabricmc.loom.task.RemapJarTask>("remapJar") {
        archiveClassifier.set("")
        dependsOn(shadowJar)
        inputFile.set(shadowJar.get().archiveFile)
        destinationDirectory.set(rootProject.layout.buildDirectory.dir("libs"))
    }
    shadowJar {
        archiveClassifier.set("dev")
        mergeServiceFiles()
        relocate("io.github.notenoughupdates.moulconfig", "de.steingaming.hqol.deps.moulconfig")
        configurations = listOf(shadowModImpl)
    }
}
/*
subprojects {
    afterEvaluate {
        val serviceLocation = try {
            project.property("moulconfig-service")
        } catch (e: MissingPropertyException) {
            null
        }
        if (serviceLocation != null)
            tasks.named<Jar>("shadowJar") {
                // Fix IMinecraft service
                doFirst {
                    val genServicesDir = File(layout.buildDirectory.asFile.get(), "generated-resources/services/META-INF/services")
                    genServicesDir.mkdirs()
                    File(
                        genServicesDir,
                        "de.steingaming.hqol.deps.moulconfig.common.IMinecraft"
                    ).writeText("$serviceLocation\n")
                }
                from(File(layout.buildDirectory.asFile.get(), "generated-resources/services")) {
                    into("")
                }
            }
    }
}*/