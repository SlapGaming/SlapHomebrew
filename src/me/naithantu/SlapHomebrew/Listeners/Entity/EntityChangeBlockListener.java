package me.naithantu.SlapHomebrew.Listeners.Entity;

import me.naithantu.SlapHomebrew.SlapHomebrew;

import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityChangeBlockEvent;

public class EntityChangeBlockListener implements Listener {
	SlapHomebrew plugin;

	public EntityChangeBlockListener(SlapHomebrew plugin) {
		this.plugin = plugin;
	}

	@EventHandler
	public void onEntityChangeBlock(EntityChangeBlockEvent event) {
		if (event.getEntityType() == EntityType.BOAT && event.getBlock().getType() == Material.WATER_LILY) {
			Entity boat = event.getEntity();
			if (boat.getPassenger() == null) {
				event.setCancelled(true);
			} else if (boat.getPassenger() instanceof Player) {
				Player player = (Player) boat.getPassenger();
				if (!plugin.getWorldGaurd().canBuild(player, event.getBlock())) {
					event.setCancelled(true);
				}
			}
		}
	}
}
