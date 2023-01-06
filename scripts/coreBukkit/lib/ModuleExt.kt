package coreBukkit.lib

import cf.wayzer.scriptAgent.Config
import cf.wayzer.scriptAgent.define.Script
import cf.wayzer.scriptAgent.util.DSLBuilder
import coreBukkit.lib.ModuleExt.registerCls
import coreBukkit.lib.ModuleExt.unregisterCls
import org.bukkit.Bukkit
import org.bukkit.command.CommandMap
import org.bukkit.command.PluginCommand
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.plugin.java.JavaPluginLoader

object ModuleExt{
    internal fun registerCls(cls: Class<*>) {
        val m = JavaPluginLoader::class.java.getDeclaredMethod("setClass", String::class.java, Class::class.java)
        m.isAccessible = true
        m.invoke(Config.pluginMain.pluginLoader, cls.name, cls)
    }
    internal fun unregisterCls(cls: Class<*>) {
        val m = JavaPluginLoader::class.java.getDeclaredMethod("removeClass", String::class.java)
        m.isAccessible = true
        m.invoke(Config.pluginMain.pluginLoader, cls.name)
    }
    fun getCommandMap(): CommandMap {
        val server = Bukkit.getServer()
        return server::class.java.getMethod("getCommandMap").invoke(server) as CommandMap
    }
}
val Config.pluginMain by DSLBuilder.dataKeyWithDefault<JavaPlugin>{ error("pluginMain can't be null") }
val Config.pluginCommand by DSLBuilder.dataKeyWithDefault<PluginCommand>{ error("pluginCommand can't be null") }
fun Script.exportClass(clazz: Class<*>){
    onEnable(1){
        registerCls(clazz)
    }
    onDisable{
        unregisterCls(clazz)
    }
}