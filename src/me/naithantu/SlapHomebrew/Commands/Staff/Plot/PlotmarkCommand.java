package me.naithantu.SlapHomebrew.Commands.Staff.Plot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import me.naithantu.SlapHomebrew.Commands.AbstractCommand;
import me.naithantu.SlapHomebrew.Commands.Exception.CommandException;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.google.common.base.Joiner;

public class PlotmarkCommand extends AbstractCommand {

	public PlotmarkCommand(CommandSender sender, String[] args) {
		super(sender, args);
	}

	public boolean handle() throws CommandException {
		Player player = getPlayer(); //Check if player
		testPermission("plot.mod"); //Test perm

		String reason = " - ";
		//Add reason.
		if (args.length > 1) {
			List<String> message = new ArrayList<String>();
			for (int i = 1; i < args.length; i++) {
				message.add(args[i]);
			}
			reason = Joiner.on(" ").join(message);
		}

		HashMap<Integer, String> plots = plugin.getPlots();
		int amount = plots.size() + 1;

		//Add extra information to plotinfo.
		String location = locationToString(player.getLocation());
		String date = new SimpleDateFormat("MMM.d HH:mm z").format(new Date());
		date = date.substring(0, 1).toUpperCase() + date.substring(1);
		String playerName = player.getName();
		plots.put(amount, location + "<:>" + date + "<:>" + playerName + "<:> " + reason);
		List<Integer> unfinishedPlots = plugin.getUnfinishedPlots();
		unfinishedPlots.add(amount);
		player.sendMessage(ChatColor.GOLD + "A plot request has been made!");

		return true;
	}

	public String locationToString(Location location) {
		String world = location.getWorld().getName();
		Double x = location.getX();
		Double y = location.getY();
		Double z = location.getZ();
		float yaw = location.getYaw();
		float pitch = location.getPitch();
		return world + ":" + x + ":" + y + ":" + z + ":" + yaw + ":" + pitch;
	}
}
