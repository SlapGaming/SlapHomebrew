package me.naithantu.SlapHomebrew.Commands.Teleport;

import me.naithantu.SlapHomebrew.Commands.AbstractCommand;
import me.naithantu.SlapHomebrew.Commands.Exception.CommandException;
import me.naithantu.SlapHomebrew.PlayerExtension.PlayerControl;
import me.naithantu.SlapHomebrew.PlayerExtension.PlayerTeleporter.AbstractTeleportRequest;
import me.naithantu.SlapHomebrew.PlayerExtension.SlapPlayer;
import me.naithantu.SlapHomebrew.Util.Util;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.List;

public class TeleportAcceptCommand extends AbstractCommand {

	public TeleportAcceptCommand(CommandSender sender, String[] args) {
		super(sender, args);
	}

	@Override
	public boolean handle() throws CommandException {
		SlapPlayer p = getSlapPlayer();
		testPermission("tp.accept");
		
		if (args.length == 0) { //Accept the request, only if there's one.
			HashMap<String, AbstractTeleportRequest> requests = p.getTeleporter().getIncomingRequests(); //Get all requests
			if (requests.isEmpty()) { //Check if any Requests
				throw new CommandException("You have no pending incoming teleport requests standing.");
			}
			if (requests.size() > 1) { //If more than one requests
				throw new CommandException(
					"You have multiple incoming teleport requests. Use " + ChatColor.YELLOW + "/tpaccept [playername] \n" + 
					ChatColor.RED + "Pending requests: " + Util.buildString(requests.keySet(), ", ")
				);
			}
			requests.values().iterator().next().accept(); //Get first and Only request & Accept it			
		} else { //Name given
			AbstractTeleportRequest request = p.getTeleporter().getIncomingRequest(args[0]); //Get the request based on the given playername
			if (request == null) { //If no request found
				throw new CommandException("You have no pending incoming request from " + args[0]);
			}
			request.accept();
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
		List<String> list = createEmptyList();
		if (sender instanceof Player && args.length == 1) { //If a player
			list.addAll(PlayerControl.getPlayer(sender.getName()).getTeleporter().getIncomingRequests().keySet()); //Add options
			list = filterResults(list, args[0]);
		}
		return list;
	}

}
