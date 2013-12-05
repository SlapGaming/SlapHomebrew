package me.naithantu.SlapHomebrew.Commands.Basics;

import me.naithantu.SlapHomebrew.SlapHomebrew;
import me.naithantu.SlapHomebrew.Commands.AbstractCommand;
import me.naithantu.SlapHomebrew.Controllers.Lag;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public class LagCommand extends AbstractCommand {
	
	private static Lag lag;

	public LagCommand(CommandSender sender, String[] args, SlapHomebrew plugin) {
		super(sender, args, plugin);
		if (lag == null) {
			lag = plugin.getLag();
		}
	}

	public boolean handle() {
		if (!testPermission(sender, "lag")) {
			this.noPermission(sender);
			return true;
		}
		
		double tps = lag.getTPS();
		StringBuilder builder = new StringBuilder();
		builder.append(ChatColor.YELLOW).append("Server Status: ");
		if (tps >= 17 && tps <= 23) {
			builder.append(ChatColor.GREEN + "All Good!");
		} else if (tps >= 14 && tps <= 26) {
			builder.append(ChatColor.GOLD + "Small Hiccup.");
		} else {
			builder.append(ChatColor.RED + "Struggling.");
		}
		
		builder.append(" (");
		builder.append((double) Math.round(tps * 10) / 10);
		builder.append(" Ticks)");

		sender.sendMessage(builder.toString());
		return true;
	}
}
