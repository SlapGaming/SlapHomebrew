package me.naithantu.SlapHomebrew.Commands.Teleport;

import me.naithantu.SlapHomebrew.Commands.AbstractCommand;
import me.naithantu.SlapHomebrew.Commands.Exception.CommandException;
import me.naithantu.SlapHomebrew.Controllers.FancyMessage.FancyMessageControl;
import me.naithantu.SlapHomebrew.PlayerExtension.PlayerTeleporter;
import me.naithantu.SlapHomebrew.PlayerExtension.PlayerTeleporter.AbstractTeleportRequest;
import me.naithantu.SlapHomebrew.PlayerExtension.SlapPlayer;
import me.naithantu.SlapHomebrew.Util.Util;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.HashSet;

public class TeleportRequests extends AbstractCommand {

	public TeleportRequests(CommandSender sender, String[] args) {
		super(sender, args);
	}

	@Override
	public boolean handle() throws CommandException {
		SlapPlayer p = getSlapPlayer();
		testPermission("tp.requests");
		
		//Get teleporter
		PlayerTeleporter teleporter = p.getTeleporter(); 
		
		//Get FancyMessageControl
		FancyMessageControl fmc = plugin.getFancyMessage();
		
		//Message
		hMsg("--- Teleport requests ---" + ChatColor.GRAY + " (Tag []'s are clickable!)");

		//Check if any outgoing requests
		if (teleporter.getOutgoingRequest() == null) {
			p.sendMessage(" Outgoing: " + ChatColor.RED + "None");
		} else {
			AbstractTeleportRequest req = teleporter.getOutgoingRequest();
			if (!req.isPending() || req.hasTimedOut() || !req.areOnline()) {
				p.sendMessage(" Outgoing: " + ChatColor.GOLD + req.getRequested().getName() + ChatColor.WHITE + " - " + ChatColor.GRAY + "Timed out.");
			} else {
				Util.sendJsonMessage(p.p(), fmc.getTeleportRequestOutgoing(req.getRequested().getName()));
			}
		}
		
		//Check if any incoming requests
		if (teleporter.getIncomingRequests().isEmpty()) {
			p.sendMessage(" Incoming: " + ChatColor.RED + "None");
		} else {
			p.sendMessage(" Incoming:");
			for (AbstractTeleportRequest req : new HashSet<AbstractTeleportRequest>(teleporter.getIncomingRequests().values())) {
				if (!req.isPending() || req.hasTimedOut() || !req.areOnline()) {
					req.removeFromRequests();
					p.sendMessage("  \u2517\u25B6 " + ChatColor.GOLD + req.getRequested().getName() + ChatColor.WHITE + " - " + ChatColor.GRAY + " Timed out.");
					continue;
				}
				Util.sendJsonMessage(p.p(), fmc.getTeleportRequestIncoming(req.getRequester().getName()));
			}
		}
		return true;
	}

}
