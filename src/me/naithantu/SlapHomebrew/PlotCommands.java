package me.naithantu.SlapHomebrew;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.google.common.base.Joiner;

public class PlotCommands implements CommandExecutor {

	private SlapHomebrew plugin;

	public PlotCommands(SlapHomebrew instance) {
		plugin = instance;
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

	public Location stringToLocation(int plotNumber) {
		String[] plotInfo = plugin.getPlots().get(plotNumber).split("<:>");
		String[] locationInfo = plotInfo[0].split(":");
		Location location = new Location(plugin.getServer().getWorld(locationInfo[0]), Double.valueOf(locationInfo[1]), Double.valueOf(locationInfo[2]), Double.valueOf(locationInfo[3]),
				Float.valueOf(locationInfo[4]), Float.valueOf(locationInfo[5]));
		return location;
	}

	private boolean markCommand(Player player, String[] args) {
		if (!player.hasPermission("slaphomebrew.plot.mod")) {
			player.sendMessage(ChatColor.RED + "You do not have access to that command!");
			return true;
		}
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

	private boolean checkCommand(Player player, String[] args) {
		if (!player.hasPermission("slaphomebrew.plot.mod")) {
			player.sendMessage(ChatColor.RED + "You do not have access to that command!");
			return true;
		}
		if (args.length == 1) {
			List<Integer> unfinishedPlots = plugin.getUnfinishedPlots();
			player.sendMessage(ChatColor.AQUA + "--------- " + unfinishedPlots.size() + " Plots Waiting To Be Cleared ---------");
			for (int plotNumber : unfinishedPlots) {
				player.sendMessage(sendPlotInfo(plotNumber));
			}
		} else if (args.length == 2) {
			int plotNumber;
			try {
				plotNumber = Integer.parseInt(args[1]);
			} catch (NumberFormatException e) {
				player.sendMessage(ChatColor.RED + "Usage: /plot check [number]");
				return true;
			}
			if (plugin.getUnfinishedPlots().contains(plotNumber)) {
				player.sendMessage(sendPlotInfo(plotNumber));
			} else {
				if (plugin.getPlots().size() + 1 > plotNumber) {
					String[] plotMessages = sendPlotInfo(plotNumber).split("\\{:\\}");
					player.sendMessage(plotMessages[0]);
					player.sendMessage(plotMessages[1]);
					if (!plotMessages[2].equalsIgnoreCase(" - ")) {
						player.sendMessage(ChatColor.YELLOW + "Comment: " + ChatColor.DARK_GREEN + plotMessages[2]);
					}
				} else {
					player.sendMessage(ChatColor.RED + "Plot request not found!");
				}
			}
		} else {
			player.sendMessage(ChatColor.RED + "Usage: /plot check <number>");
		}
		return true;
	}

	private boolean tpCommand(Player player, String[] args) {
		if (!player.hasPermission("slaphomebrew.plot.mod")) {
			player.sendMessage(ChatColor.RED + "You do not have access to that command!");
			return true;
		}
		if (args.length < 2) {
			player.sendMessage(ChatColor.RED + "Usage: /plot tp <number>");
			return true;
		}
		int plotNumber;
		try {
			plotNumber = Integer.parseInt(args[1]);
		} catch (NumberFormatException e) {
			player.sendMessage(ChatColor.RED + "Usage: /plot tp [number]");
			return true;
		}
		if (plugin.getPlots().size() + 1 > plotNumber) {
			player.teleport(stringToLocation(plotNumber));
		} else {
			player.sendMessage(ChatColor.RED + "Plot request not found!");
		}
		return true;
	}

	private boolean doneCommand(Player player, String[] args) {
		if (!player.hasPermission("slaphomebrew.plot.admin")) {
			player.sendMessage(ChatColor.RED + "You do not have access to that command!");
			return true;
		}
		if (args.length == 1) {
			player.sendMessage(ChatColor.RED + "Usage: /plot done [number] <comment>");
		}
		int plotNumber;

		try {
			plotNumber = Integer.parseInt(args[1]);
		} catch (NumberFormatException e) {
			player.sendMessage(ChatColor.RED + "Usage: /plot done [number] <comment>");
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
			plugin.getPlots().put(plotNumber, plotsInfo + "<:>" + "Handled by " + player.getName() + "<:>" + comment);
		} else {
			player.sendMessage(ChatColor.RED + "You can not finish that request!");
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
		player.sendMessage(ChatColor.GOLD + "Plot Request #" + plotNumber + " was completed by " + player.getName() + "!");
		return true;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage("Error: This command may only be used by in-game players!");
			return true;
		}
		Player player = (Player) sender;
		if (commandLabel.equalsIgnoreCase("plot")) {
			if (args.length > 0) {
				String arg = args[0];
				if (arg.equalsIgnoreCase("mark")) {
					markCommand(player, args);
				}
				if (arg.equalsIgnoreCase("check")) {
					checkCommand(player, args);
				}
				if (arg.equalsIgnoreCase("tp") || arg.equalsIgnoreCase("tpid")) {
					tpCommand(player, args);
				}
				if (arg.equalsIgnoreCase("done")) {
					doneCommand(player, args);
				}
			}
		}

		if (commandLabel.equalsIgnoreCase("pmark")) {
			markCommand(player, args);
		}
		if (commandLabel.equalsIgnoreCase("pcheck")) {
			checkCommand(player, args);
		}
		if (commandLabel.equalsIgnoreCase("ptp")) {
			tpCommand(player, args);
		}
		if (commandLabel.equalsIgnoreCase("pdone")) {
			doneCommand(player, args);
		}
		return false;
	}
}
