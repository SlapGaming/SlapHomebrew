package me.naithantu.SlapHomebrew.Commands.Staff.Plot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import me.naithantu.SlapHomebrew.SlapHomebrew;
import me.naithantu.SlapHomebrew.Commands.AbstractCommand;
import me.naithantu.SlapHomebrew.Commands.Exception.CommandException;
import me.naithantu.SlapHomebrew.Commands.Exception.UsageException;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import com.google.common.base.Joiner;

public class PlotdoneCommand extends AbstractCommand {

	public PlotdoneCommand(CommandSender sender, String[] args, SlapHomebrew plugin) {
		super(sender, args, plugin);
	}

	public boolean handle() throws CommandException {
		testPermission("plot.admin"); //Test perm
		if (args.length == 1) throw new UsageException("plot done [number] <comment>"); //Check usage
		
		int plotNumber = parseInt(args[1]);
		
		List<Integer> unfinished = plugin.getUnfinishedPlots();
		if (!unfinished.contains(plotNumber)) throw new CommandException("This ID is not a unfinished plot mark."); //Check if unfinished plot mark
		
		HashMap<Integer, String> plots = plugin.getPlots(); //Get plots
		
		String plotsInfo = plots.get(plotNumber); //Some wierd parsing stuff
		List<String> message = new ArrayList<String>();
		for (int i = 2; i < args.length; i++) {
			message.add(args[i]);
		}
		String comment = " - ";
		if (!message.isEmpty())
			comment = Joiner.on(" ").join(message);
		plots.put(plotNumber, plotsInfo + "<:>" + "Handled by " + sender.getName() + "<:>" + comment); //Add to done plots
				
		//Remove plot from unfinished plots list.
		int index = 0;
		for (int i : unfinished) { //Loop thru list
			if (i == plotNumber) { //If found
				unfinished.remove(index); //Remove from list
				break; //And stop looping
			}
			index++;
		}
		msg(ChatColor.GOLD + "Plot Request #" + plotNumber + " was completed by " + sender.getName() + "!");
		return true;
	}
}
