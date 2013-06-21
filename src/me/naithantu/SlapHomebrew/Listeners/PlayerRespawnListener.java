package me.naithantu.SlapHomebrew.Listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerRespawnEvent;

public class PlayerRespawnListener implements Listener {
	@EventHandler
	public void onPlayerRespawn(final PlayerRespawnEvent event) {
		if (event.getRespawnLocation().getWorld().getName().equals("world_start")) {
			event.getPlayer().setAllowFlight(true);
		}
	}
}
