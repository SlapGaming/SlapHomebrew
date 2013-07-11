package me.naithantu.SlapHomebrew.Listeners;

import me.naithantu.SlapHomebrew.Flag;
import me.naithantu.SlapHomebrew.Horses;
import me.naithantu.SlapHomebrew.SlapHomebrew;
import me.naithantu.SlapHomebrew.Util;

import org.bukkit.Location;
import org.bukkit.entity.Animals;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Horse;
import org.bukkit.entity.NPC;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class EntityDamageByEntityListener implements Listener {
	
	SlapHomebrew plugin;
	Horses horses;

	public EntityDamageByEntityListener(SlapHomebrew plugin, Horses horses) {
		this.plugin = plugin;
		this.horses = horses;
	}

	@EventHandler
	public void onEntityDamageByEvent(EntityDamageByEntityEvent event) {
		//Entity protector on WG
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
		
		//Horse protection
		if (event.getEntityType() == EntityType.HORSE && damager instanceof Player) {
			Player player = (Player) damager;
			Horse horse = (Horse) event.getEntity();
			System.out.println("log1");
			if (horses.hasOwner(horse.getUniqueId().toString())) {
				System.out.println("log2");
				String owner = horses.getOwner(horse.getUniqueId().toString());
				if (!owner.equals(player.getName())) {
					System.out.println("log3");
					if (!player.hasPermission("slaphomebrew.staff")) {
						System.out.println("log4");
						event.setCancelled(true);
						player.sendMessage(Util.getHeader() + "You are not allowed to attack " + owner + "'s horse.");
					}
				}
			}
		}
		
		
	}
}
