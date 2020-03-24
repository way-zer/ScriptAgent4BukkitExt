package superitem.lib

import cf.wayzer.script_agent.util.DSLKey
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import superitem.lib.features.NBT
import kotlin.reflect.KClass

object SIManager {
    val items = mutableMapOf<String, Item>()
    val features by DSLKey.dataKeyWithDefault{mutableMapOf<KClass<*>, MutableList<Feature<*>>>()}

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
        return items[className.toUpperCase()]
    }
}
