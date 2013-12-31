package me.naithantu.SlapHomebrew.Commands.Staff.ImprovedRegion;

import me.naithantu.SlapHomebrew.SlapHomebrew;
import me.naithantu.SlapHomebrew.Commands.Exception.CommandException;
import me.naithantu.SlapHomebrew.Commands.Exception.UsageException;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import com.sk89q.worldguard.protection.regions.ProtectedRegion;

public class PriorityCommand extends AbstractImprovedRegionCommand {

	public PriorityCommand(SlapHomebrew plugin, Player p, String[] args) {
		super(p, args);
	}

	@Override
	protected void action() throws CommandException {
		if (args.length != 3) throw new UsageException("irg setpriority <Region ID> <Priority>"); //Usage
		
		//Parse Number
		int priority = parseInt(args[2]);
		
		//Validate RegionID
		validateRegionID(args[1]);
		
		//Get the region
		ProtectedRegion foundRegion = getRegion(args[1]);
		
		//Set the new priority
		foundRegion.setPriority(priority);
		
		//Save changes
		saveChanges();
		
		//Send message
		hMsg("Priority of region '" + foundRegion.getId() + 
				"' has been set to " + priority + "." + ChatColor.GRAY + " (Higher numbers override)");
	}

}
