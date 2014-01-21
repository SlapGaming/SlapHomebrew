package me.naithantu.SlapHomebrew.Commands.Stats;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.naithantu.SlapHomebrew.Commands.AbstractCommand;
import me.naithantu.SlapHomebrew.Commands.Exception.CommandException;
import me.naithantu.SlapHomebrew.Controllers.PlayerLogging.DeathLogger;

public class KillsCommand extends AbstractCommand {

	public KillsCommand(CommandSender sender, String[] args) {
		super(sender, args);
	}

	@Override
	public boolean handle() throws CommandException {
		testPermission("kills");
		Player p = getPlayer();
		
		checkDoingCommand();
		addDoingCommand();
		DeathLogger.sendPlayerKills(p);
		return true;
	}

}
