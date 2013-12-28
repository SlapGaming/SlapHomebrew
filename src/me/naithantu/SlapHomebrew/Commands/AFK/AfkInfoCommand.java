package me.naithantu.SlapHomebrew.Commands.AFK;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.naithantu.SlapHomebrew.SlapHomebrew;
import me.naithantu.SlapHomebrew.Commands.AbstractCommand;
import me.naithantu.SlapHomebrew.Commands.Exception.CommandException;
import me.naithantu.SlapHomebrew.Controllers.AwayFromKeyboard;
import me.naithantu.SlapHomebrew.Util.Util;

public class AfkInfoCommand extends AbstractCommand {

	public AfkInfoCommand(CommandSender sender, String[] args, SlapHomebrew plugin) {
		super(sender, args, plugin);
	}

	@Override
	public boolean handle() throws CommandException {
		testPermission("afkinfo"); //Test for permission
		
		if (args.length != 1) return false; //Check for correct usage
		
		AwayFromKeyboard afkController = plugin.getAwayFromKeyboard();
		
		if (args[0].equalsIgnoreCase("list")) { //Get full list of AFK players
			boolean afkPlayer = false;
			hMsg("--- AFK Players ---");
			for (Player p : plugin.getServer().getOnlinePlayers()) { //Loop thru players
				String pName = p.getName();
				if (afkController.isAfk(pName)) { //Check if player is afk
					afkPlayer = true;
					msg(" Player: " + ChatColor.AQUA + pName + ChatColor.WHITE + " - Reason: " + ChatColor.GRAY + afkController.getAfkReason(pName)); //Send AFK Reason
				}
			}
			if (!afkPlayer) { //If no players AFK
				msg(" There are no players AFK.");
			}
		} else { //Check for a single player
			Player foundPlayer = getOnlinePlayer(args[0], false); //Get the player
			String pName = foundPlayer.getName();
			boolean afk = afkController.isAfk(pName);
			hMsg("Player: " +  ChatColor.GREEN + pName + ChatColor.WHITE + " | AFK: " + (afk ? ChatColor.AQUA + "Yes" : ChatColor.RED + "No")); //Send AFK string
			if (afk) { //If Player is AFK
				String afkReason = afkController.getAfkReason(pName); //Get AFK Reason
				if (!afkReason.equals("AFK")) { //Check if custom AFK reason
					hMsg("AFK Reason: " + afkReason); //Send AFK Reason
				}
				
				if (Util.testPermission(sender, "afkinfo.extended")) { //If CommandSender has extended info
					long lastActive = plugin.getPlayerLogger().getLastActivity(pName); //Get the last activity of the player
					if (lastActive != 0) {
						long totalSeconds = (System.currentTimeMillis() - lastActive) / 1000;
						int minutes = (int) Math.floor(totalSeconds / (double) 60);
						int seconds = (int) totalSeconds - minutes * 60;
						hMsg("Last activity: " +  minutes + " minutes & " + seconds + " seconds ago."); //Send last activity
					}
				}
			}
		}
		return true;
	}

}
