package warage

import com.google.common.cache.CacheBuilder
import org.bukkit.event.player.AsyncPlayerPreLoginEvent
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.random.Random
import warage.lib.db.WhiteList as Table

name = "白名单绑定"

listen<AsyncPlayerPreLoginEvent> {
    if (Table.check(it.uniqueId, it.name, it.address.toString())) return@listen
    val code = getCode(it.uniqueId).toString().padStart(6, '0')
    it.disallow(AsyncPlayerPreLoginEvent.Result.KICK_WHITELIST, """
            §3欢迎来到§c§bWarAge§3服务器
            §a本服务器已经开启§c白名单§a验证
            §a请前往WARAGE官方群 §b§e163795070
            §a输入"§2§b绑定 $code§a"进行绑定
        """.trimIndent())
    logger.info("拦截玩家${it.name},绑定码${code}")
}

val usedCode = mutableMapOf<Int, UUID>()
val map = CacheBuilder.newBuilder()
        .expireAfterWrite(10, TimeUnit.MINUTES)
        .maximumSize(1000)
        .removalListener<UUID, Int> { usedCode.remove(it.value) }
        .build<UUID, Int>()!!


fun getCode(uuid: UUID): Int {
    return map.get(uuid) {
        var code: Int
        do {
            if (!enabled) throw InterruptedException()
            code = Random.nextInt(1_000_000)
        } while (code in usedCode)
        usedCode[code] = uuid
        code
    }
}

fun checkBind(qq: Long, code: String): String {
    if (!Regex("[0-9]{6}").matches(code)) return "错误的绑定码格式"
    val uuid = usedCode[code.toInt()] ?: return "找不到绑定码, 绑定码10分钟有效，若过期，请重新进服获取"
    if (Table.countByQQ(qq) > 0) return "一个qq限制绑定一个账号，如有问题，请联系管理员"
    Table.register(uuid, qq)
    return "绑定成功"
}
export(::checkBind)

command("bindList", "qq绑定白名单") {
    permission = id.replace('/', '.')
    usage = "delete <qq> // bind <qq> <code>"
    body {
        val qq = arg.getOrNull(1)?.toLongOrNull() ?: replyUsage()
        when (arg.getOrNull(0)) {
            "delete" -> {
                transaction {
                    val found = Table.find { Table.T.qq eq qq }.firstOrNull() ?: returnReply("该qq未绑定账号".with())
                    found.delete()
                    reply("已移除绑定{qq}:{uuid}".with("qq" to qq, "uuid" to found.uuid))
                }
            }
            "bind" -> {
                val code = arg.getOrNull(2)?.toIntOrNull() ?: replyUsage()
                val uuid = usedCode[code] ?: returnReply("绑定码不存在或已失效".with())
                transaction {
                    Table.register(uuid, qq)
                    reply("绑定成功{qq}:{uuid}".with("qq" to qq, "uuid" to uuid))
                }
            }
            else -> replyUsage()
        }
    }
}