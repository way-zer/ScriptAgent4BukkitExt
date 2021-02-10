@file:ImportByClass("org.bukkit.Bukkit")
@file:DependsModule("coreLibrary")

import cf.wayzer.script_agent.Config
import coreBukkit.lib.*
import coreBukkit.lib.ContentExt.listener
import coreBukkit.lib.ContentExt.listens
import coreBukkit.lib.ContentExt.commands
import coreBukkit.lib.ContentExt.subCommands
import coreBukkit.lib.ContentExt.bukkitTasks
import coreLibrary.lib.Commands
import org.bukkit.Bukkit
import org.bukkit.event.HandlerList

name = "Bukkit 核心脚本模块"

addLibraryByClass("org.bukkit.Bukkit")
addDefaultImport("coreBukkit.lib.*")
addDefaultImport("org.bukkit.ChatColor.*")
generateHelper()

onEnable{
    Commands.rootProvider.set(RootCommands)
}

onAfterContentEnable { child ->
    child.listens.forEach {
        Bukkit.getPluginManager().registerEvent(it.cls, child.listener, it.priority, it, Config.pluginMain, it.ignoreCancelled)
    }
    child.subCommands.forEach(Commands.controlCommand::addSub)
    if(child.subCommands.isNotEmpty())
        Commands.controlCommand.autoRemove(child)
    child.commands.forEach(RootCommands::addSub)
    if(child.commands.isNotEmpty())
        RootCommands.autoRemove(child)
}

onBeforeContentDisable{child->
    HandlerList.unregisterAll(child.listener)
    child.bukkitTasks.forEach { it.cancel() }
}