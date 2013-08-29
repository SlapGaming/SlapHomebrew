package me.naithantu.SlapHomebrew.Commands.Staff.Plot;

import me.naithantu.SlapHomebrew.SlapHomebrew;
import me.naithantu.SlapHomebrew.Commands.AbstractCommand;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class PlottpCommand extends AbstractCommand {

	public PlottpCommand(CommandSender sender, String[] args, SlapHomebrew plugin) {
		super(sender, args, plugin);
	}

	public boolean handle() {
		if(!(sender instanceof Player)){
			this.badMsg(sender, "You need to be in-game to do that.");
			return true;
		}
		
		Player player = (Player) sender;
		if (!testPermission(player, "plot.mod")) {
			this.noPermission(sender);
			return true;
		}


		if (args.length < 2) {
			player.sendMessage(ChatColor.RED + "Usage: /plot tp <number>");
			return true;
		}
		int plotNumber;
		try {
			plotNumber = Integer.parseInt(args[1]);
		} catch (NumberFormatException e) {
			player.sendMessage(ChatColor.RED + "Usage: /plot tp [number]");
			return true;
		}
		if (plugin.getPlots().size() + 1 > plotNumber) {
			player.teleport(stringToLocation(plotNumber));
		} else {
			player.sendMessage(ChatColor.RED + "Plot request not found!");
		}
		return true;
	}
	
	public Location stringToLocation(int plotNumber) {
		String[] plotInfo = plugin.getPlots().get(plotNumber).split("<:>");
		String[] locationInfo = plotInfo[0].split(":");
		Location location = new Location(plugin.getServer().getWorld(locationInfo[0]), Double.valueOf(locationInfo[1]), Double.valueOf(locationInfo[2]), Double.valueOf(locationInfo[3]),
				Float.valueOf(locationInfo[4]), Float.valueOf(locationInfo[5]));
		return location;
	}
}
