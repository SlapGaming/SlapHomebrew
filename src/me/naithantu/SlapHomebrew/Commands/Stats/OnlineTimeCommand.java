package me.naithantu.SlapHomebrew.Commands.Stats;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.naithantu.SlapHomebrew.Commands.AbstractCommand;
import me.naithantu.SlapHomebrew.Commands.Exception.CommandException;
import me.naithantu.SlapHomebrew.Controllers.PlayerLogging.SessionLogger;

public class OnlineTimeCommand extends AbstractCommand {

	public OnlineTimeCommand(CommandSender sender, String[] args) {
		super(sender, args);
	}

	@Override
	public boolean handle() throws CommandException {
		testPermission("onlinetime");
		Player p = getPlayer();
		
		checkDoingCommand();
		addDoingCommand();
		
		SessionLogger.sendPlayerTime(p);
		return true;
	}

}
