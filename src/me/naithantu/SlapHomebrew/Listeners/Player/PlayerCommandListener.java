package me.naithantu.SlapHomebrew.Listeners.Player;

import java.text.SimpleDateFormat;
import java.util.Date;

import me.naithantu.SlapHomebrew.SlapHomebrew;
import me.naithantu.SlapHomebrew.Controllers.AwayFromKeyboard;
import me.naithantu.SlapHomebrew.Controllers.Jails;
import me.naithantu.SlapHomebrew.Controllers.PlayerLogger;
import me.naithantu.SlapHomebrew.Util.Util;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;


public class PlayerCommandListener implements Listener {

	private SlapHomebrew plugin;
	private AwayFromKeyboard afk;
	private Jails jails;
	private PlayerLogger playerLogger;
	
	public PlayerCommandListener(SlapHomebrew plugin, AwayFromKeyboard afk, Jails jails, PlayerLogger playerLogger){
		this.plugin = plugin;
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
		
		//Morph Whisper -> M
		if (commandMessage[0].equals("/w")) {
			player.chat(event.getMessage().replace("/w", "/m"));
			event.setCancelled(true);
			return;
		} else if (commandMessage[0].equals("/whisper")) {
			player.chat(event.getMessage().replace("/whisper", "/m"));
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
						
		//Catch /modlist command -> Force to /stafflist
		if (commandMessage[0].equalsIgnoreCase("/modlist")) {
			player.chat("/stafflist");
			event.setCancelled(true);
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
			switch (commandMessage[0]) {
			//AFK response
			case "/msg":
				Player tempPlayer = Bukkit.getPlayer(commandMessage[1]);
				if (tempPlayer != null) {
					if (afk.isAfk(tempPlayer.getName())){
						afk.sendAfkReason(event.getPlayer(), tempPlayer.getName());
					}
				}
				break;
				
			//WorldGaurd logger	
			case "/rg": case "/region":
				String date = new SimpleDateFormat("MM-dd HH:mm:ss").format(new Date());
				String action = getActionString(commandMessage[1]);
				if (action != null) {
					if (commandMessage[1].equals("define") && !plugin.getRegionMap().containsKey(commandMessage[2])) {
						plugin.getRegionMap().put(commandMessage[2], date + " " + player.getName() + " made region " + message.replace(getReplaceString(commandMessage[0], commandMessage[1]), ""));
					} else if ( (commandMessage[1].equals("remove") && plugin.getRegionMap().containsKey(commandMessage[2])) || !commandMessage[1].equals("remove")) {
						if(commandMessage[1].equals("addmember") && event.getMessage().contains("flag:") && !Util.testPermission(player, "flag")){
							Util.badMsg(player, "You are not allowed to add member flags!");
							event.setCancelled(true);
						} else {
							String replaceString = getReplaceString(commandMessage[0], commandMessage[1]);
							logWorldGaurd(commandMessage[2], date, player.getName(), action, message, replaceString);
						}
					}
				}
				break;
			}
		}
		
		//Send commandspy message
		if (!event.isCancelled()) {
			String lCmd = commandMessage[0].toLowerCase();
			if (!lCmd.equals("/roll") && !lCmd.equals("/afk") && !lCmd.equals("/suicide") && !lCmd.equals("/me") && !lCmd.equals("/j") && !lCmd.equals("/jumpto") && !lCmd.equals("/ac") && !lCmd.equals("/a") && !lCmd.equals("/amsg") && !lCmd.equals("/helpop")) {
				switch (lCmd) {
				case "/r": case "/reply": case "/mail":	case "/tell": case "/t": case "/m": case "/msg":
					playerLogger.sendToCommandSpies(playerName, message, true); break;
				default:
					playerLogger.sendToCommandSpies(playerName, message, false);
				}
			}
		}
	}
	
	private void logWorldGaurd(String commandMessage, String date, String playerName, String action, String completeMessage, String replaceCommand){
		plugin.getRegionMap().put(commandMessage, plugin.getRegionMap().get(commandMessage) + "<==>" + date + " " + playerName + " " + action + " " + completeMessage.replace(replaceCommand + commandMessage, ""));
	}
	
	private String getReplaceString(String command, String arg){
		String returnString;
		if (command.equals("/rg")) {
			returnString = "/rg " + arg + " ";
		} else {
			returnString = "/region " + arg + " ";
		}
		return returnString;
	}
	
	private String getActionString(String arg){
		String returnString = null;
		switch (arg) {
		case "define": returnString = "made region"; break;
		case "remove": returnString = "removed region";	break;
		case "addowner": returnString = "added owner(s)"; break;
		case "removeowner":	returnString = "removed owner(s)"; break;
		case "addmember": returnString = "added member(s)";	break;
		case "removemember": returnString = "removed member(s)"; break;
		case "flag": returnString = "flagged region"; break;
		case "setpriority":	returnString = "set the priority to"; break; 
		case "redefine": returnString = "redefined region";	break;
		}
		return returnString;
	}
	
	
}
