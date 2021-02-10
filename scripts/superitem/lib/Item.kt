package superitem.lib

import cf.wayzer.script_agent.ISubScript
import cf.wayzer.script_agent.util.DSLBuilder
import org.bukkit.inventory.ItemStack
import kotlin.reflect.KClass

typealias Item = ISubScript

fun <T : Feature<out Any>> Item.require(feature: T): T {
    feature.bind(this)
    features.getOrPut(feature::class, ::mutableListOf).add(feature)
    return feature
}

val Item.features by DSLBuilder.dataKeyWithDefault { mutableMapOf<KClass<*>, MutableList<Feature<*>>>() }

//判断是否有对应feature
inline fun <reified T : Feature<*>> Item.has() = !features[T::class].isNullOrEmpty()

/**
 * 安全的获取指定类型的feature
 * @param index feature序数(如果有多个,从0开始)
 * @exception RuntimeException 如果不存在指定类型的feature
 */
inline fun <reified T : Feature<*>> Item.get(index: Int = 0): T = features[T::class]?.getOrNull(index) as? T
        ?: throw RuntimeException("[$clsName] Can't find ${T::class.simpleName}[$index], may you forget require it")

// 判断物品是否是当前Item的道具
fun Item.isItem(itemStack: ItemStack?) = SIManager.getItem(itemStack)?.equals(this) ?: false