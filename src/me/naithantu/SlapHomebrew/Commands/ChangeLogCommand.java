package me.naithantu.SlapHomebrew.Commands;

import me.naithantu.SlapHomebrew.ChangeLog;
import me.naithantu.SlapHomebrew.SlapHomebrew;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public class ChangeLogCommand extends AbstractCommand {
	
	private static ChangeLog changeLog = null;

	protected ChangeLogCommand(CommandSender sender, String[] args, SlapHomebrew plugin) {
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
