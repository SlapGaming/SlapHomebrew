package me.naithantu.SlapHomebrew.Commands.Teleport;

import me.naithantu.SlapHomebrew.Commands.AbstractCommand;
import me.naithantu.SlapHomebrew.Commands.Exception.CommandException;
import me.naithantu.SlapHomebrew.PlayerExtension.PlayerTeleporter.AbstractTeleportRequest;
import me.naithantu.SlapHomebrew.PlayerExtension.SlapPlayer;

import org.bukkit.command.CommandSender;

public class TeleportCancelCommand extends AbstractCommand {

	public TeleportCancelCommand(CommandSender sender, String[] args) {
		super(sender, args);
	}

	@Override
	public boolean handle() throws CommandException {
		SlapPlayer p = getSlapPlayer(); //Get SlapPlayer
		testPermission("tp.cancel"); //perms
		
		AbstractTeleportRequest request = p.getTeleporter().getOutgoingRequest(); //Get request
		if (request == null) { //Check if there is a pending request
			throw new CommandException("You have no outgoing pending requests!");
		}
		
		request.cancel(); //Cancel the request
		return true;
	}

}
