package cf.wayzer.script_agent

import cf.wayzer.script_agent.util.DSLBuilder
import org.bukkit.command.PluginCommand
import org.bukkit.plugin.java.JavaPlugin

object ConfigExt {
    var Config.pluginMain by DSLBuilder.dataKey<JavaPlugin>()
    var Config.pluginCommand by DSLBuilder.dataKey<PluginCommand>()
}