package me.naithantu.SlapHomebrew.Commands.Staff.Plot;

import me.naithantu.SlapHomebrew.SlapHomebrew;
import me.naithantu.SlapHomebrew.Commands.AbstractCommand;
import me.naithantu.SlapHomebrew.Commands.Exception.CommandException;
import me.naithantu.SlapHomebrew.Commands.Exception.UsageException;

import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class PlottpCommand extends AbstractCommand {

	public PlottpCommand(CommandSender sender, String[] args, SlapHomebrew plugin) {
		super(sender, args, plugin);
	}

	public boolean handle() throws CommandException {
		Player player = getPlayer(); //Cast to player
		testPermission("plot.mod"); //Test perm
		if (args.length < 2) throw new UsageException("plot tp <number>"); //Check usage
		
		int plotNumber = parseInt(args[1]);
		if (plugin.getPlots().size() < plotNumber || plotNumber < 1) throw new CommandException("No plot mark found with this ID."); //Check if correct ID
		
		player.teleport(stringToLocation(plotNumber)); //TP player
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
