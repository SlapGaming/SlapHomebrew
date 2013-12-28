package me.naithantu.SlapHomebrew.Commands.Basics;

import me.naithantu.SlapHomebrew.SlapHomebrew;
import me.naithantu.SlapHomebrew.Commands.AbstractCommand;
import me.naithantu.SlapHomebrew.Commands.Exception.CommandException;
import me.naithantu.SlapHomebrew.Commands.Exception.ErrorMsg;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Boat;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;

public class BoatCommand extends AbstractCommand {

	public BoatCommand(CommandSender sender, String[] args, SlapHomebrew plugin) {
		super(sender, args, plugin);
	}

	public boolean handle() throws CommandException {
		Player player = getPlayer(); //Cast to player
		testPermission("boat"); //Test permission
		
		if (player.isInsideVehicle()) throw new CommandException(ErrorMsg.alreadyInVehicle); //Check if not already in a vehicle

		Location loc = player.getLocation();
		int depth = getWaterDepth(loc); //Get depth
		if (depth > 0 && depth <= 5) {
			final Boat boat = loc.getWorld().spawn(loc.add(0, 1, 0), Boat.class);
			boat.setPassenger(player);			
			boat.setMetadata("slapVehicle", new FixedMetadataValue(plugin, true));
		} else {
			throw new CommandException(ErrorMsg.cannotUseHere);
		}
		return true;
	}

	/**
	 * Get the water depth on a certain location
	 * @param location The location
	 * @return the depth
	 */
	public int getWaterDepth(Location location) {
		int depth = 0;
		Material material = location.getBlock().getType();
		while (material == Material.WATER || material == Material.STATIONARY_WATER) {
			location.add(0, 1, 0);
			depth++;
			if (depth > 5)
				break;
			material = location.getBlock().getType();
		}
		return depth;
	}
}
