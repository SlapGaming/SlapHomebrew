package me.naithantu.SlapHomebrew.Commands.Stats;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.naithantu.SlapHomebrew.SlapHomebrew;
import me.naithantu.SlapHomebrew.Commands.AbstractCommand;
import me.naithantu.SlapHomebrew.Controllers.PlayerLogger;

public class KillsCommand extends AbstractCommand {

	private PlayerLogger playerLogger;
	
	public KillsCommand(CommandSender sender, String[] args, SlapHomebrew plugin) {
		super(sender, args, plugin);
		if (playerLogger == null) {
			playerLogger = plugin.getPlayerLogger();
		}
	}

	@Override
	public boolean handle() {
		if (!testPermission(sender, "kills")) {
			noPermission(sender);
			return true;
		}
		if (!(sender instanceof Player)) {
			badMsg(sender, "You need to be ingame to do that.");
			return true;
		}
		playerLogger.getKills((Player) sender);
		return true;
	}

}
