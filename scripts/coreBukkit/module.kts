@file:Import("org.bukkit.Bukkit", libraryByClassLoader = true)
@file:Import("coreBukkit.lib.*", defaultImport = true)
@file:Import("org.bukkit.ChatColor.*", defaultImport = true)
@file:Depends("coreLibrary")

package coreBukkit

name = "Bukkit 核心脚本模块"

onEnable {
    Commands.rootProvider.provide(this, RootCommands)
}
ListenExt//ensure init