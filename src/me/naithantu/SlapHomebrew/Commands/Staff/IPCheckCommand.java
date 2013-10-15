package me.naithantu.SlapHomebrew.Commands.Staff;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map.Entry;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import com.earth2me.essentials.User;
import com.earth2me.essentials.UserMap;

import me.naithantu.SlapHomebrew.SlapHomebrew;
import me.naithantu.SlapHomebrew.Commands.AbstractCommand;
import me.naithantu.SlapHomebrew.Util.Util;

public class IPCheckCommand extends AbstractCommand {

	public IPCheckCommand(CommandSender sender, String[] args, SlapHomebrew plugin) {
		super(sender, args, plugin);
	}

	@Override
	public boolean handle() {
		if (!testPermission(sender, "ipcheck")) {
			noPermission(sender);
			return true;
		}		
		sender.sendMessage(Util.getHeader() + "Checking for double IP's...");
		Util.runASync(plugin, new Runnable() {
			
			@Override
			public void run() {			
				HashMap<String, String> players = new HashMap<>();
				HashMap<String, String> doubles = new HashMap<>();
				
				UserMap uM = plugin.getEssentials().getUserMap();
				for (String player : uM.getAllUniqueUsers()) {
					User u = uM.getUser(player);
					String ip = u.getLastLoginAddress();
					if (players.containsKey(ip)) {
						if (doubles.containsKey(ip)) {
							doubles.put(ip, doubles.get(ip) + ", " + u.getName());
						} else {
							doubles.put(ip, players.get(ip) + ", " + u.getName());
						}
					} else {
						players.put(ip, u.getName());
					}
				}
				boolean found = false;
				try {
					FileWriter fW = new FileWriter(plugin.getDataFolder() + "ips.txt");
					PrintWriter out = new PrintWriter(fW);
					for (Entry<String, String> entry : doubles.entrySet()) {
						out.println(entry.getKey() + " | Accounts: " + entry.getValue());
					}
					out.close();
				} catch (IOException e) {
					sender.sendMessage("Failed to write to file.");
				} finally {
					sender.sendMessage("Done checking IP's.");
				}
				
				for (Entry<String, String> entry : doubles.entrySet()) {
					found = true;
					sender.sendMessage(ChatColor.GRAY + entry.getKey() + " | Accounts: " + entry.getValue());
				}
				if (!found) {
					sender.sendMessage(Util.getHeader() + "Done checking IP's. Nothing found.");
				}
			}
		});
			
		return true;
	}

}
