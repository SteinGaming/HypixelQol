
rootProject.name = "HypixelQol"


pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
        maven("https://oss.sonatype.org/content/repositories/snapshots")
        maven("https://maven.architectury.dev/")
        maven("https://maven.fabricmc.net")
        maven(url = "https://jitpack.io/")
        maven(url = "https://maven.minecraftforge.net/")
        maven(url = "https://repo.spongepowered.org/maven/")
        maven(url = "https://repo.essential.gg/repository/maven-releases/")
        maven(url = "https://maven.architectury.dev/")
        maven("https://maven.kikugie.dev/snapshots") { name = "KikuGie Snapshots" }
    }
}

plugins {
    id("dev.kikugie.stonecutter") version "0.8.3"
}

stonecutter {
    create(rootProject) {
        // See https://stonecutter.kikugie.dev/wiki/start/#choosing-minecraft-versions
        versions("1.21.10", "1.21.11")
        vcsVersion = "1.21.10"
    }
}
