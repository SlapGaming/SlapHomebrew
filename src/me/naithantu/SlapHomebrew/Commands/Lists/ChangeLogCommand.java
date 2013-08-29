package me.naithantu.SlapHomebrew.Commands.Lists;

import me.naithantu.SlapHomebrew.SlapHomebrew;
import me.naithantu.SlapHomebrew.Commands.AbstractCommand;
import me.naithantu.SlapHomebrew.Controllers.ChangeLog;
import me.naithantu.SlapHomebrew.Util.Util;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public class ChangeLogCommand extends AbstractCommand {
	
	private static ChangeLog changeLog = null;

	public ChangeLogCommand(CommandSender sender, String[] args, SlapHomebrew plugin) {
		super(sender, args, plugin);
		if (changeLog == null) {
			changeLog = plugin.getChangeLog();
		}
	}

	@Override
	public boolean handle() {
		if (args.length == 0) {
			//get Latest
			changeLog.showPage(sender, 1);
		} else {
			if (testPermission(sender, "addchangelog")) {
				if (args.length > 2 && args[0].equalsIgnoreCase("add")) {
					//Args[0] = add | args[1] = date | The rest = change
					String change = args[2]; int x = 3;
					while (x < args.length) {
						change = change + " " + args[x];
						x++;
					}
					changeLog.addToChangelog(args[1], change);
					sender.sendMessage(Util.getHeader() + "Added.");
					return true;
				} else if (args[0].equals("reload")) {
					changeLog.reload();
					sender.sendMessage(Util.getHeader() + "Changelog reloaded.");
					return true;
				}
			}
			try {
				int page = Integer.parseInt(args[0]);
				if (page > 0) {
					changeLog.showPage(sender, page);
				} else {
					sender.sendMessage(ChatColor.RED + "The page number has to be 1 or greater.");
				}
			} catch (NumberFormatException e) {
				return false;
			}
		}
		return true;
	}

}
