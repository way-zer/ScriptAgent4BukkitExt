@file:Suppress("unused")

package coreBukkit.lib

import cf.wayzer.script_agent.Config
import cf.wayzer.script_agent.IContentScript
import cf.wayzer.script_agent.getContextModule
import cf.wayzer.script_agent.listenTo
import cf.wayzer.script_agent.util.DSLBuilder
import coreLibrary.lib.*
import coreLibrary.lib.event.PermissionRequestEvent
import org.bukkit.ChatColor
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.SimpleCommandMap

object RootCommands : Commands() {
    class BukkitCommand(val info: CommandInfo) : Command(info.name, info.description, info.usage, info.aliases), CommandExecutor {
        override fun execute(sender: CommandSender, commandLabel: String, args: Array<out String>): Boolean {
            info.invoke(CommandContext().apply {
                reply = {msg->
                    "{msg}".with("msg" to msg, "player" to sender).toString().let {
                        ColorApi.handle(it,::minecraftColorHandler)
                    }.let(sender::sendMessage)
                }
                player = sender
                thisCommand = info
                prefix = commandLabel
                arg = args.toList()
            })
            return true
        }

        override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
            return execute(sender, label, args)
        }
    }

    override fun addSub(command: CommandInfo) {
        if (command.name == "help") return
        if (command.name == "ScriptAgent")
            return Config.pluginCommand.setExecutor(BukkitCommand(command))
        ModuleExt.getCommandMap().run {
            register(Config.pluginMain.description.name, BukkitCommand(command))
        }
    }

    override fun removeAll(script: IContentScript) {
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

    init {
        RootCommands::class.java.getContextModule()!!.apply {
            listenTo<PermissionRequestEvent> {
                if (context.player != null)
                    result = context.player!!.hasPermission(permission)
            }
        }
    }

    fun minecraftColorHandler(color: ColorApi.Color): String{
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

var CommandContext.player by DSLBuilder.dataKey<CommandSender>()