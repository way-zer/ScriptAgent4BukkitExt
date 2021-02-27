package warage.lib.db

import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.`java-time`.CurrentTimestamp
import org.jetbrains.exposed.sql.`java-time`.timestamp
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.Instant
import java.util.*

class WhiteList(id: EntityID<Int>) : IntEntity(id) {
    var uuid by T.uuid
    var qq by T.qq
    var lastName by T.lastName
    var lastTime by T.lastTime
    var lastAddress by T.lastAddress

    object T : IntIdTable("whiteList") {
        val uuid = uuid("uuid").uniqueIndex()
        val qq = long("qq").index()
        val lastName = varchar("lastName", 16).nullable()
        val createTime = timestamp("createTime").defaultExpression(CurrentTimestamp())
        val lastTime = timestamp("lastTime").defaultExpression(CurrentTimestamp())
        val lastAddress = varchar("lastIp", 16).nullable()
    }

    companion object : IntEntityClass<WhiteList>(T) {
        fun register(uuid: UUID, qq: Long) {
            transaction {
                new {
                    this.uuid = uuid
                    this.qq = qq
                }
            }
        }

        fun check(uuid: UUID, name: String, ip: String): Boolean {
            return transaction {
                val found = find { T.uuid eq uuid }.firstOrNull() ?: return@transaction false
                found.apply {
                    lastName = name
                    lastTime = Instant.now()
                    lastAddress = ip
                }
                true
            }
        }

        fun countByQQ(qq: Long): Long {
            return transaction {
                find { T.qq eq qq }.count()
            }
        }
    }
}