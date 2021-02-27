@file:ImportByClass("lk.vexview.api.VexViewAPI")
@file:ImportByClass("net.milkbowl.vault.economy.Economy")
@file:ImportByClass("com.bekvon.bukkit.residence.Residence")

@file:Suppress("RemoveRedundantQualifierName")

package warage

import com.bekvon.bukkit.residence.Residence
import com.bekvon.bukkit.residence.event.ResidenceChangedEvent
import com.bekvon.bukkit.residence.protection.ClaimedResidence
import com.bekvon.bukkit.residence.protection.FlagPermissions
import lk.vexview.api.VexViewAPI
import lk.vexview.builders.Builders
import lk.vexview.tag.TagDirection
import lk.vexview.tag.components.VexImageTag
import lk.vexview.tag.components.VexTextTag
import net.milkbowl.vault.economy.Economy
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.Location
import org.bukkit.Sound
import org.bukkit.entity.Player
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicReference
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

name = "定制占点脚本"

enum class Camp(val color: ChatColor, val checkProgress: (Double) -> Boolean) {
    UN(BLUE, { it < 0 }), UOA(RED, { it > 0 }), NONE(GRAY, { false });

    val nameWithColor get() = "$color$name"
    val opponent: Camp
        get() = when (this) {
            UN -> UOA
            UOA -> UN
            else -> error("can't get None's opponent")
        }

    companion object {
        val two = arrayOf(UN, UOA)
    }
}

data class Time(val start: String, val end: String)
data class PosData(val displayName: String, val money: Int, val dependent: Map<Camp, String>)

val msgY by config.key(5, "上部显示Y轴距离(单位未知)")
val enableWorld by config.key(setOf("world"), "战区世界设置,示例为主世界")
//val occupyTime by config.key(Duration.ofSeconds(30)!!, "占点用时") //Duration 因为上游库问题,暂时无法使用
val broadcastRange by config.key(50, "据点状态广播范围(单位格)")
val occupyOnceReward by config.key(1.0, "占点成功时的奖励倍率,最终奖励=据点价值*倍率")
val vipExtReward by config.key(1.0, "Vip额外金钱奖励倍数,默认1倍,总金额2倍")
val occupyTime by config.key(30, "占点用时(单位秒)")
val rewardTime by config.key(5 * 60, "据点奖励间隔(单位秒)")
val enableTime by config.key(Time("08:00", "20:00"), "占点开启时间,采用HH:MM格式")
val rewardK by config.key(0.0, "奖励平衡系数,建议取值0-1.5")

val poses by config.key(mapOf("example" to PosData("示例据点", 100, mapOf(Camp.UN to "un前置", Camp.UOA to "uoa前置节点"))),
        "据点设置,键为领地名,")
var occupyData by config.key(emptyMap<String, Camp>(), "占点情况数据,请勿修改")

fun inEnableTime(): Boolean {
    val format = SimpleDateFormat("HH:MM")
    return enableTime.start <= format.format(Date()) && format.format(Date()) <= enableTime.end
}

fun Player.getCamp() = when {
    hasPermission("occupy.un") -> Camp.UN
    hasPermission("occupy.uoa") -> Camp.UOA
    else -> null
}

fun Camp.getPlayers(list: Collection<Player> = Bukkit.getOnlinePlayers(), needWarWorld: Boolean = true) = list.filter {
    it.getCamp() == this && (!needWarWorld || it.world.name in enableWorld)
}

val unbalance: Boolean
    get() {
        fun unbalance(a: Int, b: Int) = a + b <= 1 || max(a, b) > 2 * min(a, b)
        //return unbalance(Camp.UN.getPlayers().size, Camp.UN.getPlayers().size)
        return false
    }

fun Location.spawnTag(name: String, text: String, team: Camp) {
    VexViewAPI.addWorldTag(world, VexImageTag(name + "icon", x + 1, y + 3, z, "[local]${team.name}icon.png", 64, 64, 2f, 2f, TagDirection(0f, 0f, 0f, true, true)))
    VexViewAPI.addWorldTag(world, VexTextTag(name, x, y + 2, z, text, false, TagDirection(0f, 0f, 0f, true, true)))
}

