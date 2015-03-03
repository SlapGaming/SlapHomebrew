package me.naithantu.SlapHomebrew.Commands.VIP;

import me.naithantu.SlapHomebrew.Commands.AbstractCommand;
import me.naithantu.SlapHomebrew.Commands.Exception.CommandException;
import me.naithantu.SlapHomebrew.PlayerExtension.PlayerControl;
import me.naithantu.SlapHomebrew.PlayerExtension.SlapPlayer;
import me.naithantu.SlapHomebrew.Util.Util;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;

public class BackCommand extends AbstractCommand {

	public BackCommand(CommandSender sender, String[] args) {
		super(sender, args);
	}

	@Override
	public boolean handle() throws CommandException {
		SlapPlayer p = PlayerControl.getPlayer(getPlayer());
		testPermission("back");
		Location backLocation = p.getTeleporter().getBackLocation(); //Get back location
		if (backLocation == null) { //Check if location is set
			throw new CommandException("There is no location to go back to!");
		}
		Util.safeTeleport(p.p(), backLocation, p.p().isFlying(), true); //Teleport the player
		return true;
	}

}
