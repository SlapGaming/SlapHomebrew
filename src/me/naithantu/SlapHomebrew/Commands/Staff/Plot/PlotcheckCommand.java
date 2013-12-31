package me.naithantu.SlapHomebrew.Commands.Staff.Plot;

import java.util.List;

import me.naithantu.SlapHomebrew.Commands.AbstractCommand;
import me.naithantu.SlapHomebrew.Commands.Exception.CommandException;
import me.naithantu.SlapHomebrew.Commands.Exception.UsageException;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public class PlotcheckCommand extends AbstractCommand {

	public PlotcheckCommand(CommandSender sender, String[] args) {
		super(sender, args);
	}

	public boolean handle() throws CommandException {
		testPermission("plot.mod"); //Test perm
		switch (args.length) {
		case 1: //Get plots waiting to be cleared
			List<Integer> unfinishedPlots = plugin.getUnfinishedPlots();
			if (unfinishedPlots.isEmpty()) throw new CommandException("There are no plots that need to be cleared!"); //Check if empty
			msg(ChatColor.AQUA + "--------- " + unfinishedPlots.size() + " Plots Waiting To Be Cleared ---------"); //Send waiting plot marks
			for (int plotNumber : unfinishedPlots) {
				msg(sendPlotInfo(plotNumber));
			}
			break;
		case 2: //Get one with a certain number
			int plotNumber = parseInt(args[1]);
			if (plugin.getUnfinishedPlots().contains(plotNumber)) { //If unfinished send info
				msg(sendPlotInfo(plotNumber));
			} else { //Get info from done list
				if (plugin.getPlots().size() + 1 < plotNumber || plotNumber < 1) throw new CommandException("No plot mark found with this ID."); //Check if Valid ID
				
				String[] plotMessages = sendPlotInfo(plotNumber).split("\\{:\\}");
				msg(plotMessages[0]);
				msg(plotMessages[1]);
				if (!plotMessages[2].equals(" - ")) {
					msg(ChatColor.YELLOW + "Comment: " + ChatColor.DARK_GREEN + plotMessages[2]);
				}	
			}
			break;
		default:
			throw new UsageException("plot check <number>");
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
