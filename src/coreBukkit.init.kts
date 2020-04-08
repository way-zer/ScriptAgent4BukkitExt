@file:ImportByClass("org.bukkit.Bukkit")
@file:DependsModule("coreLibrary")

import cf.wayzer.script_agent.Config
import coreBukkit.lib.ContentExt.listener
import coreBukkit.lib.ContentExt.listens
import coreBukkit.lib.ContentExt.commands
import coreBukkit.lib.ContentExt.subCommands
import coreBukkit.lib.ContentExt.bukkitTasks
import coreBukkit.lib.pluginMain
import coreBukkit.lib.pluginCommand
import coreBukkit.lib.PluginCommander
import coreBukkit.lib.ModuleExt
import coreBukkit.lib.ContentExt
import org.bukkit.Bukkit
import org.bukkit.command.SimpleCommandMap
import org.bukkit.event.HandlerList

name = "Bukkit 核心脚本模块"

addLibraryByClass("org.bukkit.Bukkit")
addDefaultImport("coreBukkit.lib.*")
addDefaultImport("org.bukkit.ChatColor.*")
generateHelper()

onEnable{
    DataStoreApi.open(Config.pluginMain.dataFolder.resolve("dataStore.db").path)
    ConfigBuilder.init(Config.pluginMain.dataFolder.resolve("config.conf"))
    Config.pluginCommand.setExecutor(PluginCommander)
}

onDisable{
    DataStoreApi.close()
}

onAfterContentEnable { child ->
    child.listens.forEach {
        Bukkit.getPluginManager().registerEvent(it.cls, child.listener, it.priority, it, Config.pluginMain, it.ignoreCancelled)
    }
    child.subCommands.forEach(PluginCommander::addSub)
    ModuleExt.getCommandMap().run {
        child.commands.forEach {
            register(Config.pluginMain.description.name,it)
        }
    }
}

onBeforeContentDisable{child->
    HandlerList.unregisterAll(child.listener)
    PluginCommander.removeAll(child)
    ModuleExt.getCommandMap().run {
        val knownCommands = SimpleCommandMap::class.java.getDeclaredField("knownCommands")
                .apply { isAccessible = true }.get(this) as MutableMap<*, *>
        val toRemove = mutableListOf<Any?>()
        knownCommands.forEach { (k, v) ->
            if (v is ContentExt.ScriptCommand && v.script == child) {
                toRemove.add(k)
            }
        }
        toRemove.forEach{knownCommands.remove(it)}
    }
    child.bukkitTasks.forEach { it.cancel() }
}