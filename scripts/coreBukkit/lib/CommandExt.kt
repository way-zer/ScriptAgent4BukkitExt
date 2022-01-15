package coreBukkit.lib

import cf.wayzer.scriptAgent.define.Script
import coreLibrary.lib.CommandInfo

fun Script.command(name: String, description: String, other: CommandInfo.() -> Unit) {
    RootCommands += CommandInfo(this, name, description) {
        other()
    }
}