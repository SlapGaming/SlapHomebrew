package me.naithantu.SlapHomebrew.Listeners.Player;

import me.naithantu.SlapHomebrew.Listeners.AbstractListener;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerRespawnEvent;

public class PlayerRespawnListener extends AbstractListener {
	
	@EventHandler(priority = EventPriority.LOWEST)
	public void onPlayerRespawn(final PlayerRespawnEvent event) {
		Location spawn = Bukkit.getServer().getWorld("world_start").getSpawnLocation();
		spawn.setYaw(-180F);
		event.setRespawnLocation(spawn);
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerRespawnMonitor(final PlayerRespawnEvent event) {
		if (event.getRespawnLocation().getWorld().getName().equals("world_start")) {
			event.getPlayer().setAllowFlight(true);
		}
	}
}
