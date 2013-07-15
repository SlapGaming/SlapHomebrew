package me.naithantu.SlapHomebrew.Listeners;

import me.naithantu.SlapHomebrew.Horses;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_6_R2.entity.CraftHanging;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Player;
import org.bukkit.entity.Horse.Variant;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;

public class PlayerInteractEntityListener implements Listener {
	
	private Horses horses;
	
	public PlayerInteractEntityListener(Horses horses) {
		this.horses = horses;
	}
	
	@EventHandler
	public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
		Player player = event.getPlayer();
		Entity clickedEntity = event.getRightClicked();
						
		//Horse info click
		if (horses.isInfoClick(player.getName())) {
			if (clickedEntity.getType() == EntityType.HORSE) {
				horses.infoHorse(player, clickedEntity.getUniqueId().toString());
			} else {
				player.sendMessage(ChatColor.RED + "This is not a horse.");
			}
			event.setCancelled(true);
			return;
		}
				
		//Item frame map block
		if(clickedEntity.getType() == EntityType.ITEM_FRAME){
			if(player.getItemInHand().getType() == Material.MAP){
				player.sendMessage(ChatColor.GOLD + "[SLAP] " + ChatColor.WHITE + "You may not place maps in item frames!");
				event.setCancelled(true);
			}
		}
		
		//Ignored for staff
		if (!player.hasPermission("slaphomebrew.staff")) {
			
			//Leash on Fence -- Warning: Dirty fix -- Will probably break on update
			if (clickedEntity instanceof CraftHanging) {
				if (!horses.isOwnerOfLeash(clickedEntity.getUniqueId().toString(), player.getName())) {
					player.sendMessage(ChatColor.RED + "You are not the owner of that leash.");
					event.setCancelled(true);
				}
			}
			
			//On horse
			if (clickedEntity.getType() == EntityType.HORSE) {
				Horse horse = (Horse) event.getRightClicked();
				//Leash on horse
				if (horses.hasOwner(horse.getUniqueId().toString())) {
					if (!horses.enterHorse(horse.getUniqueId().toString(), player)) {
						player.sendMessage(ChatColor.GOLD + "[SLAP] " + ChatColor.WHITE + "You're not allowed to interact with this horse.");
						event.setCancelled(true);
					}
				}
				
				//Egg on horse
				if (horse.getVariant() == Variant.SKELETON_HORSE || horse.getVariant() == Variant.UNDEAD_HORSE) {
					if (player.getItemInHand().getType() == Material.MONSTER_EGG){ 
						if (!event.isCancelled()) {
							player.sendMessage(ChatColor.RED + "This horse doesn't like what you are trying to do...");
							event.setCancelled(true);
						}
					}
				}
			}
		}
		
		
		
	}
		
}
