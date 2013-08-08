package me.naithantu.SlapHomebrew.Commands;

import java.util.Date;

import me.naithantu.SlapHomebrew.PlayerLogger;
import me.naithantu.SlapHomebrew.SlapHomebrew;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import com.earth2me.essentials.Essentials;
import com.earth2me.essentials.User;

public class TimecheckCommand extends AbstractCommand {

	private static PlayerLogger logger = null;
	private static Essentials ess = null;
	
	protected TimecheckCommand(CommandSender sender, String[] args, SlapHomebrew plugin) {
		super(sender, args, plugin);
		if (logger == null) {
			logger = plugin.getPlayerLogger();
		}		
		if (ess == null) {
			ess = plugin.getEssentials();
		}
	}

	@Override
	public boolean handle() {
		if (!testPermission(sender, "timecheck")) {
			noPermission(sender);
			return true;
		}
		if (args.length < 1) {
			return false;
		}
		User u = ess.getUserMap().getUser(args[0]);
		if (u == null) {
			badMsg(sender, "This player has never been on the server.");
		} else {
			boolean isOnline = false;
			if (plugin.getServer().getPlayer(u.getName()) != null) {
				isOnline = true;
			}
			Date fromDate; Date tillDate;
			switch (args.length) {
			case 1:
				sender.sendMessage(logger.getOnlineTime(u.getName(), isOnline));
				break;
			case 2:
				fromDate = logger.parseDate(args[1]);
				if (fromDate == null) {
					sender.sendMessage(ChatColor.RED + args[1] + " is not a valid date. Format: dd-mm-yyyy");
				} else {
					sender.sendMessage(logger.getOnlineTime(u.getName(), isOnline, fromDate));
				}
				break;
			case 3:
				fromDate = logger.parseDate(args[1]);
				if (fromDate == null) {
					sender.sendMessage(ChatColor.RED + args[1] + " is not a valid date. Format: dd-mm-yyyy");
				} else {
					tillDate = logger.parseDate(args[2]);
					if (tillDate == null) {
						sender.sendMessage(ChatColor.RED + args[2] + " is not a valid date. Format: dd-mm-yyyy");
					} else {
						sender.sendMessage(logger.getOnlineTime(u.getName(), isOnline, fromDate, tillDate));
					}
				}
				break;
			}
		}		
		return true;
	}
}
