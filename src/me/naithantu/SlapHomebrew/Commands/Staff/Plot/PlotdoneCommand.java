package me.naithantu.SlapHomebrew.Commands.Staff.Plot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import me.naithantu.SlapHomebrew.SlapHomebrew;
import me.naithantu.SlapHomebrew.Commands.AbstractCommand;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import com.google.common.base.Joiner;

public class PlotdoneCommand extends AbstractCommand {

	public PlotdoneCommand(CommandSender sender, String[] args, SlapHomebrew plugin) {
		super(sender, args, plugin);
	}

	public boolean handle() {

		if (!testPermission(sender, "plot.admin")) {
			this.noPermission(sender);
			return true;
		}

		if (args.length == 1) {
			sender.sendMessage(ChatColor.RED + "Usage: /plot done [number] <comment>");
		}
		int plotNumber;

		try {
			plotNumber = Integer.parseInt(args[1]);
		} catch (NumberFormatException e) {
			sender.sendMessage(ChatColor.RED + "Usage: /plot done [number] <comment>");
			return true;
		}

		if (plugin.getUnfinishedPlots().contains(plotNumber)) {
			HashMap<Integer, String> plots = plugin.getPlots();
			String plotsInfo = plots.get(plotNumber);
			List<String> message = new ArrayList<String>();
			for (int i = 2; i < args.length; i++) {
				message.add(args[i]);
			}
			String comment = " - ";
			if (!message.isEmpty())
				comment = Joiner.on(" ").join(message);
			plugin.getPlots().put(plotNumber, plotsInfo + "<:>" + "Handled by " + sender.getName() + "<:>" + comment);
		} else {
			sender.sendMessage(ChatColor.RED + "You can not finish that request!");
			return true;
		}
		
		//Remove plot from unfinished plots list.
		List<Integer> unfinishedPlots = plugin.getUnfinishedPlots();
		int index = 0;
		int indexToRemove = 0;
		for (int i : unfinishedPlots) {
			if (i == plotNumber) {
				indexToRemove = index;
			}
			index++;
		}
		unfinishedPlots.remove(indexToRemove);
		sender.sendMessage(ChatColor.GOLD + "Plot Request #" + plotNumber + " was completed by " + sender.getName() + "!");
		return true;
	}
}
