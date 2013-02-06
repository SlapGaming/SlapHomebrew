package me.naithantu.SlapHomebrew.Listeners;

import me.naithantu.SlapHomebrew.SlapCommands;

import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;

public class InteractListener implements Listener{
	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event) {
		Player player = event.getPlayer();
		if (SlapCommands.retroBow.contains(player.getName())) {
			player.launchProjectile(Arrow.class);
		}
	}
}
