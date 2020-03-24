@file:ImportByClass("org.bukkit.Bukkit")
//@file:MavenDepends("org.mapdb:mapdb:3.0.7")
//@file:MavenDepends("org.eclipse.collections:eclipse-collections:10.1.0")
//@file:MavenDepends("org.eclipse.collections:eclipse-collections-api:10.1.0")
//@file:MavenDepends("org.eclipse.collections:eclipse-collections-forkjoin:10.1.0")
//@file:MavenDepends("net.jpountz.lz4:lz4:1.3.0")
//@file:MavenDepends("org.mapdb:elsa:3.0.0-M5")
@file:MavenDepends("de.tr7zw:item-nbt-api:2.2.0","https://repo.codemc.org/repository/maven-public/")


import cf.wayzer.script_agent.bukkit.Helper.baseConfig
import cf.wayzer.script_agent.bukkit.Helper.exportClass
import cf.wayzer.script_agent.bukkit.Manager
//import org.mapdb.DBMaker
import superitem.lib.*
//import superitem.lib.features.DataStore
import superitem.lib.events.*

name.set("SuperItem 模块")
addLibraryByClass("de.tr7zw.changeme.nbtapi.NBTItem")
addDefaultImport("superitem.lib.*")
addDefaultImport("superitem.lib.features.*")
addDefaultImport("org.bukkit.Material")
exportClass(ItemStackHandleEvent::class.java)
exportClass(SuperItemEvent::class.java)
baseConfig()
generateHelper()

onEnable{
    val rootDir = Manager.pluginMain.dataFolder.resolve("Superitem")
    rootDir.mkdirs()
    ConfigManager.init(rootDir.resolve("config"))
//    DataStore.fileDB = DBMaker.fileDB(rootDir.resolve("data.mapdb"))
//            .fileMmapEnableIfSupported().transactionEnable().closeOnJvmShutdown().make()
//    DataStore.memoryDB = DBMaker.heapDB().make()
}

onDisable{
//    DataStore.fileDB.close()
//    DataStore.memoryDB.close()
}

onBeforeContentEnable{ item ->
    //item.require(Permission())
    item[SIManager.features].values.flatten().apply {
        forEach { ConfigManager.loadForFeature(item,it) }
        forEach { if(it is Feature.OnPostLoad){ it.onPostLoad() } }
    }
}
onAfterContentEnable{
    SIManager.items[it.clsName.toUpperCase()] = it
    ConfigManager.saveForItem(it)
}

onAfterContentDisable{ item->
    SIManager.items.remove(item.clsName.toUpperCase())
    item[SIManager.features].values.flatten().apply {
        forEach { if(it is Feature.OnDisable){ it.onDisable() } }
    }
}