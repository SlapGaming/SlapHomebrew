package me.naithantu.SlapHomebrew.Commands.Teleport;

import me.naithantu.SlapHomebrew.Commands.AbstractCommand;
import me.naithantu.SlapHomebrew.Commands.Exception.CommandException;
import me.naithantu.SlapHomebrew.Controllers.FancyMessage.FancyMessageControl;
import me.naithantu.SlapHomebrew.PlayerExtension.PlayerControl;
import me.naithantu.SlapHomebrew.PlayerExtension.SlapPlayer;
import me.naithantu.SlapHomebrew.Util.Util;
import org.bukkit.command.CommandSender;

public class TeleportAskCommand extends AbstractCommand {

	public TeleportAskCommand(CommandSender sender, String[] args) {
		super(sender, args);
	}

	@Override
	public boolean handle() throws CommandException {
		SlapPlayer p = getSlapPlayer(); //Get SlapPlayer
		testPermission("tp.ask"); //Perms
		
		if (args.length != 1) return false; //Usage
		
		SlapPlayer target = PlayerControl.getPlayer(getOnlinePlayer(args[0], false)); //Get target player
		target.getTeleporter().RequestTeleport(p, false); //Request the teleport
		
		//Get FMControl
		FancyMessageControl fmc = plugin.getFancyMessage();
		
		//Message sender
		Util.sendJsonMessage(p.p(), fmc.getPlayerRequester(false, target.getName()));
		
		//Message reciever
		Util.msg(target.p(), p.getName() + " has requested to teleport to you!");
		Util.sendJsonMessage(target.p(), fmc.getPlayerRequested(p.getName()));		
		return true;
	}

}
