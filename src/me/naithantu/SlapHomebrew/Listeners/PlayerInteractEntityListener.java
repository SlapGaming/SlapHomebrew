package me.naithantu.SlapHomebrew.Listeners;

import me.naithantu.SlapHomebrew.Horses;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Horse;
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
		if(event.getRightClicked().getType() == EntityType.ITEM_FRAME){
			if(event.getPlayer().getItemInHand().getType() == Material.MAP){
				event.getPlayer().sendMessage(ChatColor.GOLD + "[SLAP] " + ChatColor.WHITE + "You may not place maps in item frames!");
				event.setCancelled(true);
			}
		}
		
		//Leash on Horse
		if (event.getRightClicked().getType() == EntityType.HORSE) {
			Horse horse = (Horse) event.getRightClicked();
			if (horses.hasOwner(horse.getUniqueId().toString())) {
				if (!horses.enterHorse(horse.getUniqueId().toString(), event.getPlayer())) {
					event.getPlayer().sendMessage(ChatColor.GOLD + "[SLAP] " + ChatColor.WHITE + "You're not allowed to interact with this horse.");
					event.setCancelled(true);
				}
			}
		}
		
		
	}
		
}
