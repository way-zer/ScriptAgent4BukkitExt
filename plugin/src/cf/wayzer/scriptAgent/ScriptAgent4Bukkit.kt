package cf.wayzer.scriptAgent

import cf.wayzer.scriptAgent.ConfigExt.pluginCommand
import cf.wayzer.scriptAgent.ConfigExt.pluginMain
import cf.wayzer.scriptAgent.define.LoaderApi
import org.bukkit.plugin.java.JavaPlugin

@OptIn(LoaderApi::class)
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