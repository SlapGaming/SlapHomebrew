package me.naithantu.SlapHomebrew.Commands.Basics;

import me.naithantu.SlapHomebrew.Commands.AbstractCommand;
import me.naithantu.SlapHomebrew.Commands.Exception.CommandException;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public class LagCommand extends AbstractCommand {

	public LagCommand(CommandSender sender, String[] args) {
		super(sender, args);
	}

	public boolean handle() throws CommandException {
		testPermission("lag");
		
		double tps = plugin.getLag().getTPS();
		String status = ChatColor.YELLOW + "Server Status: ";
		
		if (tps >= 17 && tps <= 23) {
			status += ChatColor.GREEN + "All Good!";
		} else if (tps >= 14 && tps <= 26) {
			status += ChatColor.GOLD + "Small Hiccup.";
		} else {
			status += ChatColor.RED + "Struggling.";
		}
		
		status += " (" + ((double) Math.round(tps * 10) / 10) + " Ticks)";
		
		msg(status);
		return true;
	}
}
