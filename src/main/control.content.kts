import org.bukkit.Color.*

name.set("ScriptAgent 控制脚本")
command("module", "ScriptAgent模块控制指令", "<reload/list> [name]", listOf("mod")) { s, arg ->
    if (!s.hasPermission("ScriptAgent.admin"))
        return@command s.sendMessage("$RED 你没有权限使用该命令")
    val list = Manager.scriptManager.loadedInitScripts
    if (arg.isNotEmpty() && arg[0].toLowerCase() == "reload") {
        val initS = list.singleOrNull() ?: list.firstOrNull { it::class.java.simpleName == arg.getOrNull(1) }
        if (initS != null) {
            if (Manager.scriptManager.reloadInit(initS) != null)
                s.sendMessage("$GREEN 重载成功")
            else
                s.sendMessage("$RED 加载失败")
            return@command
        } else s.sendMessage("$RED 错误的模块名")
    }
    s.sendMessage(
        """
        |$YELLOW====已加载模块====
        |${list.joinToString("\n") {
            "$RED%-20s $GREEN:%s".format(it::class.java.simpleName, with(it) { name.get() })
        }}
        """.trimMargin()
    )
}
command("reload", "重载ScriptAgent一个脚本", "<name> [modName]") { s, arg ->
    if (!s.hasPermission("ScriptAgent.admin"))
        return@command s.sendMessage("$RED 你没有权限使用该命令")
    val list = Manager.scriptManager.loadedInitScripts
    val initS = list.singleOrNull()
        ?: list.firstOrNull { it::class.java.simpleName == arg.getOrNull(1) }
        ?: return@command s.sendMessage("$RED 找不到模块")
    val contentS = with(initS) { children.get().firstOrNull { it::class.java.simpleName == arg[0] } }
        ?: return@command s.sendMessage("$RED 找不到脚本")
    if (Manager.scriptManager.reloadContent(initS, contentS) != null)
        s.sendMessage("$GREEN 重载成功")
    else
        s.sendMessage("$RED 加载失败")
}
command("list", "列出ScriptAgent某一模块的所有脚本", "[modName]") { s, arg ->
    if (!s.hasPermission("ScriptAgent.admin"))
        return@command s.sendMessage("$RED 你没有权限使用该命令")
    val list = Manager.scriptManager.loadedInitScripts
    val initS = list.singleOrNull()
        ?: list.firstOrNull { it::class.java.simpleName == arg.getOrNull(0) }
        ?: return@command s.sendMessage("$RED 找不到模块")
    val children = with(initS) { children.get() }
    s.sendMessage(
        """
        |$YELLOW====已加载脚本====
        |${children.joinToString("\n") {
            "$RED%-20s $GREEN:%s".format(it::class.java.simpleName, with(it) { name.get() })
        }}
        """.trimMargin()
    )
}
