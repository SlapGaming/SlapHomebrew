package me.naithantu.SlapHomebrew.Commands.Fun;

import me.naithantu.SlapHomebrew.SlapHomebrew;
import me.naithantu.SlapHomebrew.Commands.AbstractCommand;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class SpartaCommand extends AbstractCommand {

	public SpartaCommand(CommandSender sender, String[] args, SlapHomebrew plugin) {
		super(sender, args, plugin);
	}

	public boolean handle() {
		if (!testPermission(sender, "sparta")) {
			this.noPermission(sender);
			return true;
		}

		if (args.length == 0)
			return false;

		final Player player = plugin.getServer().getPlayer(args[0]);
		if (player == null) {
			this.badMsg(sender, "Player not found.");
			return true;
		}

		int multiplier = -2;

		if (args.length > 1) {
			try {
				multiplier = -Integer.parseInt(args[1]);
			} catch (NumberFormatException e) {
			}
		}

		this.msg(sender, "Sparta'd " + player.getName());
		
		final int finalMultiplier = multiplier;
		
		player.sendMessage(ChatColor.RED + "THIS");
		plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
			@Override
			public void run() {
				player.sendMessage(ChatColor.RED + "IS");
				plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
					@Override
					public void run() {
						player.sendMessage(ChatColor.RED + "SPARTA!!!");
						double yaw = ((player.getLocation().getYaw() + 90) * Math.PI) / 180;
						double x = Math.cos(yaw);
						double z = Math.sin(yaw);
						Vector vector = new Vector(x, -0.2, z).multiply(finalMultiplier);
						player.setVelocity(vector);
					}
				}, 20);
			}
		}, 20);
		return true;
	}
}
