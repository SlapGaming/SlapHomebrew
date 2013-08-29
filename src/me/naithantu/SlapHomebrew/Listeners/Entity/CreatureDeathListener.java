package me.naithantu.SlapHomebrew.Listeners.Entity;

import me.naithantu.SlapHomebrew.SlapHomebrew;
import me.naithantu.SlapHomebrew.Controllers.Horses;

import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Wither;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;

public class CreatureDeathListener implements Listener {
	SlapHomebrew plugin;
	Horses horses;

	public CreatureDeathListener(SlapHomebrew plugin, Horses horses) {
		this.plugin = plugin;
		this.horses = horses;
	}

	@EventHandler
	public void onWitherDeath(EntityDeathEvent event) {
		Entity entity = event.getEntity();
		if (entity instanceof Wither) {
			//If wither has slap metadata.
			if (entity.hasMetadata("slapWither")) {
				Player player = plugin.getServer().getPlayer(entity.getMetadata("slapWither").get(0).asString());
				//Drop item naturally if player logged off, is dead or has a full inventory.
				if (player != null && !player.isDead() && player.getInventory().firstEmpty() != -1) {
					event.getDrops().clear();
					System.out.println("[SLAP] Wither star has been given to: " + player.getName());
					player.getInventory().addItem(new ItemStack(Material.NETHER_STAR, 1));
				}
			}
		}

		if (entity.hasMetadata("slapFireMob")) {
			event.setDroppedExp(0);
			event.getDrops().clear();
		}
		
		if (entity.getType() == EntityType.HORSE) {
			String entityID = entity.getUniqueId().toString();
			if (horses.hasOwner(entityID)) {
				horses.removeHorse(entityID);
			}
		}
	}
}
