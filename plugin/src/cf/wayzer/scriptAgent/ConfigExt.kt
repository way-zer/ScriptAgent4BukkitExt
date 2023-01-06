package cf.wayzer.scriptAgent

import cf.wayzer.scriptAgent.util.DSLBuilder
import org.bukkit.command.PluginCommand
import org.bukkit.plugin.java.JavaPlugin

var Config.version by DSLBuilder.dataKey<String>()
var Config.mainScript by DSLBuilder.dataKey<String>()

var Config.pluginMain by DSLBuilder.dataKey<JavaPlugin>()
var Config.pluginCommand by DSLBuilder.dataKey<PluginCommand>()