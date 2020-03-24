name.set("ScriptAgent 控制脚本")
command("module", "ScriptAgent模块控制指令", "<reload/list>", listOf("mod")) { s, arg ->
    if (!s.hasPermission("ScriptAgent.admin"))
        return@command s.sendMessage("$RED 你没有权限使用该命令")
    val list = Manager.scriptManager.loadedInitScripts
    when (arg.getOrNull(0)?.toLowerCase()) {
        "reload" -> {
            val initS = list.singleOrNull() ?: list.firstOrNull { it::class.java.simpleName.removeSuffix("_init").equals(arg.getOrNull(1),true)}
            ?: return@command s.sendMessage("$RED 错误的模块名")
            if (Manager.scriptManager.reloadInit(initS) != null)
                s.sendMessage("$GREEN 重载成功")
            else
                s.sendMessage("$RED 加载失败")
        }
        else -> {
            s.sendMessage("""
                |$YELLOW  ====已加载模块====
                |${list.joinToString("\n") {
                "$RED%-20s $GREEN:%s".format(it::class.java.simpleName.removeSuffix("_init"), with(it) { name.get() })
            }}
            """.trimMargin())
        }
    }
}
command("reload", "重载ScriptAgent一个脚本", "<name> [modName]") { s, arg ->
    if (!s.hasPermission("ScriptAgent.admin"))
        return@command s.sendMessage("$RED 你没有权限使用该命令")
    val list = Manager.scriptManager.loadedInitScripts
    val initS = list.singleOrNull()
            ?: list.firstOrNull { it::class.java.simpleName.removeSuffix("_init").equals(arg.getOrNull(1),true) }
            ?: return@command s.sendMessage("$RED 找不到模块")
    val contentS = with(initS) { children.get().firstOrNull { it::class.java.simpleName.removeSuffix("_content").equals(arg.getOrNull(0),true) } }
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
            ?: list.firstOrNull { it::class.java.simpleName.removeSuffix("_init").equals(arg.getOrNull(0),true)}
            ?: return@command s.sendMessage("$RED 找不到模块")
    val children = with(initS) { children.get() }
    s.sendMessage(
            """
        |$YELLOW====已加载脚本====
        |${children.joinToString("\n") {
                "$RED%-20s $GREEN:%s".format(it::class.java.simpleName.removeSuffix("_content"), with(it) { name.get() })
            }}
        """.trimMargin()
    )
}
command("load", "新加载单个文件", "<filename>") { s, arg ->
    if (!s.hasPermission("ScriptAgent.admin"))
        return@command s.sendMessage("$RED 你没有权限使用该命令")
    if (arg.isEmpty()) return@command s.sendMessage("$RED 请输入文件名")
    val file = Manager.scriptManager.rootDir.resolve(arg[0])
    if (!file.exists() || !file.isFile) return@command s.sendMessage("$RED 未找到对应文件")
    val success = if (file.name.endsWith(".init.kts")) {
        Manager.scriptManager.loadModule(file) != null
    } else {
        val list = Manager.scriptManager.loadedInitScripts
        val initS = list.firstOrNull { it::class.java.simpleName.removeSuffix("_init").equals(arg[0].split("/")[0],true)}
                ?: return@command s.sendMessage("$RED 找不到模块,请确定模块已先加载")
        Manager.scriptManager.loadContent(initS, file) != null
    }
    if (success)
        s.sendMessage("$GREEN 加载成功")
    else
        s.sendMessage("$RED 加载失败")
}