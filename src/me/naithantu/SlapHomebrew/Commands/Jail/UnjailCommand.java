package me.naithantu.SlapHomebrew.Commands.Jail;

import me.naithantu.SlapHomebrew.Commands.AbstractCommand;
import me.naithantu.SlapHomebrew.Commands.Exception.CommandException;
import me.naithantu.SlapHomebrew.Commands.Exception.ErrorMsg;
import me.naithantu.SlapHomebrew.Controllers.Jails;

import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

import com.earth2me.essentials.Essentials;

public class UnjailCommand extends AbstractCommand {

	private static Jails jails = null;
	private static Essentials ess = null;
	
	public UnjailCommand(CommandSender sender, String[] args) {
		super(sender, args);
		if (jails == null) {
			jails = plugin.getJails();
		}
		if (ess == null) {
			ess = plugin.getEssentials();
		}
	}

	@Override
	public boolean handle() throws CommandException {
		testPermission("jail"); //Test perm
		if (args.length != 1) return false; //Check usage
		
		OfflinePlayer offPlayer = getOfflinePlayer(args[0]); //Get player
		if (jails.isInJail(offPlayer.getName())) { //Check if in jail
			jails.releasePlayerFromJail(offPlayer.getName()); //Unjail
			hMsg("Player " + offPlayer.getName() + " unjailed.");
		} else {
			throw new CommandException(ErrorMsg.notInJail);
		}
		return true;
	}


}
