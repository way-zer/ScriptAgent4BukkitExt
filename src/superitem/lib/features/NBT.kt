package superitem.lib.features

import cf.wayzer.script_agent.bukkit.logger
import de.tr7zw.changeme.nbtapi.NBTCompound
import de.tr7zw.changeme.nbtapi.NBTItem
import de.tr7zw.changeme.nbtapi.utils.MinecraftVersion
import org.bukkit.inventory.ItemStack
import superitem.lib.Feature
import superitem.lib.get
import java.util.logging.Level

/**
 * NBT库,可以用来操作NBT
 * 具体设置参照Wiki: <a>https://minecraft.gamepedia.com/Attribute</a>
 * @sss me.dpohvar.powernbt.api.NBTManager
 */
@Suppress("unused")
class NBT(override vararg val defaultData: AttributeModifier) : Feature<Array<out NBT.AttributeModifier>>(), Feature.OnPostLoad {
    enum class AttributeType(val attributeName: String, val max: Double) {
        MaxHealth("generic.maxHealth", 1024.0),
        FollowRange("generic.followRange", 20485.0),
        KnockbackResistance("generic.knockbackResistance", 1.0),
        MovementSpeed("generic.movementSpeed", 1024.0),
        AttackDamage("generic.attackDamage", 2048.0),
        Armor("generic.armor", 30.0),
        ArmorToughness("generic.armorToughness", 20.0),
        AttackSpeed("generic.attackSpeed", 1024.0),
        Luck("generic.luck", 1024.0),
        JumpStrength("horse.jumpStrength", 2.0),
        FlyingSpeed("generic.flyingSpeed", 1024.0),
        SpawnReinforcements("zombie.spawnReinforcements", 1.0)
    }

    enum class AttributeOperation {
        Additive,
        MultiplicativeSum,
        MultiplicativeLast
    }

    enum class UseSlot {
        MainHand, OffHand, Feet, Legs, Chest, Head
    }

    /**
     * @param type 修改的属性
     * @param amount 修改值
     * @param operation 修改方式
     * @param slot 生效的槽位(默认(null)代表所有槽位)
     */
    data class AttributeModifier(
            val type: AttributeType,
            val amount: Double,
            val operation: AttributeOperation,
            val slot: UseSlot? = null
    )

    override fun onPostLoad() {
        val nbt = NBTItem(item.get<ItemInfo>().itemStackTemplate).getCompoundList("AttributeModifiers")
        data.forEach { attr ->
            if (attr.amount < attr.type.max) {
                val node = nbt.addCompound()
                node.setString("AttributeName", attr.type.attributeName)
                node.setString("Name", "SuperItem NBT ${attr.type.name}")
                attr.slot?.let { node.setString("Slot", it.name.toLowerCase()) }
                node.setInteger("Operation", attr.operation.ordinal)
                node.setDouble("Amount", attr.amount)
                node.setInteger("UUIDLeast", 894654)
                node.setInteger("UUIDMost", 2872)
            } else {
                item.logger.log(Level.WARNING, "错误的NBT属性: $attr")
            }
        }
    }

    object API {
        init {
            MinecraftVersion.disableUpdateCheck()
            MinecraftVersion.disableBStats()
        }
        fun read(item: ItemStack): NBTCompound? = NBTItem(item).let { if (it.hasNBTData()) it else null }
        fun readOrCreate(item: ItemStack): NBTCompound = NBTItem(item)
        fun write(item: ItemStack,nbt:NBTCompound){
            item.itemMeta = (nbt as NBTItem).item.itemMeta
        }
        operator fun NBTCompound.set(key:String,v:Int) {
            setInteger(key,v)
        }
        operator fun NBTCompound.set(key:String,v:String) {
            setString(key,v)
        }
    }

    companion object {
        @Deprecated("Use API", ReplaceWith("NBT.API"))
        val api = API
    }
}
