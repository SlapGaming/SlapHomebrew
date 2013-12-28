package me.naithantu.SlapHomebrew.Commands.Basics;

import me.naithantu.SlapHomebrew.SlapHomebrew;
import me.naithantu.SlapHomebrew.Commands.AbstractCommand;
import me.naithantu.SlapHomebrew.Commands.Exception.CommandException;
import me.naithantu.SlapHomebrew.Commands.Exception.ErrorMsg;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.util.Vector;

public class MinecartCommand extends AbstractCommand {

	public MinecartCommand(CommandSender sender, String[] args, SlapHomebrew plugin) {
		super(sender, args, plugin);
	}

	public boolean handle() throws CommandException {
		Player player = getPlayer(); //Cast to player
		testPermission("minecart"); //Test permission
		
		if(player.isInsideVehicle()) throw new CommandException(ErrorMsg.alreadyInVehicle); //Check if in vehcile

		Location loc = player.getLocation();
		Material blockType = loc.getBlock().getType();
		if (blockType == Material.ACTIVATOR_RAIL || blockType == Material.POWERED_RAIL || blockType == Material.RAILS || blockType == Material.DETECTOR_RAIL) {
			Minecart minecart = loc.getWorld().spawn(loc, Minecart.class);
			minecart.setPassenger(player);
			minecart.setMetadata("slapVehicle", new FixedMetadataValue(plugin, true));
			Vector v = minecart.getVelocity();
			double degreeRotation = (player.getLocation().getYaw() - 90.0F) % 360.0F;
			if (degreeRotation < 0.0D) {
				degreeRotation += 360.0D;
			}
			if (degreeRotation <= 45.0D || degreeRotation > 315.0D) {
				v.setX(-7);
			}
			if (degreeRotation > 45.0D && degreeRotation <= 135.0D) {
				v.setZ(-7);
			}
			if (degreeRotation > 135.0D && degreeRotation <= 225.0D) {
				v.setX(7);
			}
			if (degreeRotation > 225.0D && degreeRotation <= 315.0D) {
				v.setZ(7);
			}
			minecart.setVelocity(v);
		} else {
			throw new CommandException(ErrorMsg.cannotUseHere);
		}
		return true;
	}
}
