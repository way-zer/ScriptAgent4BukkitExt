package coreBukkit.lib

import cf.wayzer.script_agent.Config
import cf.wayzer.script_agent.IContentScript
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender

object PluginCommander : CommandExecutor {
    private val subCommands = mutableMapOf<String, ContentExt.ScriptCommand>()
    override fun onCommand(
            sender: CommandSender,
            command: Command,
            label: String,
            args: Array<out String>
    ): Boolean {
        val cmd = subCommands[args.getOrNull(0)] ?: subCommands["help"]!!
        return cmd.execute(sender, label, args.sliceArray(1 until args.size))
    }

    fun addSub(name: String, cmd: ContentExt.ScriptCommand) {
        if (name in subCommands)
            Config.pluginMain.logger.warning(
                    "conflict subCommand: $name from ${cmd.script?.name}"
            )
        else subCommands[name] = cmd
    }

    fun addSub(cmd: ContentExt.ScriptCommand) {
        addSub(cmd.name, cmd)
        cmd.aliases.forEach { addSub(it, cmd) }
    }

    fun removeAll(script: IContentScript) {
        val toRemove = mutableListOf<String>()
        subCommands.forEach { (k, s) ->
            if (s.script == script) toRemove.add(k)
        }
        toRemove.forEach{ subCommands.remove(it)}
    }
    init {
        addSub(ContentExt.ScriptCommand(null, "help", "Plugin help") { s, _ ->
            val commands = subCommands.values.toSet()
            s.sendMessage("""
                    |§2§l====ScriptAgent====
                    |${commands.joinToString("\n") {
                val script = it.script?.logger?.name?.let { s1 -> "From $s1" } ?: ""
                "§6§l${it.name}§8(${it.aliases.joinToString()}) §7${it.usage}".padEnd(30) + " §5${it.description} §4$script"
            }
            }
                """.trimMargin())
        })
    }
}