package coreBukkit.lib

import cf.wayzer.scriptAgent.Config
import kotlinx.coroutines.*
import org.bukkit.Bukkit
import java.lang.Runnable
import java.util.concurrent.ConcurrentLinkedQueue
import kotlin.coroutines.CoroutineContext

object BukkitDispatcher : CoroutineDispatcher() {
    @Volatile
    private var inBlocking = false
    private var blockingQueue = ConcurrentLinkedQueue<Runnable>()

    override fun isDispatchNeeded(context: CoroutineContext): Boolean {
        return !Bukkit.isPrimaryThread()
    }

    override fun dispatch(context: CoroutineContext, block: Runnable) {
        if (inBlocking) {
            blockingQueue.add(block)
            return
        }
        Bukkit.getScheduler().runTask(Config.pluginMain, block)
    }

    @OptIn(InternalCoroutinesApi::class)
    override fun dispatchYield(context: CoroutineContext, block: Runnable) {
        Bukkit.getScheduler().runTask(Config.pluginMain, block)
    }

    fun <T> safeBlocking(block: suspend CoroutineScope.() -> T): T {
        return if (inBlocking) runBlocking(Dispatchers.game, block)
        else runBlocking {
            inBlocking = true
            launch {
                while (inBlocking || blockingQueue.isNotEmpty()) {
                    blockingQueue.poll()?.run() ?: yield()
                }
            }
            withContext(Dispatchers.game, block).also {
                inBlocking = false
            }
        }
    }
}

@Suppress("unused")
val Dispatchers.game
    get() = BukkitDispatcher

@Suppress("unused")
@Deprecated("use Dispatchers.game for better universal", ReplaceWith("Dispatchers.game"))
val Dispatchers.bukkit
    get() = BukkitDispatcher