@file:Suppress("unused")

package coreBukkit.lib

import cf.wayzer.scriptAgent.Config
import cf.wayzer.scriptAgent.define.ISubScript
import cf.wayzer.scriptAgent.util.DSLBuilder.Companion.dataKeyWithDefault
import coreBukkit.lib.ContentExt.bukkitTasks
import coreBukkit.lib.ContentExt.commands
import coreBukkit.lib.ContentExt.listens
import coreBukkit.lib.ContentExt.subCommands
import coreLibrary.lib.CommandInfo
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Runnable
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.event.Event
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.plugin.EventExecutor
import org.bukkit.scheduler.BukkitRunnable
import java.util.logging.Logger
import kotlin.coroutines.CoroutineContext

object ContentExt {
    class ScriptListener : Listener
    data class ScriptListen<T : Event>(
            val cls: Class<T>, val ignoreCancelled: Boolean = false,
            val priority: EventPriority = EventPriority.NORMAL, private val block: (T) -> Unit
    ) : EventExecutor {
        override fun execute(listener: Listener, e: Event) {
            if (cls.isAssignableFrom(e::class.java)) {
                block(cls.cast(e))
            }
        }
    }
    object BukkitSyncDispatcher:CoroutineDispatcher(){
        override fun dispatch(context: CoroutineContext, block: Runnable) {
            if(Bukkit.isPrimaryThread())
                block.run()
            else
                Bukkit.getScheduler().runTask(Config.pluginMain,block)
        }
    }

    val ISubScript.listener by dataKeyWithDefault { ScriptListener() }
    val ISubScript.subCommands by dataKeyWithDefault { mutableListOf<CommandInfo>() }
    val ISubScript.commands by dataKeyWithDefault { mutableListOf<CommandInfo>() }
    val ISubScript.listens by dataKeyWithDefault { mutableListOf<ScriptListen<*>>() }
    val ISubScript.bukkitTasks by dataKeyWithDefault { mutableListOf<BukkitRunnable>() }
}

@Deprecated("use CommandInfo instead")
fun ISubScript.command(
        name: String,
        description: String,
        usage: String,
        aliases: List<String> = listOf(),
        asSub: Boolean = true,
        executor: (s: CommandSender, arg: Array<out String>) -> Unit
) {
    val cmd = CommandInfo(this, name, description) {
        this.usage = usage
        this.aliases = aliases
        body {
            if (sender != null)
                executor(sender!!, arg.toTypedArray())
        }
    }
    (if (asSub) subCommands else commands).add(cmd)
}

fun ISubScript.command(name:String,description: String, asSub: Boolean = true,other:CommandInfo.()->Unit){
    (if (asSub) subCommands else commands).add(CommandInfo(this,name,description,other))
}

inline fun <reified T : Event> ISubScript.listen(
        ignoreCancelled: Boolean = false,
        priority: EventPriority = EventPriority.NORMAL,
        noinline block: (T) -> Unit
) {
    listens.add(ContentExt.ScriptListen(T::class.java, ignoreCancelled, priority, block))
}

val Dispatchers.bukkit get() = ContentExt.BukkitSyncDispatcher

@Deprecated("Recommend use launch(Dispatcher.bukkit)")
fun ISubScript.createBukkitTask(autoCancel: Boolean = true, runH: BukkitRunnable.() -> Unit): BukkitRunnable {
    val runnable = object : BukkitRunnable() {
        override fun run() {
            if (autoCancel) cancel()
            runH(this)
            if (autoCancel && !enabled)
                bukkitTasks.remove(this)
        }
    }
    if (autoCancel) bukkitTasks.add(runnable)
    return runnable
}