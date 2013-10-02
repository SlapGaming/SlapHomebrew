package me.naithantu.SlapHomebrew.Commands.AFK;

import me.naithantu.SlapHomebrew.SlapHomebrew;
import me.naithantu.SlapHomebrew.Commands.AbstractCommand;
import me.naithantu.SlapHomebrew.Controllers.AwayFromKeyboard;
import me.naithantu.SlapHomebrew.Util.Util;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class AfkCommand extends AbstractCommand {
	
	private static AwayFromKeyboard afk = null;
	
	public AfkCommand(CommandSender sender, String[] args, SlapHomebrew plugin) {
		super(sender, args, plugin);
		if (afk == null) {
			afk = plugin.getAwayFromKeyboard();
		}
	}

	public boolean handle() {
	//Get AFK info of person | Command: /afk -p [player]
	if (args.length == 2) {
			if (args[0].equals("-p")) {
				if (Util.testPermission(sender, "afk.info")) {
					Player p = plugin.getServer().getPlayer(args[1]);
					if (p != null) {
						String foundPlayer = p.getName();
						if (afk.isAfk(foundPlayer)) {
							long lastActive = plugin.getPlayerLogger().getLastActivity(foundPlayer);
							String lastActiveMsg;
							if (lastActive != 0) {
								long totalSeconds = (System.currentTimeMillis() - lastActive) / 1000;
								int minutes = (int) Math.floor(totalSeconds / (double) 60);
								int seconds = (int) totalSeconds - minutes * 60;
								lastActiveMsg = minutes + " minutes & " + seconds + " seconds ago.";
							} else {
								lastActiveMsg = "Unknown.";
							}
							sender.sendMessage(new String[] {Util.getHeader() + "Player: " + foundPlayer, Util.getHeader() + "AFK Reason: " + afk.getAfkReason(foundPlayer), Util.getHeader() + "Last Activity: " + lastActiveMsg});
							return true;
						} else {
							badMsg(sender, "Player is not afk.");
							return true;
						}
					} else {
						badMsg(sender, "Player doesn't exist.");
						return true;
					}
				}
			}
		}
		
		if (!(sender instanceof Player)) {
			badMsg(sender, "You need to be ingame");
			return true;
		}
		
		String playername = sender.getName();
		
		//Prevent auto AFK | command: /afk -prevent
		if (args.length == 1) {
			if (args[0].equals("-prevent")) {
				if (Util.testPermission(sender, "afk.prevent")) {
					if (afk.hasPreventAFK(playername)) {
						afk.removeFromPreventAFK(playername);
						sender.sendMessage(Util.getHeader() + "Prevent AFK is off.");
						return true;
					} else {
						afk.setPreventAFK(playername);
						sender.sendMessage(Util.getHeader() + "Prevent AFK is on.");
						return true;
					}
				}
			}
		}
		
		if (!afk.isAfk(playername)) {
			//Player currently not AFK -> Go AFK
			if (args.length == 0) {
				//No reason
				afk.goAfk(playername, "AFK");
			} else if (args.length > 0) {				
				//With reason
				String reason = null;
				for (String arg : args) {
					if (reason == null) {
						reason = arg;
					} else {
						reason = reason + " " + arg;
					}
				}
				afk.goAfk(playername, reason);
			}
		} else {
			//Player AFK -> Leave AFK
			afk.leaveAfk(playername);
		}
		return true;
	}
}