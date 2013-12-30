package me.naithantu.SlapHomebrew.Commands.Staff.ImprovedRegion;

import me.naithantu.SlapHomebrew.SlapHomebrew;
import me.naithantu.SlapHomebrew.Commands.Exception.CommandException;
import me.naithantu.SlapHomebrew.Commands.Exception.UsageException;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldguard.protection.regions.ProtectedPolygonalRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

public class TeleportCommand extends AbstractImprovedRegionCommand {

	public TeleportCommand(SlapHomebrew plugin, Player p, String[] args) {
		super(plugin, p, args);
	}

	@Override
	protected void action() throws CommandException {
		if (args.length < 2) throw new UsageException("irg teleport <Region ID> [sel]" + ChatColor.GRAY + "(will select the region)"); //Usage
		validateRegionID(args[1]);
		ProtectedRegion region = getRegion(args[1]); //Get Region
		BlockVector max = region.getMaximumPoint(); //Get points
		BlockVector min = region.getMinimumPoint();
		int z = (max.getBlockZ() + min.getBlockZ()) / 2; //To ints
		int x = (max.getBlockX() + min.getBlockX()) / 2;
		int y = p.getWorld().getHighestBlockYAt(x, z) + 1; //Get highest block of the loc
		Location topLoc = new Location(p.getWorld(), x, y, z);
		p.teleport(topLoc); //Teleport
		boolean isOutsideRegion = !region.contains(x, y, z); //Check if outside region
		boolean isPolyRegion = region instanceof ProtectedPolygonalRegion; //Check if 
		if (isOutsideRegion || isPolyRegion) { //Check if warnings need to be send
			String warning = ChatColor.GRAY + "Warning:";
			if (isOutsideRegion) warning += " You've been teleported " + (y > max.getBlockY() ? "on top of" : "under") + " the region!";
			if (isPolyRegion) warning += " The region is a poly, this might not be the center.";
			p.sendMessage(warning);
		}
		
		if (args.length > 3) { //Select region
			setPlayerSelection(region);
		}
		
		hMsg("You've been teleported to region " + ChatColor.RED + region.getId());
	}

}
