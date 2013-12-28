package me.naithantu.SlapHomebrew.Commands.Fun;

import java.util.HashSet;

import me.naithantu.SlapHomebrew.SlapHomebrew;
import me.naithantu.SlapHomebrew.Commands.AbstractCommand;
import me.naithantu.SlapHomebrew.Commands.Exception.CommandException;
import me.naithantu.SlapHomebrew.Commands.Exception.ErrorMsg;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class RideCommand extends AbstractCommand {

	private static HashSet<String> rightClicks = new HashSet<>();
	
	public RideCommand(CommandSender sender, String[] args, SlapHomebrew plugin){
		super(sender, args, plugin);
	}

	@Override
	public boolean handle() throws CommandException {
		Player player = getPlayer();
		testPermission("ride");
		
		if (player.isInsideVehicle()) player.getVehicle().eject(); //Check if in a vehicle
		if (args.length != 1) return false; //Check usage
		
		if (args[0].equalsIgnoreCase("click")) { //Click modus
			rightClicks.add(player.getName());
			hMsg("Right-click an entity to ride it.");
		} else { //Playername
			Player targetPlayer = getOnlinePlayer(args[0], false);
			if (targetPlayer.getName().equals(player.getName())) throw new CommandException(ErrorMsg.breakingServer); //Trying to ride itself
			targetPlayer.setPassenger(player);
			
		}
		return true;
	}
	
	/**
	 * Get if player has rightClick to ride entity enabled
	 * @param player The player
	 * @return rightClick to ride
	 */
	public static boolean rightClick(String player){
		if (rightClicks.contains(player)) {
			rightClicks.remove(player);
			return true;
		}
		return false;
	}
	
}
