package superitem.lib

import cf.wayzer.scriptAgent.define.Script
import cf.wayzer.scriptAgent.util.DSLBuilder
import org.bukkit.inventory.ItemStack
import kotlin.reflect.KClass

typealias Item = Script

val Script.isItem get() = featuresKey.run { get() != null }

fun <T : Feature<out Any>> Item.require(feature: T): T {
    feature.bind(this)
    features.getOrPut(feature::class, ::mutableListOf).add(feature)
    return feature
}

private val featuresKey =
    DSLBuilder.DataKeyWithDefault("features") { mutableMapOf<KClass<*>, MutableList<Feature<*>>>() }
val Item.features by featuresKey

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