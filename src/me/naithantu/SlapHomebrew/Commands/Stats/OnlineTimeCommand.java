package me.naithantu.SlapHomebrew.Commands.Stats;

import java.util.ArrayList;
import java.util.List;

import me.naithantu.SlapHomebrew.Commands.AbstractCommand;
import me.naithantu.SlapHomebrew.Commands.Exception.CommandException;
import me.naithantu.SlapHomebrew.Controllers.PlayerLogging.SessionLogger;
import me.naithantu.SlapHomebrew.Controllers.PlayerLogging.SessionLogger.LeaderboardEntry;
import me.naithantu.SlapHomebrew.PlayerExtension.UUIDControl;
import me.naithantu.SlapHomebrew.Util.Util;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class OnlineTimeCommand extends AbstractCommand {

	public OnlineTimeCommand(CommandSender sender, String[] args) {
		super(sender, args);
	}

	@Override
	public boolean handle() throws CommandException {
		testPermission("onlinetime");
		Player p = getPlayer();
		
		checkDoingCommand();
		addDoingCommand();
		
		if (args.length > 0) {
			switch (args[0].toLowerCase()) {
			case "lb": case "leader": case "list": case "leaderboard": case "board": case "top": //If want leaderboard
				final SessionLogger logger = SessionLogger.getInstance(p); //Find instance
				Util.runASync(new Runnable() {
					@Override
					public void run() {
						ArrayList<LeaderboardEntry> entries = logger.getLeaderboard(null, null, 10); //Get alltime leaderboard
						int rank = 1;
						hMsg(ChatColor.YELLOW + "--- " + ChatColor.GOLD + "Onlinetime Leaderboard" + ChatColor.YELLOW + " ---");
						for (LeaderboardEntry entry : entries) { //Loop thru entries
							String playername = UUIDControl.getInstance().getUUIDProfile(entry.getUUID()).getCurrentName();
							sender.sendMessage(ChatColor.GREEN + String.valueOf(rank) + ". " + ChatColor.GOLD + playername +  ChatColor.WHITE + " - " + Util.getTimePlayedString(entry.getPlaytime())); //Send score
							rank++; //Increment rank
						}
						removeDoingCommand(); //When done, remove from doing command
					}
				});
				return true;
			}
		}
		
		SessionLogger.sendPlayerTime(p); //Send played time
		return true;
	}	
	
	/**
	 * TabComplete on this command
	 * @param sender The sender of the command
	 * @param args given arguments
	 * @return List of options
	 */
	public static List<String> tabComplete(CommandSender sender, String[] args) {
		return createNewList("leaderboard");
	}

}
