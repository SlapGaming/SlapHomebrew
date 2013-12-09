package me.naithantu.SlapHomebrew.Listeners.Player;

import me.naithantu.SlapHomebrew.Commands.Fun.RideCommand;
import me.naithantu.SlapHomebrew.Commands.Staff.TeleportMobCommand;
import me.naithantu.SlapHomebrew.Controllers.Horses;
import me.naithantu.SlapHomebrew.Controllers.PlayerLogger;
import me.naithantu.SlapHomebrew.Util.Util;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Horse;
import org.bukkit.entity.LeashHitch;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Horse.Variant;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;

public class PlayerInteractEntityListener implements Listener {
	
	private Horses horses;
	private PlayerLogger playerLogger;
	
	public PlayerInteractEntityListener(Horses horses, PlayerLogger playerLogger) {
		this.horses = horses;
		this.playerLogger = playerLogger;
	}
	
	@EventHandler
	public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
		Player player = event.getPlayer();
		String playername = player.getName();
		Entity clickedEntity = event.getRightClicked();
				
		//Set last activity
		playerLogger.setLastActivity(playername);
		
		//Horse info click
		if (horses.isInfoClick(playername)) {
			if (clickedEntity.getType() == EntityType.HORSE) {
				horses.infoHorse(player, clickedEntity.getUniqueId().toString());
			} else {
				player.sendMessage(ChatColor.RED + "This is not a horse.");
			}
			event.setCancelled(true);
			return;
		}
				
		//Entity Ride Click
		if (RideCommand.rightClick(playername)) {
			if (player.isInsideVehicle()) {
				player.getVehicle().eject();
			}
			if (clickedEntity instanceof LivingEntity) {
				clickedEntity.setPassenger(player);
			}
			return;
		}
		
		//Singlemove Teleport mob
		if (TeleportMobCommand.isInMap(playername)) {
			event.setCancelled(true);
			Player toPlayer = Bukkit.getPlayerExact(TeleportMobCommand.getToPlayerName(playername));
			if (toPlayer == null || !toPlayer.isOnline()) {
				TeleportMobCommand.toPlayerWentOffline(playername);
				Util.badMsg(player, "The player to teleport to is offline! Single Move disabled.");
				return;
			}
			
			if (clickedEntity instanceof LivingEntity && !(clickedEntity instanceof Player)) {
				TeleportMobCommand.teleportMob((LivingEntity) clickedEntity, toPlayer.getLocation());
			} else {
				Util.badMsg(player, "This entity cannot be teleported!");
			}
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
			if (clickedEntity instanceof LeashHitch) {
				if (!horses.isOwnerOfLeash(clickedEntity.getUniqueId().toString(), playername)) {
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
			}
		}
		
		//Horse egg on undead/skeleton horse -> Nope
		if (clickedEntity.getType() == EntityType.HORSE) {
			Horse horse = (Horse) event.getRightClicked();
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
