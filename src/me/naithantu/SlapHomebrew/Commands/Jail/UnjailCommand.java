package me.naithantu.SlapHomebrew.Commands.Jail;

import me.naithantu.SlapHomebrew.SlapHomebrew;
import me.naithantu.SlapHomebrew.Commands.AbstractCommand;
import me.naithantu.SlapHomebrew.Controllers.Jails;
import me.naithantu.SlapHomebrew.Util.Util;

import org.bukkit.command.CommandSender;

import com.earth2me.essentials.Essentials;
import com.earth2me.essentials.User;

public class UnjailCommand extends AbstractCommand {

	private static Jails jails = null;
	private static Essentials ess = null;
	
	public UnjailCommand(CommandSender sender, String[] args, SlapHomebrew plugin) {
		super(sender, args, plugin);
		if (jails == null) {
			jails = plugin.getJails();
		}
		if (ess == null) {
			ess = plugin.getEssentials();
		}
	}

	@Override
	public boolean handle() {
		if (!testPermission(sender, "jail")) {
			noPermission(sender);
			return true;
		}
		if (args.length < 1) {
			return false;
		}
		User u = ess.getUserMap().getUser(args[0]);
		if (u != null) {
			if (jails.isInJail(u.getName())) {
				jails.releasePlayerFromJail(u.getName());
				sender.sendMessage(Util.getHeader() + "Player unjailed.");
			} else {
				badMsg(sender, "This player is not in jail.");
			}
		} else {
			badMsg(sender, "This player doesn't exist.");
		}
		return true;
	}


}
