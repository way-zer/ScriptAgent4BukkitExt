import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.entity.Wolf
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEntityEvent
import org.bukkit.event.player.PlayerInteractEvent

require(
    ItemInfo(
        Material.BONE, "§7傀儡法杖", listOf(
            "§2§o召唤傀儡与你并肩作战"
        )
    )
)

val entities = mutableSetOf<Entity>()

fun spawn(p: Player) {
    val e = List(3) { p.world.spawn(p.location, Wolf::class.java) }
    e.forEach { it.owner = p }
    entities.addAll(e)
    launch(Dispatchers.game) {
        delay(120_000)
        e.forEach { it.remove() }
        entities.removeAll(e)
    }
}

listen<PlayerInteractEvent> { e ->
    if (e.action == Action.RIGHT_CLICK_AIR || e.action == Action.RIGHT_CLICK_BLOCK) {
        if (isItem(e.item) && get<Permission>().hasPermission(e.player)) {
            e.isCancelled = true
            val item = e.item!!
            val p = e.player
            if (item.amount <= 1) item.type = Material.AIR else item.amount = item.amount - 1
            p.inventory.setItem(e.hand!!, item)
            p.updateInventory()
            spawn(p)
        }
    }
}

listen<PlayerInteractEntityEvent> { e ->
    if (e.rightClicked is Wolf && e.rightClicked in entities)
        e.isCancelled = true
}

onDisable {
    launch(Dispatchers.game) {
        entities.forEach { it.remove() }
    }
}