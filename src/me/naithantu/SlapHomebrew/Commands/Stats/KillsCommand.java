package me.naithantu.SlapHomebrew.Commands.Stats;

import me.naithantu.SlapHomebrew.Commands.AbstractCommand;
import me.naithantu.SlapHomebrew.Commands.Exception.CommandException;
import me.naithantu.SlapHomebrew.Controllers.PlayerLogger;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class KillsCommand extends AbstractCommand {

	private PlayerLogger playerLogger;
	
	public KillsCommand(CommandSender sender, String[] args) {
		super(sender, args);
		if (playerLogger == null) {
			playerLogger = plugin.getPlayerLogger();
		}
	}

	@Override
	public boolean handle() throws CommandException {
		Player p = getPlayer();
		testPermission("kills");
		playerLogger.getKills(p);
		return true;
	}

}
