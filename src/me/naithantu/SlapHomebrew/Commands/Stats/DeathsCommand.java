package me.naithantu.SlapHomebrew.Commands.Stats;

import me.naithantu.SlapHomebrew.Commands.AbstractCommand;
import me.naithantu.SlapHomebrew.Commands.Exception.CommandException;
import me.naithantu.SlapHomebrew.Controllers.PlayerLogging.DeathLogger;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class DeathsCommand extends AbstractCommand {

	public DeathsCommand(CommandSender sender, String[] args) {
		super(sender, args);
	}

	@Override
	public boolean handle() throws CommandException {
		testPermission("deaths");
		Player p = getPlayer();
		
		checkDoingCommand();
		addDoingCommand();
		DeathLogger.sendPlayerDeaths(p);
		return true;
	}

}
