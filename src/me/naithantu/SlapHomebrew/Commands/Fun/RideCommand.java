package me.naithantu.SlapHomebrew.Commands.Fun;

import java.util.List;

import me.naithantu.SlapHomebrew.Commands.AbstractCommand;
import me.naithantu.SlapHomebrew.Commands.Exception.CommandException;
import me.naithantu.SlapHomebrew.Commands.Exception.ErrorMsg;
import me.naithantu.SlapHomebrew.PlayerExtension.PlayerControl;
import me.naithantu.SlapHomebrew.Util.Util;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class RideCommand extends AbstractCommand {
	
	public RideCommand(CommandSender sender, String[] args) {
		super(sender, args);
	}

	@Override
	public boolean handle() throws CommandException {
		Player player = getPlayer();
		testPermission("ride");
		
		if (player.isInsideVehicle()) player.getVehicle().eject(); //Check if in a vehicle
		if (args.length != 1) return false; //Check usage
		
		if (args[0].equalsIgnoreCase("click")) { //Click modus
			PlayerControl.getPlayer(player).setRideOnRightClick(true);
			hMsg("Right-click an entity to ride it.");
		} else { //Playername
			Player targetPlayer = getOnlinePlayer(args[0], false);
			if (targetPlayer.getName().equals(player.getName())) throw new CommandException(ErrorMsg.breakingServer); //Trying to ride itself
			targetPlayer.setPassenger(player);
			
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
		if (!Util.testPermission(sender, "ride") || args.length > 1) return createEmptyList(); //No permission
		
		List<String> players = listAllPlayers(sender.getName());
		players.add(0, "click");
		return filterResults(players, args[0]);
	}
	
}
