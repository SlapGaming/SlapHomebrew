package me.naithantu.SlapHomebrew.Commands;

import me.naithantu.SlapHomebrew.SlapHomebrew;

import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class MinecartCommand extends AbstractCommandHandler {
	CommandSender sender;
	String[] args;

	public MinecartCommand(CommandSender sender, String[] args) {
		this.sender = sender;
		this.args = args;
	}

	public boolean handle() {
		if(!(sender instanceof Player)){
			this.badMsg(sender, "You need to be in-game to do that.");
			return true;
		}
		
		Player player = (Player) sender;
		if (!player.hasPermission("slaphomebrew.minecart")) {
			this.noPermission(sender);
			return true;
		}

		World w = player.getWorld();
		int railBlock = w.getBlockTypeIdAt(player.getLocation());
		if (railBlock == 66 || railBlock == 27 || railBlock == 28) {
			Minecart m = w.spawn(player.getLocation(), Minecart.class);
			m.setPassenger(player);
			SlapHomebrew.mCarts.add(m.getUniqueId());
			Vector v = m.getVelocity();
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
			m.setVelocity(v);
		}
		return true;
	}
}
