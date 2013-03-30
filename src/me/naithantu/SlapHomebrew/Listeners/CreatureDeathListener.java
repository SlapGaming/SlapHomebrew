package me.naithantu.SlapHomebrew.Listeners;

import me.naithantu.SlapHomebrew.SlapHomebrew;
import me.naithantu.SlapHomebrew.Util;

import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Wither;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;

public class CreatureDeathListener implements Listener {
	SlapHomebrew plugin;

	public CreatureDeathListener(SlapHomebrew plugin) {
		this.plugin = plugin;
	}

	@EventHandler
	public void onWitherDeath(EntityDeathEvent event) {
		Entity entity = event.getEntity();
		if (entity instanceof Wither) {
			//If wither has slap metadata.
			if (entity.hasMetadata("slapWither")) {
				Player player = plugin.getServer().getPlayer(entity.getMetadata("slapWither").get(0).toString());
				//Drop item naturally if player logged off, is dead or has a full inventory.
				if (player == null || player.isDead() || Util.hasFullInventory(player))
					return;
				event.getDrops().clear();
				player.getInventory().addItem(new ItemStack(Material.NETHER_STAR, 1));
			}
		}
	}
}
