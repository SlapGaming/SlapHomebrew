package me.naithantu.SlapHomebrew.Commands.AFK;

import me.naithantu.SlapHomebrew.SlapHomebrew;
import me.naithantu.SlapHomebrew.Commands.AbstractCommand;
import me.naithantu.SlapHomebrew.Controllers.AwayFromKeyboard;

import org.bukkit.command.CommandSender;

public class AfkCommand extends AbstractCommand {
	
	private static AwayFromKeyboard afk = null;
	
	public AfkCommand(CommandSender sender, String[] args, SlapHomebrew plugin) {
		super(sender, args, plugin);
		if (afk == null) {
			afk = plugin.getAwayFromKeyboard();
		}
	}

	public boolean handle() {
		if (!afk.isAfk(sender.getName())) {
			//Player currently not AFK -> Go AFK
			if (args.length == 0) {
				//No reason
				afk.goAfk(sender.getName(), "AFK");
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
				afk.goAfk(sender.getName(), reason);
			}
		} else {
			//Player AFK -> Leave AFK
			afk.leaveAfk(sender.getName());
		}
		return true;
	}
}