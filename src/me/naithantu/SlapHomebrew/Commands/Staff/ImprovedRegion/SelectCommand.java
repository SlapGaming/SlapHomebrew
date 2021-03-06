package me.naithantu.SlapHomebrew.Commands.Staff.ImprovedRegion;

import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import me.naithantu.SlapHomebrew.Commands.Exception.CommandException;
import org.bukkit.entity.Player;

public class SelectCommand extends AbstractImprovedRegionCommand {

	public SelectCommand(Player p, String[] args) {
		super(p, args);
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
