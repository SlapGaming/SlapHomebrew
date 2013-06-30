package me.naithantu.SlapHomebrew.Listeners;

import me.naithantu.SlapHomebrew.Flag;
import me.naithantu.SlapHomebrew.SlapHomebrew;
import me.naithantu.SlapHomebrew.Util;

import org.bukkit.Location;
import org.bukkit.entity.Animals;
import org.bukkit.entity.Entity;
import org.bukkit.entity.NPC;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class EntityDamageByEntityListener implements Listener {
	SlapHomebrew plugin;

	public EntityDamageByEntityListener(SlapHomebrew plugin) {
		this.plugin = plugin;
	}

	@EventHandler
	public void onEntityDamageByEvent(EntityDamageByEntityEvent event) {
		Entity damager = event.getDamager();
		if (damager != null && damager instanceof Player) {
			Entity entity = event.getEntity();
			if (entity instanceof Animals || entity instanceof NPC) {
				Location entityLoc = event.getEntity().getLocation();
				Player player = (Player) damager;
				if (!player.hasPermission("slaphomebrew.staff") && !Util.hasFlag(plugin, entityLoc, Flag.NOMOBPROTECT) && !plugin.getWorldGuard().canBuild(player, entityLoc)) {
					event.setCancelled(true);
					player.sendMessage(Util.getHeader() + "You may not attack animals or villagers here!");
				}
			}
		}
	}
}
