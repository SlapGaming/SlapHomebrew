package me.naithantu.SlapHomebrew.Commands.Staff.ImprovedRegion;

import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import me.naithantu.SlapHomebrew.Commands.Exception.CommandException;
import me.naithantu.SlapHomebrew.Commands.Exception.UsageException;
import me.naithantu.SlapHomebrew.Controllers.PlayerLogging.RegionLogger;
import me.naithantu.SlapHomebrew.Controllers.PlayerLogging.RegionLogger.ChangeType;
import me.naithantu.SlapHomebrew.Controllers.PlayerLogging.RegionLogger.ChangerIsA;
import org.bukkit.entity.Player;

public class DeleteCommand extends AbstractImprovedRegionCommand {

	public DeleteCommand(Player p, String[] args) {
		super(p, args);
	}

	@Override
	protected void action() throws CommandException {
		if (args.length < 2) throw new UsageException("irg delete <Region ID>"); //Usage
		validateRegionID(args[1]); //Check if valid
		ProtectedRegion region = getRegion(args[1]); //Get region
		rm.removeRegion(region.getId()); //Remove region
		
		//Save & Msg
		saveChanges();
		hMsg("Region '" + region.getId() + "' deleted.");
		
		//Log
		RegionLogger.logRegionChange(region, p, ChangerIsA.staff, ChangeType.remove, null);
	}

}
