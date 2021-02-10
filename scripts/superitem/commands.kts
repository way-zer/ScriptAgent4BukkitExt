package superitem

import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

name = "SuperItem 命令控制脚本"

command("superitem", "SuperItem管理命令", "<list/get/give> [arg]", listOf("si")) { s, arg ->
    if (arg.isEmpty()) return@command help(s)
    when (arg[0].toLowerCase()) {
        "list" -> listItem(s, arg)
        "get" -> getItem(s, arg)
        "give" -> giveItem(s, arg)
        else -> help(s)
    }
}

fun help(s: CommandSender) {
    s.sendMessage("§c========= §c§lSuper Item§c =========")
    s.sendMessage("§a §l-------§5 By: §5§lWay__Zer §a§l-------")
    s.sendMessage("§c========== §c§l使用说明§c ==========")
    s.sendMessage("§a§l+§5/SuperItem list <页码> §e打开Item列表")
    s.sendMessage("§a§l+§5/SuperItem get <ID/ClassName> §e获取Item")
    s.sendMessage("§a§l+§5/SuperItem give <ID/ClassName> <PLAYER> §e给予玩家Item")
}

fun listItem(s: CommandSender, args: Array<out String>) {
    if (!s.hasPermission("SuperItem.command.list")) {
        s.sendMessage("§c没有权限")
        return
    }
    var pages = 1
    val list = SIManager.items.toList()
    val maxPages = (list.size - 1) / 10 + 1
    if (args.size > 1) {
        pages = args[1].toIntOrNull() ?: 1
    }
    if (pages < 1)
        pages = 1
    else if (pages > maxPages) {
        pages = maxPages
    }

    s.sendMessage("§a=========== §b已开启  Item 列表 §a============")
    var i = pages * 10 - 10
    while (i < list.size && i < pages * 10) {
        val itemName = if (!list[i].second.has<ItemInfo>()) "§c NO_ITEM"
        else list[i].second.get<ItemInfo>().newItemStack().itemMeta?.displayName
        s.sendMessage(String.format("§e%03d §a|§e %-20s §a|§e %s",
                i, list[i].first, itemName))
        i++
    }
    s.sendMessage("§a================   §7$pages/$maxPages   §a================")
}

fun getItem(s: CommandSender, args: Array<out String>) {
    if (args.size < 2) return s.sendMessage("§c请输入ID")
    if (s !is Player) return s.sendMessage("§c不能使用控制台运行")
    if (!s.hasPermission("SuperItem.command.get")) return s.sendMessage("§c没有权限")
    val item = getItemByNameOrID(args[1]) ?: return s.sendMessage("§c请输入正确的ID")
    if (!item.has<ItemInfo>()) return s.sendMessage("§c没有实体物品可以获取")
    if (item.get<ItemInfo>().givePlayer(s))
        s.sendMessage("§a获取成功")
}

fun giveItem(s: CommandSender, args: Array<out String>) {
    if (args.size < 3) return s.sendMessage("§c请输入参数")
    if (!s.hasPermission("SuperItem.command.give")) return s.sendMessage("§c没有权限")
    val player = Bukkit.getPlayer(args[2]) ?: return s.sendMessage("§c找不到玩家")
    val item = getItemByNameOrID(args[1]) ?: return s.sendMessage("§c请输入正确的ID")
    if (!item.has<ItemInfo>()) return s.sendMessage("§c没有实体物品可以获取")
    if (item.get<ItemInfo>().givePlayer(player))
        s.sendMessage("§a给予成功")
    else {
        item.get<ItemInfo>().drop(player.location, player)
        player.sendMessage("§a背包已满，已掉落")
        s.sendMessage("§e背包已满，已掉落")
    }
}

fun getItemByNameOrID(str: String): Item? {
    return SIManager.getItem(str) ?: str.toIntOrNull()?.let {
        val list = SIManager.items.toList()
        if (it in list.indices) list[it].second
        else null
    }
}