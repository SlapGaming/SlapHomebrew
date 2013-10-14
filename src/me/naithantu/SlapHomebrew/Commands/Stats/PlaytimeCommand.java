package me.naithantu.SlapHomebrew.Commands.Stats;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.naithantu.SlapHomebrew.SlapHomebrew;
import me.naithantu.SlapHomebrew.Commands.AbstractCommand;
import me.naithantu.SlapHomebrew.Controllers.PlayerLogger;

public class PlaytimeCommand extends AbstractCommand {

	private PlayerLogger playerLogger;
	
	public PlaytimeCommand(CommandSender sender, String[] args, SlapHomebrew plugin) {
		super(sender, args, plugin);
		if (playerLogger == null) {
			playerLogger = plugin.getPlayerLogger();
		}
	}

	@Override
	public boolean handle() {
		if (!testPermission(sender, "playtime")) {
			noPermission(sender);
			return true;
		}
		if (!(sender instanceof Player)) {
			badMsg(sender, "You need to be ingame to do that.");
			return true;
		}
		Player p = (Player) sender;
		if (args.length > 0) {
			if (args[0].toLowerCase().equals("list")) {
				playerLogger.sendPlaytimeList(p);
			} else {
				badMsg(sender, "Usage: /playtime <list>");
			}
		} else {
			playerLogger.getOnlineTime(p, p.getName(), p.isOnline());
		}
		return true;
	}

}
