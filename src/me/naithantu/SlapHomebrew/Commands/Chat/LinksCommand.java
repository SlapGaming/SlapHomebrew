package me.naithantu.SlapHomebrew.Commands.Chat;

import me.naithantu.SlapHomebrew.Commands.AbstractCommand;
import me.naithantu.SlapHomebrew.Commands.Exception.CommandException;
import me.naithantu.SlapHomebrew.Controllers.FancyMessage.FancyMessageControl;
import me.naithantu.SlapHomebrew.Util.Util;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class LinksCommand extends AbstractCommand {

	public LinksCommand(CommandSender sender, String[] args) {
		super(sender, args);
	}

	@Override
	public boolean handle() throws CommandException {
		Player p = getPlayer();
		testPermission("links");
		
		FancyMessageControl fmc = plugin.getFancyMessage(); //Get FMC
		String json = fmc.getJsonMessage("links"); //Get the message
		if (json == null) { //If not found
			throw new CommandException("This currently not available, sorry!");
		} else { //send message
			Util.sendJsonMessage(p, json);
		}
		return true;
	}

}
