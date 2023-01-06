package main

if (Config.mainScript != id)
    error("仅可通过SAMain启用")

suspend fun boot(action: String) = ScriptManager.transaction {
    //add 添加需要加载的脚本(前缀判断) exclude 排除脚本(可以作为依赖被加载)
    addAll()
    exclude("main/scratch")
//    exclude("mirai")
//    exclude("superitem")

    when (action) {
        "load" -> {
            load()
        }

        "enable" -> {
            enable()
        }
    }
}

BukkitDispatcher.safeBlocking { boot("load") }
onEnable { boot("enable") }