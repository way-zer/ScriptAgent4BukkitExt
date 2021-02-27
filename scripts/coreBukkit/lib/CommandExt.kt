package coreBukkit.lib

import cf.wayzer.scriptAgent.define.ISubScript
import coreLibrary.lib.CommandInfo

fun ISubScript.command(name: String, description: String, other: CommandInfo.() -> Unit) {
    RootCommands += CommandInfo(this, name, description) {
        other()
    }
}