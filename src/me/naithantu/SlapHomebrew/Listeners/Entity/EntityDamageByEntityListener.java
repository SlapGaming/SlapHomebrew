package me.naithantu.SlapHomebrew.Listeners.Entity;

import java.util.HashSet;

import me.naithantu.SlapHomebrew.SlapHomebrew;
import me.naithantu.SlapHomebrew.Controllers.Flag;
import me.naithantu.SlapHomebrew.Controllers.Horses;
import me.naithantu.SlapHomebrew.Util.Util;

import org.bukkit.Location;
import org.bukkit.entity.Animals;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Horse;
import org.bukkit.entity.NPC;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
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
		if(damager instanceof Projectile){
			Projectile projectile = (Projectile) damager;
			damager = projectile.getShooter();
		}
		
		if (damager != null && damager instanceof Player) {
			Entity entity = event.getEntity();
			
			if (event.getEntity() instanceof Player) {
				String world = event.getEntity().getWorld().getName();
				if (world.equals("world_pvp")) {
					HashSet<String> pvpWorld = plugin.getExtras().getPvpWorld();
					if (pvpWorld.contains(((Player) event.getEntity()).getName())) {
						Util.msg((Player) entity, "You have been attacked! Teleport cancelled!");
						pvpWorld.remove(((Player) event.getEntity()).getName());
					}
				}
			}
			
			if (entity instanceof Animals || entity instanceof NPC) {
				Location entityLoc = event.getEntity().getLocation();
				Player player = (Player) damager;
				if (!player.hasPermission("slaphomebrew.staff") && !Util.hasFlag(plugin, entityLoc, Flag.NOMOBPROTECT) && !plugin.getWorldGaurd().canBuild(player, entityLoc)) {
					event.setCancelled(true);
					player.sendMessage(Util.getHeader() + "You may not attack animals or villagers here!");
				}
			}
		}
		
		//Horse protection
		if (event.getEntityType() == EntityType.HORSE && damager instanceof Player) {
			Player player = (Player) damager;
			Horse horse = (Horse) event.getEntity();
			if (horses.hasOwner(horse.getUniqueId().toString())) {
				String owner = horses.getOwner(horse.getUniqueId().toString());
				if (!owner.equals(player.getName())) {
					if (!player.hasPermission("slaphomebrew.staff")) {
						event.setCancelled(true);
						player.sendMessage(Util.getHeader() + "You are not allowed to attack " + owner + "'s horse.");
					}
				}
			}
		}
		
		
	}
}
