@file:DependsModule("coreBukkit")
@file:MavenDepends("de.tr7zw:item-nbt-api:2.2.0", "https://repo.codemc.org/repository/maven-public/")

import cf.wayzer.script_agent.Config
import coreLibrary.lib.dataDirectory
import superitem.lib.*
import superitem.lib.events.ItemStackHandleEvent
import superitem.lib.events.SuperItemEvent
import superitem.lib.features.Permission

name = "SuperItem 模块"
addLibraryByClass("de.tr7zw.changeme.nbtapi.NBTItem")
addDefaultImport("superitem.lib.*")
addDefaultImport("superitem.lib.features.*")
addDefaultImport("org.bukkit.Material")
exportClass(ItemStackHandleEvent::class.java)
exportClass(SuperItemEvent::class.java)
generateHelper()

onEnable {
    val rootDir = Config.pluginMain.dataFolder.resolve("Superitem")
    rootDir.mkdirs()
    ConfigManager.init(Config.dataDirectory.resolve("superitem"))
}

onBeforeContentEnable { item ->
    item.require(Permission())
    item.features.values.flatten().apply {
        forEach { ConfigManager.loadForFeature(item, it) }
        forEach {
            if (it is Feature.OnPostLoad) {
                it.onPostLoad()
            }
        }
    }
}
onAfterContentEnable {
    SIManager.items[it.clsName.toUpperCase()] = it
    ConfigManager.saveForItem(it)
}

onAfterContentDisable { item ->
    SIManager.items.remove(item.clsName.toUpperCase())
    item.features.values.flatten().apply {
        forEach {
            if (it is Feature.OnDisable) {
                it.onDisable()
            }
        }
    }
}