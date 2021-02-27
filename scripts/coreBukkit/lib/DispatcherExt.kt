package coreBukkit.lib

import cf.wayzer.scriptAgent.Config
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import org.bukkit.Bukkit
import kotlin.coroutines.CoroutineContext

object MindustryDispatcher : CoroutineDispatcher() {
    override fun dispatch(context: CoroutineContext, block: Runnable) {
        if(Bukkit.isPrimaryThread())
            block.run()
        else
            Bukkit.getScheduler().runTask(Config.pluginMain,block)
    }
}

@Suppress("unused")
val Dispatchers.game
    get() = MindustryDispatcher
@Suppress("unused")
@Deprecated("use Dispatchers.game for better universal", ReplaceWith("Dispatchers.game"))
val Dispatchers.bukkit
    get() = MindustryDispatcher