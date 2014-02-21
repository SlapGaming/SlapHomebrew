package me.naithantu.SlapHomebrew.Commands.Homes;

import java.util.List;

import me.naithantu.SlapHomebrew.Commands.AbstractCommand;
import me.naithantu.SlapHomebrew.Commands.Exception.CommandException;
import me.naithantu.SlapHomebrew.Controllers.Homes;
import me.naithantu.SlapHomebrew.PlayerExtension.SlapPlayer;
import me.naithantu.SlapHomebrew.Util.Util;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public class SetHomeCommand extends AbstractCommand {

	public SetHomeCommand(CommandSender sender, String[] args) {
		super(sender, args);
	}

	@Override
	public boolean handle() throws CommandException {
		SlapPlayer p = getSlapPlayer();
		testPermission("sethome");
		
		//Get homes
		Homes homeControl = plugin.getHomes();
		List<String> homes = homeControl.getHomes(p.getName());
		int currentHomes = homes.size();
		int allowedHomes = homeControl.getTotalNumberOfHomes(p.getName());
		
		if (currentHomes > allowedHomes) { //Check if not already over their limit
			throw new CommandException("You have more homes than you're allowed to set. You cannot set anymore homes.");
		}
		
		//Parse homename
		String homename;
		if (args.length > 0) {
			homename = Util.sanitizeYamlString(args[0]);
		} else {
			homename = "home";
		}
		
		//Check if already a home set with this name
		boolean alreadySet = homes.contains(homename);
		
		//Check if trying to set more homes than allowed
		if (currentHomes == allowedHomes && !alreadySet) {
			throw new CommandException("You already have already the maximum number of homes (" + allowedHomes + ").");
		}
		
		homeControl.setHome(p.getName(), homename, p.getLocation()); //Set home
		hMsg("Home set with name: " + ChatColor.GREEN + homename);
		return true;
	}

}
