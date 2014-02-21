package me.naithantu.SlapHomebrew.Commands.Teleport;

import org.bukkit.command.CommandSender;

import me.naithantu.SlapHomebrew.Commands.AbstractCommand;
import me.naithantu.SlapHomebrew.Commands.Exception.CommandException;
import me.naithantu.SlapHomebrew.Controllers.FancyMessage.FancyMessageControl;
import me.naithantu.SlapHomebrew.PlayerExtension.PlayerControl;
import me.naithantu.SlapHomebrew.PlayerExtension.SlapPlayer;
import me.naithantu.SlapHomebrew.Util.Util;

public class TeleportAskHereCommand extends AbstractCommand {

	public TeleportAskHereCommand(CommandSender sender, String[] args) {
		super(sender, args);
	}

	@Override
	public boolean handle() throws CommandException {
		SlapPlayer p = getSlapPlayer(); //Get SlapPlayer
		testPermission("tp.askhere"); //Perms
		
		if (args.length != 1) return false; //Usage
		
		SlapPlayer target = PlayerControl.getPlayer(getOnlinePlayer(args[0], false)); //Get target player
		target.getTeleporter().RequestTeleport(p, true); //Request the teleport
		
		//Get FMControl
		FancyMessageControl fmc = plugin.getFancyMessage();
		
		//Message sender
		Util.sendJsonMessage(p.p(), fmc.getPlayerRequester(true, target.getName()));
		
		//Message reciever
		Util.msg(target.p(), p.getName() + " has requested you to teleport to them!");
		Util.sendJsonMessage(target.p(), fmc.getPlayerRequested(p.getName()));		
		return true;
	}

}