fun Player.showStatus(state: String, progress: Int) {
    /**@param now 总进度,-100为UN完全占领,100为UOA完全占领
     */
    fun getProgress(now: Int) = buildString {
        val all = 100
        val abs = kotlin.math.abs(now)
        append(" ".repeat(26))
        append("${Camp.UN.color}${ChatColor.BOLD}UN ")
        if (progress <= 0) {
            append(Camp.UN.color)
            append("|".repeat(abs))
            append(Camp.NONE.color)
            append("|".repeat(all - abs))
        } else {
            append(Camp.NONE.color)
            append("|".repeat(all - abs))
            append(Camp.UOA.color)
            append("|".repeat(abs))
        }
        append("${Camp.UOA.color}${ChatColor.BOLD} UOA")
    }
    Builders.texts().run {
        offset(-1, msgY)
        scale(2.5)
        addLine(state)
        addLine(getProgress(progress))
        VexViewAPI.sendHUD(this@showStatus, toHUD("occupy_progress", 2, 10))
    }
}


inner class RuntimePosData(private val name: String, val data: PosData, val residence: ClaimedResidence) {
    val tagName get() = "occupy_$name"
    val tagPos
        get() = residence.areaArray[0].run {
            highLoc.clone().add(lowLoc).multiply(0.5)!!
        }

    var state
        get() = occupyData.getOrDefault(name, Camp.NONE)
        set(value) {
            if (value == state) return
            occupyData = occupyData + (name to value)
            stateChange()
        }

    @Volatile
    private var progress = when (state) {
        Camp.UN -> -100.0
        Camp.UOA -> 100.0
        Camp.NONE -> 0.0
    }
    private val playerState = AtomicInteger(0) //正代表UN阵营,负代表UOA阵营人数,都有或都没有为0
    private val broadcastStatePlayers = AtomicReference<List<Player>>(emptyList())
    private lateinit var lastTag: String
    private fun stateChange() {
        val msg = buildString {
            if (state == Camp.NONE)
                append("${ChatColor.GRAY}未被占领")
            else
                append(state.nameWithColor + "据点")
            when {
                playerState.get() > 0 && state != Camp.UN -> append(" ${Camp.UN.nameWithColor}争夺中")
                playerState.get() < 0 && state != Camp.UOA -> append(" ${Camp.UOA.nameWithColor}争夺中")
            }
        }
        lastTag = "${ChatColor.GREEN}${data.displayName}: $msg"
        tagPos.spawnTag(tagName, lastTag, state)
    }

    init {
        stateChange()
    }

    fun dependentPos(c: Camp): RuntimePosData? = runtimeData[data.dependent[c]]
    fun dependentOk(c: Camp): Boolean {
        return when (val depend = data.dependent[c] ?: "NO") {
            "NO" -> false
            "YES" -> true
            in runtimeData -> runtimeData[depend]!!.state == c
            else -> error("[占点]据点${data.displayName}的依赖($depend)不存在,可选:YES,NO,或其他节点ID")
        }
    }

    //Must in [Dispatchers.bukkit]
    fun findPlayersTick() {
        broadcastStatePlayers.set(tagPos.world.players.filter { it.location.distanceSquared(tagPos) < broadcastRange * broadcastRange })
        var newState = 0
        if (!inEnableTime())
            return residence.playersInResidence.forEach { it.sendMessage("${GREEN}[据点]${YELLOW}非占点时间") }
        if (unbalance)
            return residence.playersInResidence.forEach { it.sendMessage("${GREEN}[据点]${YELLOW}当前双方战区在线人数不均衡,禁止占点") }
        for (p in residence.playersInResidence) {
            val camp = p.getCamp()
            if (camp !in Camp.two) return
            if (camp == Camp.UN && newState < 0 || camp == Camp.UOA && newState > 0) {
                newState = 0 //has opponent
                break
            }
            when (camp) {
                Camp.UN -> newState++
                Camp.UOA -> newState--
                else -> {
                }
            }
        }
        if (playerState.get() == newState) return
        playerState.set(newState)
        stateChange()
        tick(0.0)//quick update
    }

    //Must in [Dispatchers.bukkit]
    fun tick(dTime: Double) {
        var newProgress = progress
        newProgress -= playerState.get() * dTime * 100 / occupyTime
        if (newProgress > 100) newProgress = 100.0
        if (newProgress < -100) newProgress = -100.0
        progress.let { now ->
            if (now == newProgress) return@let
            for (camp in Camp.two) {
                if (dependentOk(camp)) continue
                if (camp.checkProgress(now) || state != Camp.NONE) return@let
                if (camp.checkProgress(newProgress))
                    camp.getPlayers(broadcastStatePlayers.get()).forEach {
                        it.sendMessage("${GREEN}[据点]${YELLOW}当前不能占领${data.displayName}")
                    }
            }
            progress = newProgress
            when {
                state == Camp.NONE && abs(newProgress) == 100.0 -> {
                    state = Camp.two.first { it.checkProgress(newProgress) }
                    eco?.let { eco ->
                        state.getPlayers(broadcastStatePlayers.get(), false).forEach {
                            val money = data.money * occupyOnceReward
                            eco.depositPlayer(it, money)
                            it.sendMessage("${GREEN}[据点]${YELLOW}占领 ${data.displayName} ${YELLOW}成功,奖励${"%.2f".format(money)}₠")
                            if (it.hasPermission("occupy.vip")) {
                                eco.depositPlayer(it, money * vipExtReward)
                                it.sendMessage("${GREEN}[据点]${YELLOW}VIP额外奖励${"%.2f".format(money * vipExtReward)}₠")
                            }
                        }
						state.getPlayers(needWarWorld = false).forEach {
                        it.sendMessage("${GREEN}[前线战报]${YELLOW}据点${GOLD}${data.displayName}${YELLOW}已被我军占领!")
                    }
                    }
                    broadcastStatePlayers.get().forEach {
                        it.playSound(tagPos, Sound.EXPLODE, 1f, 1f)
                    }
                }
                state in Camp.two && state.opponent.checkProgress(newProgress) -> {
                    state.getPlayers(needWarWorld = false).forEach {
                        it.sendMessage("${GREEN}[前线战报]${YELLOW}${RED}据点${YELLOW}${data.displayName}${RED}被抢夺!!")
                        it.sendMessage("${GREEN}[前线战报]${RED}不能让敌军占据优势，请火速前往夺回！")
                    }
                    state = Camp.NONE
                    broadcastStatePlayers.get().forEach {
                        it.playSound(tagPos, Sound.CLICK, 1f, 1f)
                    }
                }
            }
        }
        broadcastStatePlayers.get().forEach {
            it.showStatus(lastTag, newProgress.toInt())
        }
    }
}

