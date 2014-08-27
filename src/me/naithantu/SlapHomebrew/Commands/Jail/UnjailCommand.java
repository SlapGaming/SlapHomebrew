package me.naithantu.SlapHomebrew.Commands.Jail;

import java.util.List;

import me.naithantu.SlapHomebrew.PlayerExtension.UUIDControl;
import me.naithantu.SlapHomebrew.SlapHomebrew;
import me.naithantu.SlapHomebrew.Commands.AbstractCommand;
import me.naithantu.SlapHomebrew.Commands.Exception.CommandException;
import me.naithantu.SlapHomebrew.Commands.Exception.ErrorMsg;
import me.naithantu.SlapHomebrew.Controllers.Jails;
import me.naithantu.SlapHomebrew.Util.Util;

import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

public class UnjailCommand extends AbstractCommand {
	
	public UnjailCommand(CommandSender sender, String[] args) {
		super(sender, args);
	}

	@Override
	public boolean handle() throws CommandException {
		testPermission("jail"); //Test perm
		if (args.length != 1) return false; //Check usage
		
		//Get jails controller
		Jails jails = plugin.getJails();
		
		UUIDControl.UUIDProfile offPlayer = getOfflinePlayer(args[0]); //Get player
        String currentName = offPlayer.getCurrentName();
		if (jails.isInJail(currentName)) { //Check if in jail
			jails.releasePlayerFromJail(currentName); //Unjail
			hMsg("Player " + currentName + " unjailed.");
		} else {
			throw new CommandException(ErrorMsg.notInJail);
		}
		return true;
	}
	
	/**
	 * TabComplete on this command
	 * @param sender The sender of the command
	 * @param args given arguments
	 * @return List of options
	 */
	public static List<String> tabComplete(CommandSender sender, String[] args) {
		if (!Util.testPermission(sender, "jail") || args.length > 1) return createEmptyList(); //No perm
		
		return filterResults( //Return filtered jailed players
			SlapHomebrew.getInstance().getJails().getJailedPlayers(),
			args[0]
		);
	}


}
