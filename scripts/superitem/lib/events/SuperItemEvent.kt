package superitem.lib.events

import org.bukkit.event.Cancellable
import org.bukkit.event.Event
import org.bukkit.event.HandlerList

/**
 * Use for script custom event
 */
data class SuperItemEvent(val type: String, private var data: Any) : Event(), Cancellable {
    init {
        try {
            Class.forName(data::class.java.name, false, SuperItemEvent::class.java.classLoader)
        } catch (e: Exception) {
            throw Error("Can only use common Type as data", e)
        }
    }

    @Suppress("UNCHECKED_CAST")
    operator fun <T> invoke(): T {
        return data as T
    }

    private var isCancelled = false
    override fun getHandlers(): HandlerList {
        return handlerList
    }

    override fun setCancelled(cancel: Boolean) {
        isCancelled = cancel
    }

    override fun isCancelled(): Boolean {
        return isCancelled
    }

    companion object {
        val handlerList = HandlerList()
    }
}
