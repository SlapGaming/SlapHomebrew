package me.naithantu.SlapHomebrew.Commands.VIP;

import me.naithantu.SlapHomebrew.Commands.AbstractVipCommand;
import me.naithantu.SlapHomebrew.Commands.Exception.CommandException;
import me.naithantu.SlapHomebrew.PlayerExtension.SlapPlayer;
import me.naithantu.SlapHomebrew.Util.Util;

import org.bukkit.Location;
import org.bukkit.command.CommandSender;

public class BackdeathCommand extends AbstractVipCommand {

	public BackdeathCommand(CommandSender sender, String[] args) {
		super(sender, args);
	}

	public boolean handle() throws CommandException {
		SlapPlayer player = getSlapPlayer();
		testPermission("backdeath");
		
		//Get death location
		Location deathLoc = player.getDeathLocation();
		
		//Check if death location is set
		if (deathLoc == null) {
			throw new CommandException("There is nothing to go back to.");
		}
		
		//Msg & Teleport
		hMsg("You have been teleported to your death location!");
		Util.safeTeleport(player.p(), deathLoc, true, true);
		
		//Reset death location
		player.setDeathLocation(null);
		return true;
	}
}
