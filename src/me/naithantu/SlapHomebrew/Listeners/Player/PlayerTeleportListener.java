package me.naithantu.SlapHomebrew.Listeners.Player;

import me.naithantu.SlapHomebrew.Controllers.AwayFromKeyboard;
import me.naithantu.SlapHomebrew.Controllers.Jails;
import me.naithantu.SlapHomebrew.Listeners.AbstractListener;
import me.naithantu.SlapHomebrew.PlayerExtension.PlayerControl;

import me.naithantu.SlapHomebrew.PlayerExtension.SlapPlayer;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

public class PlayerTeleportListener extends AbstractListener {

	private Jails jails;
	private AwayFromKeyboard afk;
	
	public PlayerTeleportListener(Jails jails, AwayFromKeyboard afk) {
		this.jails = jails;
		this.afk = afk;
	}
	
	@EventHandler
	public void onPlayerTeleport(PlayerTeleportEvent event) {
		Player player = event.getPlayer();
		String playername = player.getName();
        String UUID = player.getUniqueId().toString();
		
		//Block if in jail
		if (jails.isJailed(UUID)) {
			event.setCancelled(true);
			return;
		}
		
		//Remove AFK if AFK
		if (afk.isAfk(player)) {
			afk.leaveAfk(player);
		}
		
		//Set last activity
		SlapPlayer sPlayer = PlayerControl.getPlayer(player);
        if (sPlayer != null) {
            sPlayer.active();
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
