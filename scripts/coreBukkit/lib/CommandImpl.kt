@file:Suppress("unused")

package coreBukkit.lib

import cf.wayzer.scriptAgent.Config
import cf.wayzer.scriptAgent.define.Script
import cf.wayzer.scriptAgent.util.DSLBuilder
import coreLibrary.lib.*
import org.bukkit.ChatColor
import org.bukkit.command.*
import org.bukkit.command.TabCompleter
import org.bukkit.entity.Player

object RootCommands : Commands() {
    class BukkitCommand(val info: CommandInfo) : Command(info.name, info.description, info.usage, info.aliases),
        CommandExecutor, TabCompleter {
        override fun execute(sender: CommandSender, commandLabel: String, args: Array<out String>): Boolean {
            info.invoke(CommandContext().apply {
                reply = { msg ->
                    "{msg}".with("msg" to msg, "player" to sender).toString().let {
                        ColorApi.handle(it, ::minecraftColorHandler)
                    }.let(sender::sendMessage)
                }
                this.sender = sender
                hasPermission = { sender.hasPermission(it) }
                prefix = "/$commandLabel "
                arg = args.toList()
            })
            return true
        }

        override fun tabComplete(sender: CommandSender, alias: String, args: Array<out String>): List<String> {
            var result: List<String> = emptyList()

            try {
                info.onComplete(CommandContext().apply {
                    this.sender = sender
                    hasPermission = { sender.hasPermission(it) }
                    replyTabComplete = { result = it;CommandInfo.Return() }
                    prefix = "/$alias "
                    arg = args.toList()
                })
            } catch (_: CommandInfo.Return) {
            }
            return result
        }

        override fun onCommand(
            sender: CommandSender,
            command: Command,
            label: String,
            args: Array<out String>
        ): Boolean {
            return execute(sender, label, args)
        }

        override fun onTabComplete(
            sender: CommandSender,
            command: Command,
            alias: String,
            args: Array<out String>
        ): List<String> {
            return tabComplete(sender, alias, args)
        }
    }

    override fun addSub(name: String, command: CommandInfo, isAliases: Boolean) {
        if (name == "help" || isAliases) return
        if (name == "ScriptAgent")
            return Config.pluginCommand.run {
                BukkitCommand(command).let {
                    setExecutor(it)
                    tabCompleter = it
                }
            }
        ModuleExt.getCommandMap().run {
            register(Config.pluginMain.description.name, BukkitCommand(command))
        }
    }

    override fun removeAll(script: Script) {
        ModuleExt.getCommandMap().run {
            val knownCommands = SimpleCommandMap::class.java.getDeclaredField("knownCommands")
                .apply { isAccessible = true }.get(this) as MutableMap<*, *>
            val toRemove = mutableListOf<Any?>()
            knownCommands.forEach { (k, v) ->
                if (v is BukkitCommand && v.info.script == script) {
                    toRemove.add(k)
                }
            }
            toRemove.forEach { knownCommands.remove(it) }
        }
    }

    fun minecraftColorHandler(color: ColorApi.Color): String {
        return when (color) {
            ConsoleColor.RESET -> ChatColor.RESET
            ConsoleColor.BOLD -> ChatColor.BOLD
            ConsoleColor.ITALIC -> ChatColor.ITALIC
            ConsoleColor.UNDERLINED -> ChatColor.UNDERLINE
            ConsoleColor.BLACK -> ChatColor.BLACK
            ConsoleColor.RED -> ChatColor.DARK_RED
            ConsoleColor.GREEN -> ChatColor.DARK_GREEN
            ConsoleColor.YELLOW -> ChatColor.YELLOW
            ConsoleColor.BLUE -> ChatColor.DARK_BLUE
            ConsoleColor.PURPLE -> ChatColor.DARK_PURPLE
            ConsoleColor.CYAN -> ChatColor.DARK_AQUA
            ConsoleColor.LIGHT_RED -> ChatColor.RED
            ConsoleColor.LIGHT_GREEN -> ChatColor.GREEN
            ConsoleColor.LIGHT_YELLOW -> ChatColor.YELLOW
            ConsoleColor.LIGHT_BLUE -> ChatColor.BLUE
            ConsoleColor.LIGHT_PURPLE -> ChatColor.LIGHT_PURPLE
            ConsoleColor.LIGHT_CYAN -> ChatColor.AQUA
            ConsoleColor.WHITE -> ChatColor.WHITE
            else -> return ""
        }.toString()
    }
}

var CommandContext.sender by DSLBuilder.dataKey<CommandSender>()
val CommandContext.player get() = sender as? Player