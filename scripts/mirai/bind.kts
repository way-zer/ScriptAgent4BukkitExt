@file:Depends("warage/whiteList", "绑定服务", soft = true)

package mirai

globalEventChannel().subscribeMessages {
    startsWith("绑定 ") { code ->
        val check = depends("warage/whiteList")?.import<(Long, String) -> String>("checkBind")
        subject.sendMessage(QuoteReply(message) + (check?.invoke(sender.id, code) ?: "绑定服务暂时不可用，请联系管理员"))
    }
}