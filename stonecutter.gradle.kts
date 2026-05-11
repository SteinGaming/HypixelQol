plugins {
    id("dev.kikugie.stonecutter")
    id("net.fabricmc.fabric-loom-remap") version "1.15-SNAPSHOT" apply false
    // id("me.modmuss50.mod-publish-plugin") version "1.0.+" apply false
}

stonecutter active "26.1"

/*
// Make newer versions be published last
stonecutter tasks {
    order("publishModrinth")
    order("publishCurseforge")
}
 */

// See https://stonecutter.kikugie.dev/wiki/config/params
stonecutter parameters {
    swaps["mod_version"] = "\"${property("mod.version")}\";"
    swaps["minecraft"] = "\"${node.metadata.version}\";"
    constants["release"] = property("mod.id") != "template"
    dependencies["fapi"] = node.project.property("deps.fabric_api") as String

    replacements {
        string(current.parsed < "26.1") {
            replace("ContainerInput", "ClickType")
            replace("ClientCommands", "ClientCommandManager")
            replace("LevelRenderContext", "WorldRenderContext")
            replace("ClientLevelEvents", "ClientWorldEvents")
            replace("START_LEVEL_TICK", "START_WORLD_TICK")
            replace("AFTER_CLIENT_LEVEL_CHANGE", "AFTER_CLIENT_WORLD_CHANGE")
        }
        string(current.parsed >= "26.1") {
            replace("ClientCommandManager", "ClientCommands")
            replace("WorldRenderContext", "LevelRenderContext")
            replace("ClientWorldEvents", "ClientLevelEvents")
            replace("AFTER_CLIENT_WORLD_CHANGE", "AFTER_CLIENT_LEVEL_CHANGE")
        }
    }
}