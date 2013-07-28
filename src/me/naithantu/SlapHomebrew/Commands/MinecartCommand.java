package me.naithantu.SlapHomebrew.Commands;

import me.naithantu.SlapHomebrew.SlapHomebrew;

import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.util.Vector;

public class MinecartCommand extends AbstractCommand {

	public MinecartCommand(CommandSender sender, String[] args, SlapHomebrew plugin) {
		super(sender, args, plugin);
	}

	public boolean handle() {
		if(!(sender instanceof Player)){
			this.badMsg(sender, "You need to be in-game to do that.");
			return true;
		}
		
		Player player = (Player) sender;
		if (!testPermission(player, "minecart")) {
			this.noPermission(sender);
			return true;
		}
		
		if(player.isInsideVehicle()){
			this.badMsg(sender, "You are already in a vehicle.");
			return true;
		}

		World w = player.getWorld();
		int railBlock = w.getBlockTypeIdAt(player.getLocation());
		if (railBlock == 66 || railBlock == 27 || railBlock == 28) {
			Minecart minecart = w.spawn(player.getLocation(), Minecart.class);
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
		}
		return true;
	}
}
