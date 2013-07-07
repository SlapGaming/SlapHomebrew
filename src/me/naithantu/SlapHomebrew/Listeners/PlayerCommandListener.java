package me.naithantu.SlapHomebrew.Listeners;

import java.text.SimpleDateFormat;
import java.util.Date;

import me.naithantu.SlapHomebrew.AwayFromKeyboard;
import me.naithantu.SlapHomebrew.Jail;
import me.naithantu.SlapHomebrew.SlapHomebrew;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import com.earth2me.essentials.User;

public class PlayerCommandListener implements Listener {

	private AwayFromKeyboard afk;
	private SlapHomebrew plugin;
	
	public PlayerCommandListener(SlapHomebrew plugin, AwayFromKeyboard afk){
		this.afk = afk;
		this.plugin = plugin;
	}
	
	@EventHandler
	public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
		Player player = event.getPlayer();
		String message = event.getMessage().toLowerCase().trim();
		String[] commandMessage = message.split(" ");
		if (commandMessage[0].equalsIgnoreCase("/tjail") || commandMessage[0].equalsIgnoreCase("/jail") || commandMessage[0].equalsIgnoreCase("/togglejail")) {
			boolean hasBeenJailed = true;
			if (!player.hasPermission("slaphomebrew.longjail") && player.hasPermission("essentials.togglejail")) {
				//Check the number of args, to not block usage messages.
				if (commandMessage.length > 3) {
					String time = "";
					int i = 0;
					for (String string : commandMessage) {
						if (i > 2)
							time += string + " ";
						i++;
					}
					Jail jail = new Jail();
					if (!jail.testJail(time)) {
						player.sendMessage(ChatColor.RED + "You may not jail someone for that long!");
						hasBeenJailed = false;
						event.setCancelled(true);
					}
				}
			}
			if(hasBeenJailed && Bukkit.getServer().getPlayer(commandMessage[1]) != null && commandMessage.length > 3)
				Bukkit.getServer().broadcastMessage(ChatColor.GOLD + "[SLAP] " + ChatColor.WHITE + Bukkit.getServer().getPlayer(commandMessage[1]).getName() + " has been jailed.");
		}
		
		if (commandMessage[0].equalsIgnoreCase("/jails")) {
			if (player.hasPermission("slaphomebrew.jails") && player.hasPermission("essentials.togglejail")) {
				player.sendMessage(ChatColor.GRAY + "one two three");
				event.setCancelled(true);
			}
		}
		
		//Cancel commands in Jail
		User targetUser = plugin.getEssentials().getUserMap().getUser(player.getName());
		if (targetUser.isJailed()) {
			if (!commandMessage[0].equalsIgnoreCase("/modreq")) {
				event.setCancelled(true);
				targetUser.sendMessage(ChatColor.RED + "You are still in jail for: " + targetUser.getJailTimeout());
			}
		}
		
		//Leave AFK on certain Commands
		String playerName = player.getName();
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
					if (commandMessage[1].equals("define") && !SlapHomebrew.worldGuard.containsKey(commandMessage[2])) {
						SlapHomebrew.worldGuard.put(commandMessage[2], date + " " + player.getName() + " made region " + message.replace(getReplaceString(commandMessage[0], commandMessage[1]), ""));
					} else if ( (commandMessage[1].equals("remove") && SlapHomebrew.worldGuard.containsKey(commandMessage[2])) || !commandMessage[1].equals("remove")) {
						String replaceString = getReplaceString(commandMessage[0], commandMessage[1]);
						logWorldGaurd(commandMessage[2], date, player.getName(), action, message, replaceString);
					}
				}
				break;
			}
		}
	}
	
	private void logWorldGaurd(String commandMessage, String date, String playerName, String action, String completeMessage, String replaceCommand){
		SlapHomebrew.worldGuard.put(commandMessage, SlapHomebrew.worldGuard.get(commandMessage) + "<==>" + date + " " + playerName + " " + action + " " + completeMessage.replace(replaceCommand + commandMessage, ""));
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
