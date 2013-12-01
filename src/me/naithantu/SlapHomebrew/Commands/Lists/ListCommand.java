package me.naithantu.SlapHomebrew.Commands.Lists;

import java.util.HashMap;
import java.util.SortedSet;
import java.util.TreeSet;

import me.naithantu.SlapHomebrew.SlapHomebrew;
import me.naithantu.SlapHomebrew.Commands.AbstractCommand;
import me.naithantu.SlapHomebrew.Controllers.AwayFromKeyboard;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import ru.tehkode.permissions.PermissionUser;
import ru.tehkode.permissions.bukkit.PermissionsEx;

public class ListCommand extends AbstractCommand {

	private static AwayFromKeyboard afk = null;
	
	private SortedSet<String> set;
	private HashMap<String, String> fullName;
	
	public ListCommand(CommandSender sender, String[] args, SlapHomebrew plugin) {
		super(sender, args, plugin);
		set = new TreeSet<String>();
		fullName = new HashMap<>();
		if (afk == null) {
			afk = plugin.getAwayFromKeyboard();
		}
	}

	@Override
	public boolean handle() {
		if (!testPermission(sender, "list")) {
			noPermission(sender);
			return true;
		}
		
		int maxPlayers = plugin.getTabController().getMaxPlayers();
		int nrOfOnlinePlayers = 0;
		
		for (Player onlinePlayer : plugin.getServer().getOnlinePlayers()) {
			nrOfOnlinePlayers++;
			String playerName = onlinePlayer.getName();
			String playerNameL = onlinePlayer.getName().toLowerCase();
			PermissionUser pexUser = PermissionsEx.getUser(playerName);
			String colorPrefix = "";
			if (pexUser != null) {
				String prefix = pexUser.getPrefix();
				if (prefix != null);
				colorPrefix = ChatColor.translateAlternateColorCodes('&', prefix.substring(0, 2));
			}
			String afkString = "";
			if (afk.isAfk(playerName)) afkString = ChatColor.WHITE + "[AFK]";
			fullName.put(playerNameL, colorPrefix + playerName + afkString);
			set.add(playerNameL);
		}
		
		if (nrOfOnlinePlayers == 0) {
			sender.sendMessage(ChatColor.RED + "There are no players online.");
		} else {
			String firstline;
			if (nrOfOnlinePlayers == 1) {
				firstline = "There is " + ChatColor.GOLD + "1" + ChatColor.WHITE + " out of maximum " + ChatColor.GOLD + maxPlayers + ChatColor.WHITE + " players online.";
			} else {
				firstline = "There are " + ChatColor.GOLD + nrOfOnlinePlayers + ChatColor.WHITE + " out of maximum " + ChatColor.GOLD + maxPlayers + ChatColor.WHITE + " players online.";
			}
			boolean first = true;
			String secondLine = "Players: ";
			for (String playerName : set) {
				if (first) {
					first = false;
					secondLine = secondLine + fullName.get(playerName);
				} else {
					secondLine = secondLine + ChatColor.WHITE +  ", " + fullName.get(playerName);
				}
			}
			sender.sendMessage(new String[] {firstline, secondLine});
		}
		return true;
	}

	

	
}
