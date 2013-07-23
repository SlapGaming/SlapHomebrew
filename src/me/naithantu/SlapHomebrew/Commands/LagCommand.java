package me.naithantu.SlapHomebrew.Commands;

import me.naithantu.SlapHomebrew.Lag;
import me.naithantu.SlapHomebrew.SlapHomebrew;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public class LagCommand extends AbstractCommand {
	Lag lag;

	public LagCommand(CommandSender sender, String[] args, SlapHomebrew plugin, Lag lag) {
		super(sender, args, plugin);
		this.lag = lag;
	}

	public boolean handle() {
		if (!testPermission(sender, "lag")) {
			this.noPermission(sender);
			return true;
		}
		
		double tps = lag.getTPS();
		System.out.println("TPS: " + tps);
		StringBuilder builder = new StringBuilder();
		builder.append(ChatColor.YELLOW).append("Ticks per second: ");
		if (tps >= 17 && tps <= 23) {
			builder.append(ChatColor.GREEN);
		} else if (tps >= 14 && tps <= 26) {
			builder.append(ChatColor.GOLD);
		} else {
			builder.append(ChatColor.RED);
		}
		
		builder.append((double) Math.round(tps * 10) / 10).append(" [");
		builder.append(Math.round(tps * 5));
		builder.append("%]");

		sender.sendMessage(builder.toString());
		return true;
	}
}
