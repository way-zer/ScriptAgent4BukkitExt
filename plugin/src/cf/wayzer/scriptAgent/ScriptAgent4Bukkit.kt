package cf.wayzer.scriptAgent

import cf.wayzer.scriptAgent.ConfigExt.pluginCommand
import cf.wayzer.scriptAgent.ConfigExt.pluginMain
import cf.wayzer.scriptAgent.define.LoaderApi
import org.bukkit.plugin.java.JavaPlugin

@OptIn(LoaderApi::class)
class ScriptAgent4Bukkit : JavaPlugin() {
    init {
        ScriptAgent.load()
        Config.rootDir = dataFolder
        if (!dataFolder.exists()) dataFolder.mkdirs()
    }

    override fun onEnable() {
        Config.logger = logger
        Config.pluginMain = this
        Config.pluginCommand = getCommand("ScriptAgent")
        ScriptRegistry.scanRoot()
        ScriptManager.loadAll(true)
    }

    override fun onDisable() {
        ScriptManager.disableAll()
    }
}