package coreBukkit

import cf.wayzer.script_agent.Config

name = "ScriptAgent 控制脚本"
command("module", "ScriptAgent模块控制指令", "<reload/list>", listOf("mod")) { s, arg ->
    if (!s.hasPermission("ScriptAgent.admin"))
        return@command s.sendMessage("$RED 你没有权限使用该命令")
    val list = Config.inst.loadedInitScripts
    when (arg.getOrNull(0)?.toLowerCase()) {
        "reload" -> {
            val initS = list.values.singleOrNull() ?: list[arg.getOrNull(1)?.toLowerCase()?:""]
            ?: return@command s.sendMessage("$RED 错误的模块名")
            if (Config.inst.reloadInit(initS) != null)
                s.sendMessage("$GREEN 重载成功")
            else
                s.sendMessage("$RED 加载失败")
        }
        else -> {
            s.sendMessage("""
                |$YELLOW  ====已加载模块====
                |${list.values.joinToString("\n") { "$RED%-20s $GREEN:%s".format(it.clsName, it.name) }}
            """.trimMargin())
        }
    }
}
command("reload", "重载ScriptAgent一个脚本", "<name> [modName]") { s, arg ->
    if (!s.hasPermission("ScriptAgent.admin"))
        return@command s.sendMessage("$RED 你没有权限使用该命令")
    val list = Config.inst.loadedInitScripts
    val initS = list.values.singleOrNull() ?:list[arg.getOrNull(1)?.toLowerCase()]
            ?: return@command s.sendMessage("$RED 找不到模块")
    val contentS = with(initS) { children.firstOrNull { it.clsName.equals(arg.getOrNull(0), true) } }
            ?: return@command s.sendMessage("$RED 找不到脚本")
    if (Config.inst.reloadContent(initS, contentS) != null)
        s.sendMessage("$GREEN 重载成功")
    else
        s.sendMessage("$RED 加载失败")
}
command("list", "列出ScriptAgent某一模块的所有脚本", "[modName]") { s, arg ->
    if (!s.hasPermission("ScriptAgent.admin"))
        return@command s.sendMessage("$RED 你没有权限使用该命令")
    val list = Config.inst.loadedInitScripts
    val initS = list.values.singleOrNull() ?: list[arg.getOrNull(0)?.toLowerCase()]
            ?: return@command s.sendMessage("$RED 找不到模块")
    s.sendMessage(
            """
        |$YELLOW====已加载脚本====
        |${initS.children.joinToString("\n") { "$RED%-20s $GREEN:%s".format(it.clsName, it.name) }}
        """.trimMargin()
    )
}
command("load", "新加载单个文件", "<filename>") { s, arg ->
    if (!s.hasPermission("ScriptAgent.admin"))
        return@command s.sendMessage("$RED 你没有权限使用该命令")
    if (arg.isEmpty()) return@command s.sendMessage("$RED 请输入文件名")
    val file = Config.rootDir.resolve(arg[0])
    if (!file.exists() || !file.isFile) return@command s.sendMessage("$RED 未找到对应文件")
    val success = if (file.name.endsWith(Config.moduleDefineSuffix)) {
        Config.inst.loadModule(file) != null
    } else {
        val list = Config.inst.loadedInitScripts
        val initS = list[arg[0].split("/")[0].toLowerCase()] ?: return@command s.sendMessage("$RED 找不到模块,请确定模块已先加载")
        Config.inst.loadContent(initS, file) != null
    }
    if (success)
        s.sendMessage("$GREEN 加载成功")
    else
        s.sendMessage("$RED 加载失败")
}