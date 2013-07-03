package me.naithantu.SlapHomebrew.Listeners;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

public class PlayerTeleportListener implements Listener {

	@EventHandler
	public void onPlayerTeleport(PlayerTeleportEvent event) {
		Player player = event.getPlayer();
		//Check for boats/minecarts to remove.
		Location from = event.getFrom();
		for(Entity entity: from.getWorld().getEntities()){
			System.out.println(entity.toString());
			if(entity.hasMetadata("slapVehicle")){ 
				if(entity.getMetadata("slapVehicle").get(0).asString().equals(player.getName())){
					entity.remove();
				}
			}
		}
		
		if (event.getTo().getWorld().getName().equals("world_nether") && event.getTo().getBlockY() >= 127){ 
			player.sendMessage(ChatColor.RED + "You may not go above the nether!");
			event.setCancelled(true);
		}
		if (player.getWorld().getName().equals("world_pvp") && event.getCause().equals(TeleportCause.ENDER_PEARL)) {
			player.sendMessage(ChatColor.GOLD + "[SLAP]" + ChatColor.WHITE + " Using ender pearls to teleport is not allowed in pvp!");
			event.setCancelled(true);
		}
	}
}
