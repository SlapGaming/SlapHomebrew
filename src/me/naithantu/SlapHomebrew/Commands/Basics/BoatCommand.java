package me.naithantu.SlapHomebrew.Commands.Basics;

import me.naithantu.SlapHomebrew.SlapHomebrew;
import me.naithantu.SlapHomebrew.Commands.AbstractCommand;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Boat;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;

public class BoatCommand extends AbstractCommand {

	public BoatCommand(CommandSender sender, String[] args, SlapHomebrew plugin) {
		super(sender, args, plugin);
	}

	public boolean handle() {
		if (!(sender instanceof Player)) {
			this.badMsg(sender, "You need to be in-game to do that.");
			return true;
		}

		final Player player = (Player) sender;
		if (!testPermission(player, "boat")) {
			this.noPermission(sender);
			return true;
		}

		if (player.isInsideVehicle()) {
			this.badMsg(sender, "You are already in a vehicle.");
			return true;
		}

		World w = player.getWorld();
		int depth = getWaterDepth(player.getLocation());
		if (depth > 0 && depth <= 5) {
			final Boat boat = w.spawn(player.getLocation().add(0, 1, 0), Boat.class);
			boat.setPassenger(player);			
			boat.setMetadata("slapVehicle", new FixedMetadataValue(plugin, true));
		} else {
			this.badMsg(sender, "You can not use that here!");
		}
		return true;
	}

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
