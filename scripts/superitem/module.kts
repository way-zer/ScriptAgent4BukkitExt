@file:Depends("coreBukkit")
@file:Import("https://repo.codemc.org/repository/maven-public/", mavenRepository = true)
@file:Import("de.tr7zw:item-nbt-api:2.9.0-SNAPSHOT", mavenDepends = true)
@file:Import("superitem.lib.*", defaultImport = true)
@file:Import("superitem.lib.features.*", defaultImport = true)
@file:Import("org.bukkit.Material", defaultImport = true)

package superitem

import cf.wayzer.scriptAgent.events.ScriptDisableEvent
import cf.wayzer.scriptAgent.events.ScriptEnableEvent
import superitem.lib.events.ItemStackHandleEvent
import superitem.lib.events.SuperItemEvent
import kotlin.collections.set

name = "SuperItem 模块"
exportClass(ItemStackHandleEvent::class.java)
exportClass(SuperItemEvent::class.java)

onEnable {
    ConfigManager.init(Config.dataDir.resolve("superitem"))
}

listenTo<ScriptEnableEvent>(Event.Priority.Before) {
    if (!script.isItem) return@listenTo
    val item = script
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
listenTo<ScriptEnableEvent>(Event.Priority.After) {
    if (!script.isItem) return@listenTo
    SIManager.items[script.clsName.uppercase()] = script
    ConfigManager.saveForItem(script)
}

listenTo<ScriptDisableEvent>(Event.Priority.After) {
    if (!script.isItem) return@listenTo
    SIManager.items.remove(script.clsName.uppercase())
}