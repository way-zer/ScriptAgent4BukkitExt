@file:Suppress("unused")

package coreBukkit.lib

import cf.wayzer.script_agent.Config
import cf.wayzer.script_agent.IContentScript
import cf.wayzer.script_agent.util.DSLBuilder.Companion.dataKeyWithDefault
import coreBukkit.lib.ContentExt.bukkitTasks
import coreBukkit.lib.ContentExt.commands
import coreBukkit.lib.ContentExt.listens
import coreBukkit.lib.ContentExt.subCommands
import coreLibrary.lib.CommandContext
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

    val IContentScript.listener by dataKeyWithDefault { ScriptListener() }
    val IContentScript.subCommands by dataKeyWithDefault { mutableListOf<CommandInfo>() }
    val IContentScript.commands by dataKeyWithDefault { mutableListOf<CommandInfo>() }
    val IContentScript.listens by dataKeyWithDefault { mutableListOf<ScriptListen<*>>() }
    val IContentScript.bukkitTasks by dataKeyWithDefault { mutableListOf<BukkitRunnable>() }
}

val IContentScript.logger: Logger get() = Logger.getLogger("SA-$id")
@Deprecated("use CommandInfo instead")
fun IContentScript.command(
        name: String,
        description: String,
        usage: String = "",
        aliases: List<String> = listOf(),
        asSub: Boolean = true,
        executor: (s: CommandSender, arg: Array<out String>) -> Unit
) {
    val cmd = CommandInfo(this, name, description, {
        this.usage = usage
        this.aliases = aliases
    }) {
        if (sender != null)
            executor(sender!!, arg.toTypedArray())
    }
    (if (asSub) subCommands else commands).add(cmd)
}

fun IContentScript.command(name:String,description: String,other:CommandInfo.()->Unit, asSub: Boolean = true,executor: CommandContext.()->Unit){
    (if (asSub) subCommands else commands).add(CommandInfo(this,name,description,other,executor))
}

inline fun <reified T : Event> IContentScript.listen(
        ignoreCancelled: Boolean = false,
        priority: EventPriority = EventPriority.NORMAL,
        noinline block: (T) -> Unit
) {
    listens.add(ContentExt.ScriptListen(T::class.java, ignoreCancelled, priority, block))
}

val Dispatchers.bukkit get() = ContentExt.BukkitSyncDispatcher

@Deprecated("Recommend use launch(Dispatcher.bukkit)")
fun IContentScript.createBukkitTask(autoCancel: Boolean = true, runH: BukkitRunnable.() -> Unit): BukkitRunnable {
    val runnable = object : BukkitRunnable() {
        override fun run() {
            if (autoCancel) cancel()
            runH(this)
            if (autoCancel && isCancelled)
                bukkitTasks.remove(this)
        }
    }
    if (autoCancel) bukkitTasks.add(runnable)
    return runnable
}