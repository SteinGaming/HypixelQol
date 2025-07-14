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

    version = "2.0.0-SNAPSHOT"
    group = "eu.steingaming"
}
