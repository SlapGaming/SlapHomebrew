package me.naithantu.SlapHomebrew.Listeners;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;

public class PlayerInteractEntityListener implements Listener {
	@EventHandler
	public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
		if(event.getRightClicked().getType() == EntityType.ITEM_FRAME){
			if(event.getPlayer().getItemInHand().getType() == Material.MAP){
				event.getPlayer().sendMessage(ChatColor.GOLD + "[SLAP] " + ChatColor.WHITE + "You may not place maps in item frames!");
				event.setCancelled(true);
			}
		}
	}
}
