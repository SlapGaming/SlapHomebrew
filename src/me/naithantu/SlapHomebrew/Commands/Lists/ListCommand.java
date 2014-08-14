package me.naithantu.SlapHomebrew.Commands.Lists;

import java.util.ArrayList;
import java.util.TreeSet;

import me.naithantu.SlapHomebrew.Commands.AbstractCommand;
import me.naithantu.SlapHomebrew.Commands.Exception.CommandException;
import me.naithantu.SlapHomebrew.Controllers.AwayFromKeyboard;
import me.naithantu.SlapHomebrew.Util.Util;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import ru.tehkode.permissions.PermissionUser;
import ru.tehkode.permissions.bukkit.PermissionsEx;

public class ListCommand extends AbstractCommand {
	
	public ListCommand(CommandSender sender, String[] args) {
		super(sender, args);
	}

	@Override
	public boolean handle() throws CommandException {
		testPermission("list"); //Test permission
				
		int maxPlayers = plugin.getTabController().getMaxPlayers(); //Get max players
		int nrOfOnlinePlayers = 0;
		TreeSet<String> sortedNames = new TreeSet<>(String.CASE_INSENSITIVE_ORDER); //Create new Sorted List
		
		AwayFromKeyboard afk = plugin.getAwayFromKeyboard(); //Get AFK Controller
		
		for (Player onlinePlayer : plugin.getServer().getOnlinePlayers()) { //Loop thru players & add to sortedset
			nrOfOnlinePlayers++;
			sortedNames.add(onlinePlayer.getName());			
		}
		
		if (nrOfOnlinePlayers == 0) { //No players online -> Console
			throw new CommandException("There are no players online.");
		} else if (nrOfOnlinePlayers == 1) { //Only 1 player online
			if (sender.getName().equals(sortedNames.first())) { //Check if that player is the one executing the command
				hMsg("You are the only person online at the moment!");
				return true;
			}
		}
		
		String[] coloredNames = new String[nrOfOnlinePlayers];
		int x = 0;
		for (String player : sortedNames) { //Loop thru players -> Sorted
			String colorPrefix = "", afkString = ChatColor.WHITE + ""; //Set strings
			PermissionUser user = PermissionsEx.getUser(player); //Get PEX user
			if (user != null) { //Check if exists
				String prefix = user.getPrefix();
				if (prefix != null && prefix.length() > 1) { //Check if has prefix
					colorPrefix = prefix.substring(0, 2);
				}
			}
			
			if (afk.isAfk(player)) afkString += " [AFK]"; //Check if AFK
			coloredNames[x++] = colorPrefix + player + afkString;
		}
		
		//Create new ArrayList for all lines to be send
		ArrayList<String> lines = new ArrayList<>();
		
		//Get for this server
		lines.add("There " + (nrOfOnlinePlayers == 1 ? "is " : "are ") +	ChatColor.GOLD + (nrOfOnlinePlayers) + ChatColor.WHITE + " out of maximum " + ChatColor.GOLD + maxPlayers + ChatColor.WHITE + " players online.");
		lines.add("Players: " + (nrOfOnlinePlayers > 0 ? ChatColor.translateAlternateColorCodes('&', Util.buildString(coloredNames, ", ", 0)) : "No players"));

		//Send lines
		sender.sendMessage(lines.toArray(new String[lines.size()]));
		return true;
	}

	

	
}
