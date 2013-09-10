package me.naithantu.SlapHomebrew.Commands.Lists;

import org.bukkit.command.CommandSender;

import me.naithantu.SlapHomebrew.SlapHomebrew;
import me.naithantu.SlapHomebrew.Commands.AbstractCommand;
import me.naithantu.SlapHomebrew.Controllers.WorthList;

public class WorthListCommand extends AbstractCommand {

	private static WorthList worthList;
	
	public WorthListCommand(CommandSender sender, String[] args, SlapHomebrew plugin) {
		super(sender, args, plugin);
		if (worthList == null) {
			worthList = plugin.getWorthList();
		}
	}

	@Override
	public boolean handle() {
		if (!testPermission(sender, "worthlist")) {
			noPermission(sender);
			return true;
		}
		int page = 1;
		if (args.length > 0) {
			try {
				page = Integer.parseInt(args[0]);
				if (page < 1) throw new NumberFormatException();
			} catch (NumberFormatException e) {
				badMsg(sender, "This is not a valid page number.");
				return true;
			}
		}
		if (page > worthList.getPages()) {
			badMsg(sender, "There are only " + worthList.getPages() + " pages.");
			return true;
		}
		worthList.sendPage(sender, page);
		return true;
	}

}
