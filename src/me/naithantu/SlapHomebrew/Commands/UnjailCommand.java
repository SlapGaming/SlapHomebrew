package me.naithantu.SlapHomebrew.Commands;

import me.naithantu.SlapHomebrew.Jails;
import me.naithantu.SlapHomebrew.SlapHomebrew;

import org.bukkit.command.CommandSender;

import com.earth2me.essentials.Essentials;
import com.earth2me.essentials.User;

public class UnjailCommand extends AbstractCommand {

	private static Jails jails = null;
	private static Essentials ess = null;
	
	protected UnjailCommand(CommandSender sender, String[] args, SlapHomebrew plugin) {
		super(sender, args, plugin);
		if (jails == null) {
			//Getjails
		}
		if (ess == null) {
			plugin.getEssentials();
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
		User u = ess.getUserMap().getUser(args[1]);
		if (u != null) {
			if (jails.isInJail(u.getName())) {
				jails.releasePlayerFromJail(u.getName());
			} else {
				badMsg(sender, "This player is not in jail.");
			}
		} else {
			badMsg(sender, "This player doesn't exist.");
		}
		return true;
	}


}
