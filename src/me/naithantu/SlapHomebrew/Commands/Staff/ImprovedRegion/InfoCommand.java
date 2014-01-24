package me.naithantu.SlapHomebrew.Commands.Staff.ImprovedRegion;

import me.naithantu.SlapHomebrew.Commands.Exception.CommandException;
import me.naithantu.SlapHomebrew.Commands.Exception.IRGException;
import me.naithantu.SlapHomebrew.Commands.Staff.ImprovedRegionCommand.Perm;

import org.bukkit.entity.Player;

import com.sk89q.worldguard.bukkit.commands.RegionPrintoutBuilder;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

public class InfoCommand extends AbstractImprovedRegionCommand {

	private boolean all;
	
	/**
	 * Make a new info command
	 * @param p The player
	 * @param args The args of the command
	 * @param all Is allowed to check all regions
	 * @param own Is allowed to check only their own regions
	 */
	public InfoCommand(Player p, String[] args, boolean all) {
		super(p, args);
		this.all = all;
	}

	@Override
	protected void action() throws CommandException {		
		ProtectedRegion region = null;
		if (args.length == 1 || (args.length == 2 && args[1].equalsIgnoreCase("sel"))) { //No regionname specified. Get highest priority
			if (all) { //Allowed to get all regions
				region = getHighestPriorityRegion();
			} else { //Only allowed to get their own regions
				region = getHighestOwnedPriorityRegion(true);
			}
		} else {
			validateRegionID(args[1]); //Check if valid region
			region = getRegion(args[1]); //Get the region by name
			if (!all) { //If not acces to all regions
				String playername = p.getName();
				if (!region.isMember(playername) && !region.isOwner(playername)) { //Check if owner or member
					throw new IRGException("You are not the owner or member of the specified region.");
				}
			}
		}
		
		if (args[args.length - 1].equalsIgnoreCase("sel")) { //Select region
			if (p.hasPermission(Perm.select.toString())) {
				setPlayerSelection(region);
			}
		}
		
		RegionPrintoutBuilder builder = new RegionPrintoutBuilder(region); //Build the string
		builder.appendRegionInfo();
		builder.send(p); //Send string
	}

}
