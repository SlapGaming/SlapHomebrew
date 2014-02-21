package me.naithantu.SlapHomebrew.Commands.Teleport;

import java.util.HashMap;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import me.naithantu.SlapHomebrew.Commands.AbstractCommand;
import me.naithantu.SlapHomebrew.Commands.Exception.CommandException;
import me.naithantu.SlapHomebrew.PlayerExtension.SlapPlayer;
import me.naithantu.SlapHomebrew.PlayerExtension.PlayerTeleporter.AbstractTeleportRequest;
import me.naithantu.SlapHomebrew.Util.Util;

public class TeleportDenyCommand extends AbstractCommand {

	public TeleportDenyCommand(CommandSender sender, String[] args) {
		super(sender, args);
	}

	@Override
	public boolean handle() throws CommandException {
		SlapPlayer p = getSlapPlayer();
		testPermission("tp.deny");
		
		if (args.length == 0) { //Deny the request, only if there's one.
			HashMap<String, AbstractTeleportRequest> requests = p.getTeleporter().getIncomingRequests(); //Get all requests
			if (requests.isEmpty()) { //Check if any Requests
				throw new CommandException("You have no pending incoming teleport requests standing.");
			}
			if (requests.size() > 1) { //If more than one requests
				throw new CommandException(
					"You have multiple incoming teleport requests. Use " + ChatColor.YELLOW + "/tpdeny [playername] \n" + 
					ChatColor.RED + "Pending requests: " + Util.buildString(requests.keySet(), ", ")
				);
			}
			requests.values().iterator().next().deny(); //Get first and Only request & Deny it			
		} else { //Name given
			AbstractTeleportRequest request = p.getTeleporter().getIncomingRequest(args[0]); //Get the request based on the given playername
			if (request == null) { //If no request found
				throw new CommandException("You have no pending incoming request from " + args[0]);
			}
			request.deny();
		}
		return true;
	}

}
