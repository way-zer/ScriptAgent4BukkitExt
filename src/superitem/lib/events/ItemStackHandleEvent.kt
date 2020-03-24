package superitem.lib.events

import org.bukkit.entity.Player
import org.bukkit.event.Event
import org.bukkit.event.HandlerList
import org.bukkit.inventory.ItemStack

data class ItemStackHandleEvent (var itemStack:ItemStack,val player:Player?):Event() {
    override fun getHandlers()=staticHandlers

    companion object{
        private val staticHandlers = HandlerList()
    }
}