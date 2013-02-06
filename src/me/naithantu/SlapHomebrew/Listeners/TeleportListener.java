package me.naithantu.SlapHomebrew.Listeners;

import me.naithantu.SlapHomebrew.SlapHomebrew;

import org.bukkit.ChatColor;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

public class TeleportListener implements Listener {
	@EventHandler
	public void onPlayerTeleport(PlayerTeleportEvent event) {
		Player player = event.getPlayer();
		if (player.isInsideVehicle()) {
			if (player.getVehicle() instanceof Minecart) {
				if (SlapHomebrew.mCarts.contains(player.getVehicle().getUniqueId())) {
					event.getPlayer().leaveVehicle();
				}
			}
		}
		if(event.getPlayer().getWorld().getName().equals("world_pvp") && event.getCause().equals(TeleportCause.ENDER_PEARL)){
			event.getPlayer().sendMessage(ChatColor.GOLD + "[SLAP]" + ChatColor.WHITE + " Using ender pearls to teleport is not allowed in pvp!");
			event.setCancelled(true);
		}
	}
}
