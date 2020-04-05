package cf.wayzer.script_agent

import cf.wayzer.script_agent.ConfigExt.pluginCommand
import cf.wayzer.script_agent.ConfigExt.pluginMain
import cf.wayzer.script_agent.ConfigExt.scriptManager
import org.bukkit.plugin.java.JavaPlugin

class ScriptAgent4Bukkit : JavaPlugin() {
    init {
        ScriptAgent.load()
    }
    override fun onEnable() {
        val dir = dataFolder
        if(!dir.exists())dir.mkdirs()
        Config.pluginMain = this
        Config.pluginCommand = getCommand("ScriptAgent")
        Config.scriptManager.loadDir(dir)
    }

    override fun onDisable() {
        Config.scriptManager.disableAll()
    }
}