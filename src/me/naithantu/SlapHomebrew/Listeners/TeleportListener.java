package me.naithantu.SlapHomebrew.Listeners;

import me.naithantu.SlapHomebrew.Vehicles;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

public class TeleportListener implements Listener {
	Vehicles vehicles;

	public TeleportListener(Vehicles vehicles) {
		this.vehicles = vehicles;
	}

	@EventHandler
	public void onPlayerTeleport(PlayerTeleportEvent event) {
		Player player = event.getPlayer();
		if (player.getWorld().getName().equals("world_pvp") && event.getCause().equals(TeleportCause.ENDER_PEARL)) {
			player.sendMessage(ChatColor.GOLD + "[SLAP]" + ChatColor.WHITE + " Using ender pearls to teleport is not allowed in pvp!");
			event.setCancelled(true);
		}
	}
}
