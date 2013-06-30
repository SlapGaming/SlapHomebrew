package me.naithantu.SlapHomebrew.Listeners;

import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

public class PlayerTeleportListener implements Listener {

	@EventHandler
	public void onPlayerTeleport(PlayerTeleportEvent event) {
		Player player = event.getPlayer();		
		if (event.getTo().getWorld().getName().equals("world_nether") && event.getTo().getBlockY() >= 127){ 
			player.sendMessage(ChatColor.RED + "You may not go above the nether!");
			event.setCancelled(true);
		}
		if (player.getWorld().getName().equals("world_pvp") && event.getCause().equals(TeleportCause.ENDER_PEARL)) {
			player.sendMessage(ChatColor.GOLD + "[SLAP]" + ChatColor.WHITE + " Using ender pearls to teleport is not allowed in pvp!");
			event.setCancelled(true);
		}
		
		//Allow flight for double jumping in start world.
		if(event.getTo().getWorld().getName().equals("world_start") && !event.getFrom().getWorld().getName().equals("world_start")){
			player.setAllowFlight(true);
		} else if (event.getFrom().getWorld().getName().equals("world_start") && !event.getTo().getWorld().getName().equals("world_start") && player.getGameMode() != GameMode.CREATIVE){
			player.setAllowFlight(false);
		}
		
		if(event.getFrom().getWorld().getName().equals("world_sonic") && !event.getTo().getWorld().getName().equals("world_sonic")){
			player.getInventory().clear();
			player.getInventory().setBoots(null);
			player.getActivePotionEffects().clear();
		}
	}
}
