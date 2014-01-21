package me.naithantu.SlapHomebrew.Listeners.Player;

import me.naithantu.SlapHomebrew.Controllers.AwayFromKeyboard;
import me.naithantu.SlapHomebrew.Controllers.Jails;
import me.naithantu.SlapHomebrew.Controllers.PlayerLogger;
import me.naithantu.SlapHomebrew.Listeners.AbstractListener;
import me.naithantu.SlapHomebrew.Util.Util;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;


public class PlayerCommandListener extends AbstractListener {

	private AwayFromKeyboard afk;
	private Jails jails;
	private PlayerLogger playerLogger;
	
	public PlayerCommandListener(AwayFromKeyboard afk, Jails jails, PlayerLogger playerLogger){
		this.afk = afk;
		this.jails = jails;
		this.playerLogger = playerLogger;
	}
	
	@EventHandler
	public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
		Player player = event.getPlayer();
		String message = event.getMessage().toLowerCase().trim();
		String[] commandMessage = message.split(" ");
		String playerName = player.getName();
		
		//Block commands if not moved yet
		if (!playerLogger.hasMoved(playerName)) {
			event.setCancelled(true);
			playerLogger.sendNotMovedMessage(player);
			return;
		}
		
		switch (commandMessage[0]) {//Morph commands
		case "/w": case "/whisper": case "/tell": //Morph all chat commands -> /message
			player.chat(event.getMessage().replaceFirst("(?i)"+ commandMessage[0], "/m"));
			event.setCancelled(true);
			return;
			
		case "/plugins": //Plugins -> /sPlugins
			if (!Util.testPermission(player, "spluginsoverride")) {
				player.chat("/splugins");
				event.setCancelled(true);
				return;
			}
			break;
			
		case "/leave": //Leave -> /gleave
			if (player.getWorld().getName().equals("world_sonic")) {
				event.setCancelled(true);
				player.chat("/gleave");
				return;
			}
			break;
			
		case "/?": //? -> Help
			if (!Util.testPermission(player, "spluginsoverride")) {
				player.chat(event.getMessage().replaceFirst("(?i)" + commandMessage[0], "/help"));
				event.setCancelled(true);
				return;
			}
			break;
			
		case "/region": case "/rg": //Region -> ImprovedRegion
			if (!player.hasPermission("irg.regionoverride")) {
				player.chat(event.getMessage().replaceFirst("(?i)" + commandMessage[0], "/irg"));
				event.setCancelled(true);
				return;
			}
			break;
		case "/ac": case "/helpop": //Whine at them for not using /g or /gc
			if (Util.testPermission(player, "guidechat")) {
				player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&d&l&nSTOP USING &b&l&n/AC &f&l&n| &c&l&nALSO COLORS &f&l&n| &e&l&nUSE /G"));
			}
			break;
			
		case "/modlist": //Modlist -> Stafflist
			player.chat(event.getMessage().replaceFirst("(?i)" + commandMessage[0], "/stafflist"));
			event.setCancelled(true);
			return;
		}
				
		//Set last activity
		playerLogger.setLastActivity(playerName);
		
		//Cancel commands in Jail
		if (jails.isInJail(playerName)) {
			if (commandMessage[0].equalsIgnoreCase("/timeleft")) {
				jails.getJailInfo(player);
				event.setCancelled(true);
				player.sendMessage(ChatColor.GRAY + "You are jailed. Use /timeleft to check your time left in jail.");
			} else if (commandMessage[0].equalsIgnoreCase("/unjail")) {
				if (!player.hasPermission("slaphomebrew.jail")) {
					event.setCancelled(true);
				}
			} else if (!commandMessage[0].equalsIgnoreCase("/modreq") && !commandMessage[0].equalsIgnoreCase("/ping") && !commandMessage[0].equalsIgnoreCase("/timeleft")) {
				if (commandMessage[0].equalsIgnoreCase("/msg") || commandMessage[0].equalsIgnoreCase("/m") || commandMessage[0].equalsIgnoreCase("/message") || commandMessage[0].equalsIgnoreCase("/r") || commandMessage[0].equalsIgnoreCase("/reply")) {
					if (!jails.isAllowedToMsg(playerName)) {
						event.setCancelled(true);
						player.sendMessage(ChatColor.GRAY + "You are jailed. Use /timeleft to check your time left in jail.");
					}
				} else {
					event.setCancelled(true);
					player.sendMessage(ChatColor.GRAY + "You are jailed. Use /timeleft to check your time left in jail.");
				}
			}
		}				
		
		//Leave AFK on certain Commands
		String[] command = event.getMessage().toLowerCase().split(" ");
		if (afk.isAfk(playerName)) {
			switch(command[0]) {
			case "/me": case "/pay": case "/modreq": case "/r": case "/msg": 
				afk.leaveAfk(playerName);
				break;
			}
		}
		
		if (commandMessage.length > 1) {
			switch (commandMessage[0]) {
			//AFK response
			case "/tpa": case "/tpahere":
				Player tempPlayer = Bukkit.getPlayer(commandMessage[1]);
				if (tempPlayer != null) {
					if (afk.isAfk(tempPlayer.getName())){
						afk.sendAfkReason(event.getPlayer(), tempPlayer.getName());
					}
				}
				break;
			}
		}
		
		if (commandMessage.length > 2) {
			if (commandMessage[0].equals("/msg")) { //AFK response
				Player tempPlayer = Bukkit.getPlayer(commandMessage[1]);
				if (tempPlayer != null) {
					if (afk.isAfk(tempPlayer.getName())){
						afk.sendAfkReason(event.getPlayer(), tempPlayer.getName());
					}
				}
			}
		}
		
		//Send commandspy message
		if (!event.isCancelled()) {
			switch (commandMessage[0].substring(1).toLowerCase()) {
			case "roll": case "afk": case "suicide": case "me": case "j": case "jumpto": //Standard ignored
			case "g": case "gc": case "ac": case "helpop": //GuideChat
			case "a": case "amsg": case "mc": //ModChat
			case "x": case "pc": //PotatoChat
				//Ignore these commands
				break;
			case "r": case "reply": case "mail": case "tell": case "t": case "m": case "msg":
				playerLogger.sendToCommandSpies(playerName, event.getMessage(), true); 
				break;
			default:
				playerLogger.sendToCommandSpies(playerName, event.getMessage(), false);
			}
		}
	}
	
}
