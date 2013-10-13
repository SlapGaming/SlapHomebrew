package me.naithantu.SlapHomebrew.Commands.Basics;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.naithantu.SlapHomebrew.SlapHomebrew;
import me.naithantu.SlapHomebrew.Commands.AbstractCommand;
import me.naithantu.SlapHomebrew.Controllers.PlayerLogger;

public class SuicideCommand extends AbstractCommand {

	private static PlayerLogger playerLogger;
	
	public SuicideCommand(CommandSender sender, String[] args, SlapHomebrew plugin) {
		super(sender, args, plugin);
		if (playerLogger == null) {
			playerLogger = plugin.getPlayerLogger();
		}
	}

	@Override
	public boolean handle() {
		if (!sender.hasPermission("essentials.suicide")) { //Check if has permission
			noPermission(sender);
			return true;
		}
		if (!(sender instanceof Player)) {
			badMsg(sender, "You need to be ingame to do that.");
			return true;
		}
		Player p = (Player) sender;
		if (p.getHealth() == 0) {
			badMsg(sender, "Huh, you are already dead?");
		} else {
			p.sendMessage(ChatColor.GRAY + "Goodbye world D:");
			playerLogger.commitsSuicide(p.getName());
			p.setHealth(0);
		}
		return true;
	}
	
	

}
