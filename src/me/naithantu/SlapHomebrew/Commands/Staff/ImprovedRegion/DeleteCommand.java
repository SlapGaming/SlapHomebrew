package me.naithantu.SlapHomebrew.Commands.Staff.ImprovedRegion;

import me.naithantu.SlapHomebrew.SlapHomebrew;
import me.naithantu.SlapHomebrew.Commands.Exception.CommandException;
import me.naithantu.SlapHomebrew.Commands.Exception.UsageException;

import org.bukkit.entity.Player;

import com.sk89q.worldguard.protection.regions.ProtectedRegion;

public class DeleteCommand extends AbstractImprovedRegionCommand {

	public DeleteCommand(SlapHomebrew plugin, Player p, String[] args) {
		super(plugin, p, args);
	}

	@Override
	protected void action() throws CommandException {
		if (args.length < 2) throw new UsageException("irg delete <Region ID>"); //Usage
		validateRegionID(args[1]); //Check if valid
		ProtectedRegion region = getRegion(args[1]); //Get region
		rm.removeRegion(region.getId()); //Remove region
		saveChanges(); //Save
		hMsg("Region '" + region.getId() + "' deleted."); //Msg
	}

}
