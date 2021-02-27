@file:DependsModule("coreLibrary")
@file:MavenDepends("net.mamoe:mirai-core-jvm:2.4.0", single = false)

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.sendBlocking
import net.mamoe.mirai.BotFactory
import net.mamoe.mirai.utils.*

addDefaultImport("mirai.lib.*")
addLibraryByClass("net.mamoe.mirai.Bot")
addDefaultImport("net.mamoe.mirai.Bot")
addDefaultImport("net.mamoe.mirai.event.*")
addDefaultImport("net.mamoe.mirai.event.events.*")
addDefaultImport("net.mamoe.mirai.message.*")
addDefaultImport("net.mamoe.mirai.message.data.*")
addDefaultImport("net.mamoe.mirai.contact.*")
generateHelper()

val enable by config.key(false, "是否启动机器人(开启前先设置账号密码)")
val qq by config.key(1849301538L, "机器人qq号")
val password by config.key("123456", "机器人qq密码")
val qqProtocol by config.key(
        BotConfiguration.MiraiProtocol.ANDROID_PAD,
        "QQ登录类型，不同的类型可同时登录",
        "可用值: ANDROID_PHONE ANDROID_PAD ANDROID_WATCH"
)

val channel = Channel<String>(onBufferOverflow = BufferOverflow.DROP_LATEST)

onEnable {
    if (!enable) {
        println("机器人未开启,请先修改配置文件")
        return@onEnable
    }
    System.setProperty("mirai.slider.captcha.supported","")
    MiraiLogger.setDefaultLoggerCreator { tag ->
        @OptIn(MiraiInternalApi::class)
        object : PlatformLogger() {
            override fun info0(message: String?, e: Throwable?) {
                if (tag?.startsWith("Bot") == true)
                    super.info0(message, e)
            }

            override fun info0(message: String?) {
                if (tag?.startsWith("Bot") == true)
                    super.info0(message)
            }

            override fun debug0(message: String?) {}
            override fun debug0(message: String?, e: Throwable?) {}
            override fun verbose0(message: String?) {}
            override fun verbose0(message: String?, e: Throwable?) {}
        }
    }
    val bot = BotFactory.newBot(qq, password) {
        protocol = qqProtocol
        fileBasedDeviceInfo(Config.dataDirectory.resolve("miraiDeviceInfo.json").absolutePath)
        parentCoroutineContext = coroutineContext
        loginSolver = StandardCharImageLoginSolver(channel::receive)
    }
    launch {
        bot.login()
    }
}

Commands.controlCommand.let {
    it += CommandInfo(this, "mirai", "重定向输入到mirai") {
        usage = "[args...]"
        permission = "mirai.input"
        body {
            channel.sendBlocking(arg.joinToString(" "))
        }
    }
}