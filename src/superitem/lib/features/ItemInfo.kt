package superitem.lib.features

import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta
import superitem.lib.Feature
import superitem.lib.clsName
import superitem.lib.events.ItemStackHandleEvent

/**
 * 设置item的物品信息
 * @param defaultMaterial 物品的材质
 * @param defaultDamage 物品的损失或附加值
 * @param defaultName 物品的显示名
 * @param defaultLore 物品的Lore
 * @param loadOther 为物品设置其他属性
 */
class ItemInfo(
        private val defaultMaterial: Material,
        private val defaultName: String,
        private val defaultLore: List<String>,
        private val defaultDamage: Short = 0,
        private val loadOther: (ItemMeta, ItemStack) -> Unit = { _, _ -> }
) : Feature<ItemInfo.Data>(), Feature.OnPostLoad {
    interface ItemStackHandler : ((ItemStack,Player?)->Unit)
    /**
     * ItemStack初始化模板
     */
    lateinit var itemStackTemplate: ItemStack private set
    private val itemStackHandlers = mutableSetOf<ItemStackHandler>()

    override val defaultData: Data
        get() = Data(defaultMaterial, defaultDamage, defaultName, defaultLore)

    data class Data(
            val material: Material,
            val data: Short,
            val name: String,
            val lore: List<String>
    )

    /**
     * Create an ItemStack using Template and Handlers
     * @param p the player crafting
     */
    fun newItemStack(p:Player?=null):ItemStack{
        val itemStack = itemStackTemplate.clone()
        itemStackHandlers.forEach { it(itemStack,p) }
        Bukkit.getServer().pluginManager.callEvent(ItemStackHandleEvent(itemStack,p))
        itemStack.itemMeta = itemStack.itemMeta?.apply {
            setDisplayName(displayName.replace("&","§"))
            lore = lore?.map { it.replace("&","§") }
        }
        return itemStack
    }

    /**
     * Handler when an itemStack creates
     * Don't work with Recipe and Texture
     */
    fun registerHandler(handler: ItemStackHandler)=itemStackHandlers.add(handler)

    override fun onPostLoad() {
        val itemStack = ItemStack(data.material, 1)

        val im = itemStack.itemMeta!!
        im.setDisplayName(data.name)
        im.lore = data.lore
        itemStack.itemMeta = im
        loadOther(im, itemStack)
        itemStack.itemMeta = im
        val nbt = NBT.API.readOrCreate(itemStack)
        nbt.setString(NBT_TAG_NAME,item.clsName)
        NBT.API.write(itemStack, nbt)

        this.itemStackTemplate = itemStack
    }

    /**
     * 给予玩家道具
     */
    fun givePlayer(p: Player): Boolean {
        val inv = p.inventory
        val i = inv.firstEmpty()
        if (i == -1) {
            p.sendMessage("§c你身上没有足够的空位")
            return false
        }
        inv.setItem(i, inv.itemInMainHand)
        inv.setItemInMainHand(newItemStack(p))
        p.updateInventory()
        return true
    }

    fun drop(location: Location, player: Player?=null){
        location.world?.dropItem(location,newItemStack(player))
    }

    companion object {
        const val NBT_TAG_NAME="SICN"
    }
}
