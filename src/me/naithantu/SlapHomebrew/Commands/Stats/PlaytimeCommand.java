package me.naithantu.SlapHomebrew.Commands.Stats;

import me.naithantu.SlapHomebrew.Commands.AbstractCommand;
import me.naithantu.SlapHomebrew.Commands.Exception.CommandException;
import me.naithantu.SlapHomebrew.Commands.Exception.UsageException;
import me.naithantu.SlapHomebrew.Controllers.PlayerLogger;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class PlaytimeCommand extends AbstractCommand {

	private PlayerLogger playerLogger;
	
	public PlaytimeCommand(CommandSender sender, String[] args) {
		super(sender, args);
		if (playerLogger == null) {
			playerLogger = plugin.getPlayerLogger();
		}
	}

	@Override
	public boolean handle() throws CommandException {
		Player p = getPlayer();
		testPermission("playtime");
		
		if (args.length > 0) {
			if (!args[0].toLowerCase().equals("list")) throw new UsageException("playtime <list>");
			playerLogger.sendPlaytimeList(p);
		} else {
			playerLogger.sendPlaytime(p);
		}
		return true;
	}

}
