package superitem.lib.features

import cf.wayzer.scriptAgent.Config
import coreBukkit.lib.listen
import coreBukkit.lib.pluginMain
import net.md_5.bungee.api.ChatMessageType
import net.md_5.bungee.api.chat.TextComponent
import org.bukkit.Bukkit
import org.bukkit.block.Block
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.event.Cancellable
import org.bukkit.event.Event
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.inventory.CraftItemEvent
import superitem.lib.Feature
import superitem.lib.Item
import superitem.lib.isItem

/**
 * **自动Require
 * 为物品绑定权限
 * @param default 物品的权限(默认superitem.name)
 */
class Permission(private val default: String? = null) : Feature<String>(), Listener {
    override val defaultData by lazy { default ?: "superitem.${item.clsName}" }

    /**
     * 判断玩家是否有权限
     * @param tip 是否发送提示
     */
    fun hasPermission(p: Player, tip: Boolean = true): Boolean {
        return when {
            p.hasPermission(data) -> true
            else -> {
                if (tip)
                    p.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent("§c你没有权限使用"))
                false
            }
        }
    }

    override fun bind(item: Item) {
        super.bind(item)
        item.listen<CraftItemEvent>(true) { e ->
            if (item.isItem(e.recipe.result) && !hasPermission(e.whoClicked as Player, false)) {
                e.whoClicked.sendMessage("§c你没有权限合成这个物品")
                e.isCancelled = true
            }
        }
        item.listen<BlockPlaceEvent>(true) {
            if (item.isItem(it.itemInHand)) it.isCancelled = true
        }
    }

    companion object {
        /**
         * 检测事件权限
         * 影响他人或环境的建议调用
         * @param event 需要检测的事件
         * @return true 表示操作可以进行
         * @sample checkEvent(BlockBreakEvent(block,player)) 判断是否能删除方块
         * @sample checkEvent(EntityDamageByEntityEvent(player,target,ENTITY_ATTACK,1)) 判断是否能否攻击玩家
         */
        fun checkEvent(event: Event): Boolean {
            return if (event is Cancellable) {
                Bukkit.getServer().pluginManager.callEvent(event)
                !event.isCancelled
            } else {
                Config.pluginMain.logger.warning("Event ${event.eventName} is not Cancellable,don't need to check")
                true
            }
        }

        /**
         * 检测某人能否破坏方块
         * @see checkEvent
         */
        fun Player.canBreakBlock(block: Block): Boolean {
            return checkEvent(BlockBreakEvent(block, this))
        }

        /**
         * 检测某人能否伤害生物(包括人)
         * @see checkEvent
         */
        fun Player.canDamage(entity: Entity): Boolean {
            @Suppress("DEPRECATION")
            val event = EntityDamageByEntityEvent(this, entity, EntityDamageEvent.DamageCause.ENTITY_ATTACK, 2.0)
            return checkEvent(event) && event.damage >= 1.0
        }
    }
}