package me.naithantu.SlapHomebrew.Listeners.Player;

import me.naithantu.SlapHomebrew.Controllers.AwayFromKeyboard;
import me.naithantu.SlapHomebrew.Controllers.Jails;
import me.naithantu.SlapHomebrew.Controllers.PlayerLogger;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

public class PlayerTeleportListener implements Listener {

	private Jails jails;
	private AwayFromKeyboard afk;
	private PlayerLogger playerLogger;
	
	public PlayerTeleportListener(Jails jails, AwayFromKeyboard afk, PlayerLogger playerLogger) {
		this.jails = jails;
		this.afk = afk;
		this.playerLogger = playerLogger;
	}
	
	@EventHandler
	public void onPlayerTeleport(PlayerTeleportEvent event) {
		Player player = event.getPlayer();
		String playername = player.getName();
		
		//Block if in jail
		if (jails.isInsideJail(playername)) {
			event.setCancelled(true);
			return;
		}
		
		//Remove AFK if AFK
		if (afk.isAfk(playername)) {
			afk.leaveAfk(playername);
		}
		
		//Set last activity
		playerLogger.setLastActivity(playername);
		
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
