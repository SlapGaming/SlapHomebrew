package me.naithantu.SlapHomebrew.Commands.Staff;

import java.util.Date;

import me.naithantu.SlapHomebrew.Commands.AbstractCommand;
import me.naithantu.SlapHomebrew.Commands.Exception.CommandException;
import me.naithantu.SlapHomebrew.Controllers.PlayerLogger;

import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

public class TimecheckCommand extends AbstractCommand {
	
	public TimecheckCommand(CommandSender sender, String[] args) {
		super(sender, args);	
	}

	@Override
	public boolean handle() throws CommandException {
		testPermission("timecheck"); //Test perm
		if (args.length < 1) return false; //Check usage
		
		Date fromDate = null; //The date
		PlayerLogger logger = plugin.getPlayerLogger(); //Get logger
		
		if (args[0].equalsIgnoreCase("list")) {
			if (args.length < 2) return false; //Check usage
			if (args.length == 3) {
				fromDate = logger.parseDate(args[2]); //Parse date if given
			}
			try {
				int nr = Integer.parseInt(args[1]);
				logger.getTimeList(sender, false, nr, fromDate);
			} catch (NumberFormatException e) {
				logger.getTimeList(sender, true, 0, fromDate);
			}
			return true;
		}
		
		OfflinePlayer offPlayer = getOfflinePlayer(args[0]); //Get player
		String playername = offPlayer.getName(); //Get it's name
		boolean isOnline = (offPlayer.getPlayer() != null); //Check if online
		switch (args.length) {
		case 1:
			logger.getOnlineTime(sender, playername, isOnline); //Send onlinetime
			break;
		case 2:
			fromDate = logger.parseDate(args[1]); //Parse the date
			if (fromDate == null) throw new CommandException("Invalid date. Format: DD-MM-YYYY"); 
			logger.getOnlineTime(sender, playername, isOnline, fromDate);
			break;
		default:
			return false;
		}
		return true;
	}
}
