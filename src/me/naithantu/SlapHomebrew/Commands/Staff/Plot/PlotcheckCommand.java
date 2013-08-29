package me.naithantu.SlapHomebrew.Commands.Staff.Plot;

import java.util.List;

import me.naithantu.SlapHomebrew.SlapHomebrew;
import me.naithantu.SlapHomebrew.Commands.AbstractCommand;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public class PlotcheckCommand extends AbstractCommand {

	public PlotcheckCommand(CommandSender sender, String[] args, SlapHomebrew plugin) {
		super(sender, args, plugin);
	}

	public boolean handle() {
		if (!testPermission(sender, "plot.mod")) {
			this.noPermission(sender);
			return true;
		}


		if (args.length == 1) {
			List<Integer> unfinishedPlots = plugin.getUnfinishedPlots();
			sender.sendMessage(ChatColor.AQUA + "--------- " + unfinishedPlots.size() + " Plots Waiting To Be Cleared ---------");
			for (int plotNumber : unfinishedPlots) {
				sender.sendMessage(sendPlotInfo(plotNumber));
			}
		} else if (args.length == 2) {
			int plotNumber;
			try {
				plotNumber = Integer.parseInt(args[1]);
			} catch (NumberFormatException e) {
				sender.sendMessage(ChatColor.RED + "Usage: /plot check [number]");
				return true;
			}
			if (plugin.getUnfinishedPlots().contains(plotNumber)) {
				sender.sendMessage(sendPlotInfo(plotNumber));
			} else {
				if (plugin.getPlots().size() + 1 > plotNumber) {
					String[] plotMessages = sendPlotInfo(plotNumber).split("\\{:\\}");
					sender.sendMessage(plotMessages[0]);
					sender.sendMessage(plotMessages[1]);
					if (!plotMessages[2].equalsIgnoreCase(" - ")) {
						sender.sendMessage(ChatColor.YELLOW + "Comment: " + ChatColor.DARK_GREEN + plotMessages[2]);
					}
				} else {
					sender.sendMessage(ChatColor.RED + "Plot request not found!");
				}
			}
		} else {
			sender.sendMessage(ChatColor.RED + "Usage: /plot check <number>");
		}
		return true;
	}
	
	public String sendPlotInfo(int plotNumber) {
		String[] plotInfo = plugin.getPlots().get(plotNumber).split("<:>");
		if (plotInfo.length == 4) {
			return ChatColor.GOLD + "#" + plotNumber + " " + plotInfo[1] + " by " + ChatColor.GREEN + plotInfo[2] + ChatColor.GOLD + " -" + ChatColor.GRAY + plotInfo[3];
		} else if (plotInfo.length == 6) {
			return ChatColor.GOLD + "#" + plotNumber + " " + plotInfo[1] + " by " + ChatColor.GREEN + plotInfo[2] + ChatColor.GOLD + " -" + ChatColor.GRAY + plotInfo[3] + "{:}"
					+ ChatColor.LIGHT_PURPLE + plotInfo[4] + "{:}" + plotInfo[5];
		}
		return null;
	}
}
