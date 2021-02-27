package coreBukkit.lib

import cf.wayzer.scriptAgent.Config
import cf.wayzer.scriptAgent.define.ISubScript
import cf.wayzer.scriptAgent.events.ScriptDisableEvent
import cf.wayzer.scriptAgent.events.ScriptEnableEvent
import cf.wayzer.scriptAgent.getContextScript
import cf.wayzer.scriptAgent.listenTo
import cf.wayzer.scriptAgent.util.DSLBuilder
import coreBukkit.lib.ListenExt.listens
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.bukkit.Bukkit
import org.bukkit.event.Event
import org.bukkit.event.EventPriority
import org.bukkit.event.HandlerList
import org.bukkit.event.Listener
import org.bukkit.plugin.EventExecutor

object ListenExt {
    class ScriptListener : Listener
    data class MyListener<T : Event>(
            val script: ISubScript?, val cls: Class<T>,
            val ignoreCancelled: Boolean, val priority: EventPriority, val handler: (T) -> Unit
    ) : EventExecutor {
        override fun execute(listener: Listener, e: Event) {
            if (cls.isAssignableFrom(e::class.java)) {
                handler(cls.cast(e))
            }
        }
    }

    private val ISubScript.listener by DSLBuilder.dataKeyWithDefault { ScriptListener() }
    private val key = DSLBuilder.DataKeyWithDefault("listens") { mutableListOf<MyListener<*>>() }
    val ISubScript.listens by key

    init {
        ListenExt::class.java.getContextScript().apply {
            listenTo<ScriptEnableEvent>(4) { //after
                key.run { script.get() }?.forEach {
                    Bukkit.getPluginManager().registerEvent(it.cls, script.listener, it.priority, it, Config.pluginMain, it.ignoreCancelled)
                }
            }
            listenTo<ScriptDisableEvent>(2) { //before
                HandlerList.unregisterAll(script.listener)
            }
        }
    }
}

inline fun <reified T : Event> ISubScript.listen(
        ignoreCancelled: Boolean = false, priority: EventPriority = EventPriority.NORMAL,
        noinline block: (T) -> Unit
) {
    listens.add(ListenExt.MyListener(this, T::class.java, ignoreCancelled, priority, block))
}