package me.naithantu.SlapHomebrew.Commands.Staff;

import me.naithantu.SlapHomebrew.SlapHomebrew;
import me.naithantu.SlapHomebrew.Commands.AbstractCommand;
import me.naithantu.SlapHomebrew.Commands.Exception.CommandException;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public class SearchregionCommand extends AbstractCommand {
	public SearchregionCommand(CommandSender sender, String[] args, SlapHomebrew plugin) {
		super(sender, args, plugin);
	}

	public boolean handle() throws CommandException {
		testPermission("searchregion"); //Test perm
		if (args.length != 1)  return false; //Check usage
		
		String regionname = args[0].toLowerCase();
		if (!plugin.getRegionMap().containsKey(regionname)) throw new CommandException("No results found!"); //Check if there are any results
		
		String[] changes = plugin.getRegionMap().get(regionname).split("<==>"); //Get results
		sender.sendMessage(ChatColor.DARK_AQUA + "Region changes for region " + regionname + ":");
		for (String change : changes) { //Send all
			sender.sendMessage(ChatColor.GOLD + change);
		}
		return true;
	}
}
