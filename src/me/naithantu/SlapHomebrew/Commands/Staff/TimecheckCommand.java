package me.naithantu.SlapHomebrew.Commands.Staff;

import java.util.Date;

import me.naithantu.SlapHomebrew.SlapHomebrew;
import me.naithantu.SlapHomebrew.Commands.AbstractCommand;
import me.naithantu.SlapHomebrew.Controllers.PlayerLogger;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import com.earth2me.essentials.Essentials;
import com.earth2me.essentials.User;

public class TimecheckCommand extends AbstractCommand {

	private static PlayerLogger logger = null;
	private static Essentials ess = null;
	
	public TimecheckCommand(CommandSender sender, String[] args, SlapHomebrew plugin) {
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
		if (args[0].equalsIgnoreCase("list")) {
			if (args.length < 2) return false;
			Date fromDate = null;
			if (args.length == 3) {
				fromDate = logger.parseDate(args[2]);
			}
			try {
				int nr = Integer.parseInt(args[1]);
				logger.getTimeList(sender, false, nr, fromDate);
			} catch (NumberFormatException e) {
				logger.getTimeList(sender, true, 0, fromDate);
			}
			return true;
		}
		User u = ess.getUserMap().getUser(args[0]);
		if (u == null) {
			badMsg(sender, "This player has never been on the server.");
		} else {
			Date fromDate = null;
			switch (args.length) {
			case 1:
				logger.getOnlineTime(sender, u.getName(), u.isOnline());
				break;
			case 2:
				fromDate = logger.parseDate(args[1]);
				if (fromDate == null) {
					sender.sendMessage(ChatColor.RED + args[1] + " is not a valid date. Format: dd-mm-yyyy");
				} else {
					logger.getOnlineTime(sender, u.getName(), u.isOnline(), fromDate);
				}
				break;
			}
		}		
		return true;
	}
}
