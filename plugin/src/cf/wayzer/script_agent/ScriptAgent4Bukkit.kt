package cf.wayzer.script_agent

import cf.wayzer.script_agent.ConfigExt.pluginCommand
import cf.wayzer.script_agent.ConfigExt.pluginMain
import org.bukkit.plugin.java.JavaPlugin

class ScriptAgent4Bukkit : JavaPlugin() {
    init {
        ScriptAgent.load()
    }
    override fun onEnable() {
        val dir = dataFolder
        if(!dir.exists())dir.mkdirs()
        Config.logger = logger
        Config.pluginMain = this
        Config.pluginCommand = getCommand("ScriptAgent")
        ScriptManager.loadDir(dir)
    }

    override fun onDisable() {
        ScriptManager.disableAll()
    }
}