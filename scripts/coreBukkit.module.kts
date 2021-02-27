@file:ImportByClass("org.bukkit.Bukkit")
@file:DependsModule("coreLibrary")

import coreBukkit.lib.ListenExt
import coreBukkit.lib.RootCommands

name = "Bukkit 核心脚本模块"

addLibraryByClass("org.bukkit.Bukkit")
addDefaultImport("coreBukkit.lib.*")
addDefaultImport("org.bukkit.ChatColor.*")
generateHelper()

onEnable {
    Commands.rootProvider.set(RootCommands)
}
ListenExt//ensure init