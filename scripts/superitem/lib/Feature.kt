package superitem.lib

import org.bukkit.event.Listener

abstract class Feature<H> where H : Any {
    open fun bind(item: Item) {
        this.item = item
    }

    /**
     * 当此Feature加载完时调用
     */
    interface OnPostLoad {
        fun onPostLoad()
    }

    /**
     * 当插件关闭时调用
     */
    interface OnDisable {
        fun onDisable()
    }

    @Deprecated("请直接使用Item的Listen函数")
    interface HasListener {
        val listener: Listener
    }

    /**
     * 绑定的Item
     */
    protected lateinit var item: Item
    abstract val defaultData: H?

    /**
     * Feature的配置信息
     */
    lateinit var data: H
    val name: String
        get() = javaClass.simpleName
}
