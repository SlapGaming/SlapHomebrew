package me.naithantu.SlapHomebrew.Listeners.Player;

import me.naithantu.SlapHomebrew.SlapHomebrew;
import me.naithantu.SlapHomebrew.Commands.Staff.MessageCommand;
import me.naithantu.SlapHomebrew.Controllers.AwayFromKeyboard;
import me.naithantu.SlapHomebrew.Controllers.Jails;
import me.naithantu.SlapHomebrew.Controllers.PlayerLogger;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class PlayerChatListener implements Listener {
	private SlapHomebrew plugin;
	private AwayFromKeyboard afk;
	private Jails jails;
	private PlayerLogger playerLogger;
	
	public PlayerChatListener(SlapHomebrew plugin, AwayFromKeyboard afk, Jails jails, PlayerLogger playerLogger){
		this.plugin = plugin;
		this.afk = afk;
		this.jails = jails;
		this.playerLogger = playerLogger;
	}
	@EventHandler
	public void onPlayerChat(AsyncPlayerChatEvent event) {
		Player player = event.getPlayer();
		String playerName = player.getName();
				
		//Block chat if not moved yet
		if (!playerLogger.hasMoved(playerName)) {
			if (event.getMessage().matches("connected with .* using MineChat")) {
				player.kickPlayer("MineChat is not allowed on this server.");
				event.setCancelled(true);
				return;
			}
			event.setCancelled(true);
			playerLogger.sendNotMovedMessage(player);
			return;
		}		
		
		//Block chat while in jail.
		if (jails.isInJail(playerName)) {
			if (!jails.isAllowedToChat(playerName)) {
				event.setCancelled(true);
				player.sendMessage(ChatColor.GRAY + "You are jailed. Use /timeleft to check your time left in jail.");
				return;
			}
		}
		
		//Set last activity
		playerLogger.setLastActivity(playerName);
		
		String message = event.getMessage().toLowerCase();
		
		//Block "lag" messages
		if (message.equals("lag") || message.equals("lagg")) {
			event.setCancelled(true);
			player.chat("/lag");
			return;
		}
		
		if (plugin.getMessages().contains(player.getName())) {
			message = event.getMessage();
			player.sendMessage(ChatColor.GOLD + "[SLAP] " + ChatColor.WHITE + "The new message has " + MessageCommand.messageName + " as name and " + message + " as message.");
			plugin.getMessages().remove(player.getName());
			plugin.getConfig().set("messages." + MessageCommand.messageName, message);
			event.setCancelled(true);
			plugin.saveConfig();
		}
		
		//Check for AFK
		if (afk.isAfk(playerName)) {
			afk.leaveAfk(playerName);
		}
		
		//DoubleMsg
		if (player.hasPermission("slaphomebrew.staff") && !event.isCancelled()) {
			message = event.getMessage();
			if (playerLogger.hasMessage(playerName)) {
				playerLogger.sendSecondMessage(playerName, message);
				event.setCancelled(true);
			} else {
				int l = message.length();
				if (l > 10) {
					if (message.substring(l - 3).equals("*--")) {
						playerLogger.setFirstMessage(playerName, message);
						event.setCancelled(true);
					}
				}
			}
		}
	}
}
