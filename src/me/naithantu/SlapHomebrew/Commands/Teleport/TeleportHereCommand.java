package me.naithantu.SlapHomebrew.Commands.Teleport;

import me.naithantu.SlapHomebrew.Commands.AbstractCommand;
import me.naithantu.SlapHomebrew.Commands.Exception.CommandException;
import me.naithantu.SlapHomebrew.Util.Util;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashSet;

public class TeleportHereCommand extends AbstractCommand {

	public TeleportHereCommand(CommandSender sender, String[] args) {
		super(sender, args);
	}

	@Override
	public boolean handle() throws CommandException {
		Player p = getPlayer();
		testPermission("tp.directhere");
		
		if (args.length < 1) return false; //Usage
		
		HashSet<Player> set = new HashSet<>();
		for (String player : args) { //Get all players
			set.add(getOnlinePlayer(player, false));
		}
		
		for (Player foundPlayer : set) { //Loop thru players
			try { 
				Util.safeTeleport(foundPlayer, p.getLocation(), p.isFlying(), true, true); //Try to teleport
			}  catch (CommandException e) {
				Util.badMsg(p, "Failed to teleport " + foundPlayer.getName() + " to you. (Lava/Void?)"); //Notify player if someone was not able to be teleported
			}
		}
		return true;
	}

}
