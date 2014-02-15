package me.naithantu.SlapHomebrew.Commands.Fun;

import me.naithantu.SlapHomebrew.Commands.AbstractCommand;
import me.naithantu.SlapHomebrew.Commands.Exception.CommandException;
import me.naithantu.SlapHomebrew.Commands.Exception.ErrorMsg;
import me.naithantu.SlapHomebrew.PlayerExtension.PlayerControl;

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
	
}
