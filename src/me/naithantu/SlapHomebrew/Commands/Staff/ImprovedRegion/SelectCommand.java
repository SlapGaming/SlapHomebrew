package me.naithantu.SlapHomebrew.Commands.Staff.ImprovedRegion;

import me.naithantu.SlapHomebrew.SlapHomebrew;
import me.naithantu.SlapHomebrew.Commands.Exception.CommandException;

import org.bukkit.entity.Player;

import com.sk89q.worldguard.protection.regions.ProtectedRegion;

public class SelectCommand extends AbstractImprovedRegionCommand {

	public SelectCommand(SlapHomebrew plugin, Player p, String[] args) {
		super(plugin, p, args);
	}

	@Override
	protected void action() throws CommandException {
		ProtectedRegion region;
		if (args.length == 1) { //No region name given | Find region with highest priority
			region = getHighestPriorityRegion(); //Get highest priority region
		} else {
			validateRegionID(args[1]); //Check if valid region
			region = getRegion(args[1]); //Get region
		}
		setPlayerSelection(region); //Set the selection
	}

}
