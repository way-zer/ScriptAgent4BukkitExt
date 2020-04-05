@file:Suppress("unused")

package coreBukkit.lib

import cf.wayzer.script_agent.IContentScript
import coreBukkit.lib.ContentExt.bukkitTasks
import coreBukkit.lib.ContentExt.commands
import coreBukkit.lib.ContentExt.listens
import coreBukkit.lib.ContentExt.subCommands
import cf.wayzer.script_agent.util.DSLBuilder.Companion.dataKeyWithDefault
import org.bukkit.command.Command
import org.bukkit.command.CommandException
import org.bukkit.command.CommandSender
import org.bukkit.event.Event
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.plugin.EventExecutor
import org.bukkit.scheduler.BukkitRunnable
import java.util.logging.Logger

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

    open class ScriptCommand(
        val script: IContentScript?,
        name: String,
        description: String,
        usage: String = "",
        aliases: List<String> = listOf(),
        private val executor: (s: CommandSender, arg: Array<out String>) -> Unit
    ) : Command(name, description, usage, aliases) {
        override fun execute(sender: CommandSender, commandLabel: String, args: Array<out String>): Boolean {
            try {
                executor(sender, args)
                return true
            } catch (e: Exception) {
                throw CommandException("[${script?.logger?.name}]execute Command $name fail", e)
            }
        }
    }

    val IContentScript.listener by dataKeyWithDefault { ScriptListener() }
    val IContentScript.subCommands by dataKeyWithDefault { mutableListOf<ScriptCommand>() }
    val IContentScript.commands by dataKeyWithDefault { mutableListOf<ScriptCommand>() }
    val IContentScript.listens by dataKeyWithDefault { mutableListOf<ScriptListen<*>>() }
    val IContentScript.bukkitTasks by dataKeyWithDefault { mutableListOf<BukkitRunnable>() }
}

val IContentScript.logger: Logger get() = Logger.getLogger("SA-${module?.name}-${name}")
fun IContentScript.command(
    name: String,
    description: String,
    usage: String = "",
    aliases: List<String> = listOf(),
    asSub: Boolean = true,
    executor: (s: CommandSender, arg: Array<out String>) -> Unit
) {
    val cmd = ContentExt.ScriptCommand(this, name, description, usage, aliases, executor)
    (if (asSub) subCommands else commands).add(cmd)
}

inline fun <reified T : Event> IContentScript.listen(
    ignoreCancelled: Boolean = false,
    priority: EventPriority = EventPriority.NORMAL,
    noinline block: (T) -> Unit
) {
    listens.add(ContentExt.ScriptListen(T::class.java, ignoreCancelled, priority, block))
}

fun IContentScript.createBukkitTask(autoCancel: Boolean = true, runH: BukkitRunnable.() -> Unit): BukkitRunnable {
    val runnable = object : BukkitRunnable() {
        override fun run() {
            if(autoCancel)cancel()
            runH(this)
            if (autoCancel && isCancelled)
                bukkitTasks.remove(this)
        }
    }
    if (autoCancel) bukkitTasks.add(runnable)
    return runnable
}