val runtimeData = mutableMapOf<String, RuntimePosData>()

listen<ResidenceChangedEvent> { e ->
    e.from?.let { runtimeData[it.name] }?.findPlayersTick()
    e.to?.let { runtimeData[it.name] }?.apply {
        e.player.sendMessage("${GREEN}[据点]${YELLOW}你已进入${state.nameWithColor}${YELLOW}占领的据点:${data.displayName}")
        findPlayersTick()
    }
}

var eco: Economy? = null

onEnable {
    launch {
        while (Residence.getResidenceManager() == null) {
            logger.info("检测到领地插件未完成初始化，延时1分钟加载")
            delay(60_000)
        }
        poses.forEach { (name, data) ->
            val residence = Residence.getResidenceManager().getByName(name)
                    ?: return@forEach logger.warning("找不到对应领地:$name")
            runtimeData[name] = RuntimePosData(name, data, residence)
        }
        launch {
            while (true) {
                withContext(Dispatchers.game) {
                    runtimeData.values.forEach { it.findPlayersTick() }
                }
                delay(2000)
            }
        }
        launch {
            while (true) {
                delay(400)
                withContext(Dispatchers.game) {
                    runtimeData.values.forEach { it.tick(0.4) }
                }
            }
        }
        eco = Bukkit.getServicesManager().getRegistration(Economy::class.java)?.provider
        if (eco != null)
            launch {
                logger.warning("$GREEN 奖励系统初始化成功")
                while (true) {
                    delay(rewardTime * 1000L)
                    withContext(Dispatchers.game) {
                        if (unbalance)
                            return@withContext Bukkit.broadcastMessage("${GREEN}[据点]${YELLOW}战区人数过少,无据点奖励")
                        Camp.two.forEach { camp ->
                            val poses = runtimeData.values.filter { it.state == camp }
                            if (poses.isEmpty()) return@forEach
                            val selfN = camp.getPlayers().size
                            val otherN = camp.opponent.getPlayers().size
                            val k = 1 + (otherN - selfN) * 2.0 / (selfN + otherN) * rewardK
                            val baseMoney = poses.sumBy { it.data.money } * 0.5
                            val sumMoney = baseMoney * min(k, 1.5)
                            camp.getPlayers(needWarWorld = true).forEach {
                                if (sumMoney > 0) {
                                    eco!!.depositPlayer(it, sumMoney)
                                    it.sendMessage("${GREEN}[战区补贴]${YELLOW}当前共占领${poses.size}个据点，奖励${"%.2f".format(sumMoney)}₠")
                                    if (it.hasPermission("occupy.vip")) {
                                        eco!!.depositPlayer(it, sumMoney * vipExtReward)
                                        it.sendMessage("${GREEN}[战区补贴]${YELLOW}VIP额外奖励${"%.2f".format(sumMoney * vipExtReward)}₠")
                                    }
                                }
                            }
                            camp.getPlayers(needWarWorld = false).forEach {
                                if (sumMoney <= 0) {
                                    eco!!.depositPlayer(it, baseMoney)
                                    it.sendMessage("${GREEN}[据点]${YELLOW}高战场维护费用,据点奖励降为${"%.2f".format(baseMoney)}₠")
                                }
                                if (selfN + otherN <= 0) {
                                    eco!!.depositPlayer(it, baseMoney)
                                    it.sendMessage("${GREEN}[据点]${YELLOW}战区无战事,据点奖励降为${"%.2f".format(baseMoney)}₠")
                                }
                                if (sumMoney > 0) {
                                    eco!!.depositPlayer(it, sumMoney)
                                    it.sendMessage("${GREEN}[据点]${YELLOW}当前共占领${poses.size}个据点，奖励${"%.2f".format(sumMoney)}₠")
                                    if (it.hasPermission("occupy.vip")) {
                                        eco!!.depositPlayer(it, sumMoney * vipExtReward)
                                        it.sendMessage("${GREEN}[据点]${YELLOW}VIP额外奖励${"%.2f".format(sumMoney * vipExtReward)}₠")
                                    }
                                }
                            }
                        }
                        runtimeData.values.forEach { it.tick(0.4) }
                    }
                }
            }
        else logger.warning("$RED 未找到Vault插件,奖励系统自动关闭")
    }
}

onDisable {
    runtimeData.values.forEach {
        VexViewAPI.removeWorldTag(it.tagPos.world, it.tagName)
        VexViewAPI.removeWorldTag(it.tagPos.world, it.tagName + "icon")
    }
}

val playerJob = mutableMapOf<Player, Job>()

command("occupyTp", "传送至占点前线"){
    permission = "occupy.tp"
    body{
        val camp = player?.getCamp() ?: returnReply("[red]找不到队伍".with())
        val pos = runtimeData.values.find { it.state != camp && it.dependentPos(camp)?.state == camp }?.dependentPos(camp)
                ?: returnReply("[red]找不到可传送据点".with())
        playerJob[player!!]?.cancel()
        playerJob[player!!] = launch {
            reply("[GREEN][据点][YELLOW]将在10秒后传送至前线据点: {name}".with("name" to pos.data.displayName))
            delay(10000)
            withContext(Dispatchers.game) {
                pos.residence.permissions.setPlayerFlag(player!!.name, "tp", FlagPermissions.FlagState.TRUE)
                pos.residence.tpToResidence(null, player!!, true)
                pos.residence.permissions.setPlayerFlag(player!!.name, "tp", FlagPermissions.FlagState.NEITHER)
            }
        }
    }
}