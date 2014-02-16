package me.naithantu.SlapHomebrew.Commands.Fun;

import org.bukkit.command.CommandSender;

import me.naithantu.SlapHomebrew.Commands.AbstractCommand;
import me.naithantu.SlapHomebrew.Commands.Exception.CommandException;
import me.naithantu.SlapHomebrew.PlayerExtension.PlayerControl;
import me.naithantu.SlapHomebrew.PlayerExtension.SlapPlayer;

public class RageQuitCommand extends AbstractCommand {

	public RageQuitCommand(CommandSender sender, String[] args) {
		super(sender, args);
	}

	@Override
	public boolean handle() throws CommandException {
		SlapPlayer slapPlayer = PlayerControl.getPlayer(getPlayer()); //Get player
		testPermission("ragequit"); //Perms
		
		slapPlayer.setRageQuit(true); //Ragequit
		slapPlayer.p().kickPlayer("You left in a fit or rage!");
		return true;
	}

}
