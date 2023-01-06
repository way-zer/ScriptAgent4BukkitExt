package superitem.lib

import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import superitem.lib.features.NBT

object SIManager {
    val items = mutableMapOf<String, Item>()

    /**
     * 通过 ItemStack 获取 Item
     * 没有对应Item返回 null
     */
    fun getItem(itemStack: ItemStack?): Item? {
        if (itemStack == null || itemStack.type == Material.AIR)
            return null
        return NBT.API.read(itemStack)?.let { getItem(it.getString("SICN")) }
    }

    /**
     * 通过 Item.class.simpleName 查询Item
     */
    fun getItem(className: String): Item? {
        return items[className.uppercase()]
    }
}
