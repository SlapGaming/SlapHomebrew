package me.naithantu.SlapHomebrew.Commands.VIP;

import java.util.List;

import me.naithantu.SlapHomebrew.Commands.AbstractVipCommand;
import me.naithantu.SlapHomebrew.Commands.Exception.CommandException;
import me.naithantu.SlapHomebrew.Util.Util;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

public class TpallowCommand extends AbstractVipCommand {
	
	public TpallowCommand(CommandSender sender, String[] args) {
		super(sender, args);
	}

	public boolean handle() throws CommandException {
		Player p = getPlayer();
		String playername = p.getName();
		testPermission("tpblock");

		FileConfiguration config = plugin.getConfig();
		if (args.length == 1) {
			String arg = args[0].toLowerCase();
			if (!config.getStringList("tpallow." + playername).contains(arg)) {
				List<String> tempList = config.getStringList("tpallow." + playername);
				tempList.add(arg);
				config.set("tpallow." + playername, tempList);
				hMsg("Added " + args[0] + " to the whitelist!");
				plugin.saveConfig();
			} else {
				List<String> tempList = config.getStringList("tpallow." + playername);
				tempList.remove(arg);
				config.set("tpallow." + playername, tempList);
				hMsg(" Removed " + args[0] + " from the whitelist!");
				plugin.saveConfig();
			}
		} else {
			hMsg("You are currently allowing:");
			hMsg(ChatColor.RED + Util.buildString(config.getStringList("tpallow." + playername), ChatColor.WHITE + ", " + ChatColor.RED));
		}
		return true;
	}
}
