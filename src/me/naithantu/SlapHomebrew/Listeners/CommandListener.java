package me.naithantu.SlapHomebrew.Listeners;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import me.naithantu.SlapHomebrew.Jail;
import me.naithantu.SlapHomebrew.SlapHomebrew;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

public class CommandListener implements Listener {

	@EventHandler
	public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
		Player player = event.getPlayer();
		String message = event.getMessage().toLowerCase().trim();
		String[] commandMessage = message.split(" ");
		if (commandMessage[0].equalsIgnoreCase("/tjail") || commandMessage[0].equalsIgnoreCase("/jail") || commandMessage[0].equalsIgnoreCase("/togglejail")) {
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
						event.setCancelled(true);
					}
				}
			}
		}
		
		if (commandMessage[0].equalsIgnoreCase("/jails")) {
			if (player.hasPermission("slaphomebrew.jails") && player.hasPermission("essentials.togglejail")) {
				player.sendMessage(ChatColor.GRAY + "one two three");
				event.setCancelled(true);
			}
		}
		
		
		
		//Worldguard logger.
		if (commandMessage.length < 3)
			return;
		if (commandMessage[0].equals("/rg") || commandMessage[0].equals("/region")) {
			DateFormat cmdDate = new SimpleDateFormat("MM-dd HH:mm:ss");
			Date date = new Date();
			if (commandMessage[1].equals("define")) {
				if (!SlapHomebrew.worldGuard.containsKey(commandMessage[2])) {
					if (commandMessage[0].equals("/rg"))
						SlapHomebrew.worldGuard.put(commandMessage[2], cmdDate.format(date) + " " + player.getName() + " made region " + message.replace("/rg define ", ""));
					else
						SlapHomebrew.worldGuard.put(commandMessage[2], cmdDate.format(date) + " " + player.getName() + " made region " + message.replace("/region define ", ""));
				} else {
					if (commandMessage[0].equals("/rg"))
						SlapHomebrew.worldGuard.put(commandMessage[2], SlapHomebrew.worldGuard.get(commandMessage[2]) + "<==>" + cmdDate.format(date) + " " + player.getName() + " made region "
								+ message.replace("/rg define ", ""));
					else
						SlapHomebrew.worldGuard.put(commandMessage[2], SlapHomebrew.worldGuard.get(commandMessage[2]) + "<==>" + cmdDate.format(date) + " " + player.getName() + " made region "
								+ message.replace("/region define ", ""));
				}
			} else if (commandMessage[1].equals("remove")) {
				if (SlapHomebrew.worldGuard.containsKey(commandMessage[2])) {
					if (commandMessage[0].equals("/rg"))
						SlapHomebrew.worldGuard.put(commandMessage[2], SlapHomebrew.worldGuard.get(commandMessage[2]) + "<==>" + cmdDate.format(date) + " " + player.getName() + " removed region "
								+ message.replace("/rg remove ", ""));
					else
						SlapHomebrew.worldGuard.put(commandMessage[2], SlapHomebrew.worldGuard.get(commandMessage[2]) + "<==>" + cmdDate.format(date) + " " + player.getName() + " removed region "
								+ message.replace("/region remove ", ""));
				}
			} else if (commandMessage[1].equals("addowner")) {
				if (commandMessage[0].equals("/rg"))
					SlapHomebrew.worldGuard.put(commandMessage[2], SlapHomebrew.worldGuard.get(commandMessage[2]) + "<==>" + cmdDate.format(date) + " " + player.getName() + " added owner(s)"
							+ message.replace("/rg addowner " + commandMessage[2], ""));
				else
					SlapHomebrew.worldGuard.put(commandMessage[2], SlapHomebrew.worldGuard.get(commandMessage[2]) + "<==>" + cmdDate.format(date) + " " + player.getName() + " added owner(s)"
							+ message.replace("/region addowner " + commandMessage[2], ""));
			} else if (commandMessage[1].equals("removeowner")) {
				if (commandMessage[0].equals("/rg"))
					SlapHomebrew.worldGuard.put(commandMessage[2], SlapHomebrew.worldGuard.get(commandMessage[2]) + "<==>" + cmdDate.format(date) + " " + player.getName() + " removed owner(s)"
							+ message.replace("/rg removeowner " + commandMessage[2], ""));
				else
					SlapHomebrew.worldGuard.put(commandMessage[2], SlapHomebrew.worldGuard.get(commandMessage[2]) + "<==>" + cmdDate.format(date) + " " + player.getName() + " removed owner(s)"
							+ message.replace("/region removeowner " + commandMessage[2], ""));

			} else if (commandMessage[1].equals("addmember")) {
				if (commandMessage[0].equals("/rg"))
					SlapHomebrew.worldGuard.put(commandMessage[2], SlapHomebrew.worldGuard.get(commandMessage[2]) + "<==>" + cmdDate.format(date) + " " + player.getName() + " added member(s)"
							+ message.replace("/rg addmember " + commandMessage[2], ""));
				else
					SlapHomebrew.worldGuard.put(commandMessage[2], SlapHomebrew.worldGuard.get(commandMessage[2]) + "<==>" + cmdDate.format(date) + " " + player.getName() + " added member(s)"
							+ message.replace("/region addmember " + commandMessage[2], ""));

			} else if (commandMessage[1].equals("removemember")) {
				if (commandMessage[0].equals("/rg"))
					SlapHomebrew.worldGuard.put(commandMessage[2], SlapHomebrew.worldGuard.get(commandMessage[2]) + "<==>" + cmdDate.format(date) + " " + player.getName() + " removed member(s)"
							+ message.replace("/rg removemember " + commandMessage[2], ""));
				else
					SlapHomebrew.worldGuard.put(commandMessage[2], SlapHomebrew.worldGuard.get(commandMessage[2]) + "<==>" + cmdDate.format(date) + " " + player.getName() + " removed member(s)"
							+ message.replace("/region removemember " + commandMessage[2], ""));

			} else if (commandMessage[1].equals("flag")) {
				if (commandMessage[0].equals("/rg"))
					SlapHomebrew.worldGuard.put(commandMessage[2], SlapHomebrew.worldGuard.get(commandMessage[2]) + "<==>" + cmdDate.format(date) + " " + player.getName() + " flagged region"
							+ message.replace("/rg flag " + commandMessage[2], ""));
				else
					SlapHomebrew.worldGuard.put(commandMessage[2], SlapHomebrew.worldGuard.get(commandMessage[2]) + "<==>" + cmdDate.format(date) + " " + player.getName() + " flagged region"
							+ message.replace("/region flag " + commandMessage[2], ""));

			} else if (commandMessage[1].equals("setpriority")) {
				if (commandMessage[0].equals("/rg"))
					SlapHomebrew.worldGuard.put(commandMessage[2], SlapHomebrew.worldGuard.get(commandMessage[2]) + "<==>" + cmdDate.format(date) + " " + player.getName() + " set the priority to"
							+ message.replace("/rg setpriority " + commandMessage[2], ""));
				else
					SlapHomebrew.worldGuard.put(commandMessage[2], SlapHomebrew.worldGuard.get(commandMessage[2]) + "<==>" + cmdDate.format(date) + " " + player.getName() + " set the priority to"
							+ message.replace("/region setpriority " + commandMessage[2], ""));
			} else if (commandMessage[1].equals("redefine")) {
				if (commandMessage[0].equals("/rg"))
					SlapHomebrew.worldGuard.put(commandMessage[2], SlapHomebrew.worldGuard.get(commandMessage[2]) + "<==>" + cmdDate.format(date) + " " + player.getName() + " redefined region"
							+ message.replace("/rg redefine " + commandMessage[2], ""));
				else
					SlapHomebrew.worldGuard.put(commandMessage[2], SlapHomebrew.worldGuard.get(commandMessage[2]) + "<==>" + cmdDate.format(date) + " " + player.getName() + " redefined region"
							+ message.replace("/region redefine " + commandMessage[2], ""));
			}
		}
	}
}
