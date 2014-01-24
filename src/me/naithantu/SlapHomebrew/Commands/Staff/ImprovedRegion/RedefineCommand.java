package me.naithantu.SlapHomebrew.Commands.Staff.ImprovedRegion;

import me.naithantu.SlapHomebrew.Commands.Exception.CommandException;
import me.naithantu.SlapHomebrew.Commands.Exception.UsageException;
import me.naithantu.SlapHomebrew.Controllers.PlayerLogging.RegionLogger;
import me.naithantu.SlapHomebrew.Controllers.PlayerLogging.RegionLogger.ChangeType;
import me.naithantu.SlapHomebrew.Controllers.PlayerLogging.RegionLogger.ChangerIsA;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion.CircularInheritanceException;

public class RedefineCommand extends AbstractImprovedRegionCommand {

	public RedefineCommand(Player p, String[] args) {
		super(p, args);
	}

	@Override
	protected void action() throws CommandException {
		if (args.length == 1) throw new UsageException("irg redefine <Region ID>");
		
		validateRegionID(args[1]); //Check if valid
		
		ProtectedRegion currentRegion = getRegion(args[1]); //Get the specified region
		
		//Create a new region based on the their selection
		ProtectedRegion newRegion = createRegionFromSelection(args[1]);
		
		//Copy details from old region to the new one
		newRegion.setMembers(currentRegion.getMembers());
		newRegion.setOwners(currentRegion.getOwners());
		newRegion.setFlags(currentRegion.getFlags());
		newRegion.setPriority(currentRegion.getPriority());
		try {
			newRegion.setParent(currentRegion.getParent());
		} catch (CircularInheritanceException e) {} //Exception can be ignored
		
		rm.addRegion(newRegion); //Replace the region
		saveChanges(); //Save
		
		//Give warning about height
        int height = newRegion.getMaximumPoint().getBlockY() - newRegion.getMinimumPoint().getBlockY();
        if (height <= 2) {
            p.sendMessage(ChatColor.GRAY + "Warning: The height of the region was " + (height + 1) + " block(s).");
        }
        
        //Send succesfull message
        hMsg("The region '" + newRegion.getId() + "' has been redefined.");		
        
        //Log
        RegionLogger.logRegionChange(newRegion, p, ChangerIsA.staff, ChangeType.redefine, null);
	}

	

}
