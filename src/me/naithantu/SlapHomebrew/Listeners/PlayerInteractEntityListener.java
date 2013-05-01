package me.naithantu.SlapHomebrew.Listeners;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Vehicle;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;

public class PlayerInteractEntityListener implements Listener {
	@EventHandler
	public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
		//Temporary fix for broken vehicleexitevent.
		if(event.getRightClicked().getType() == EntityType.MINECART || event.getRightClicked().getType() == EntityType.BOAT){
			if(event.getPlayer().isInsideVehicle()){
				Vehicle vehicle = (Vehicle) event.getRightClicked();
				if (vehicle.hasMetadata("slapVehicle")) {
					vehicle.remove();
				}
			}
		}
		
		if(event.getRightClicked().getType() == EntityType.ITEM_FRAME){
			if(event.getPlayer().getItemInHand().getType() == Material.MAP){
				event.getPlayer().sendMessage(ChatColor.GOLD + "[SLAP] " + ChatColor.WHITE + "You may not place maps in item frames!");
				event.setCancelled(true);
			}
		}
	}
}
