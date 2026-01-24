import groovy.lang.MissingPropertyException

plugins {
    idea
    java
    kotlin("jvm") version "2.2.0"
}

allprojects {
    apply(plugin = "idea")
    apply(plugin = "java")
    apply(plugin = "kotlin")

    repositories {
        mavenCentral()
    }

    version = "2.5.0-SNAPSHOT"
    group = "de.steingaming"
}

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
}