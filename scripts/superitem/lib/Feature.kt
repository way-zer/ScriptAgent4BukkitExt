package superitem.lib

abstract class Feature<H : Any> {
    open fun bind(item: Item) {
        this.item = item
    }

    /**
     * 当所有Feature加载完时调用
     */
    interface OnPostLoad {
        fun onPostLoad()
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
