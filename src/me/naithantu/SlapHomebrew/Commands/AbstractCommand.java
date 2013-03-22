package me.naithantu.SlapHomebrew.Commands;

import me.naithantu.SlapHomebrew.SlapHomebrew;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public abstract class AbstractCommand {

	abstract public boolean handle();

	protected CommandSender sender;
	protected String[] args;
	protected SlapHomebrew plugin;

	protected AbstractCommand(CommandSender sender, String[] args, SlapHomebrew plugin) {
		this.sender = sender;
		this.args = args;
		this.plugin = plugin;
	}

	protected void msg(CommandSender sender, String msg) {
		if (sender instanceof Player) {
			sender.sendMessage(ChatColor.GOLD + "[SLAP] " + ChatColor.WHITE + msg);
		} else {
			sender.sendMessage("[SLAP] " + msg);
		}
	}

	protected void badMsg(CommandSender sender, String msg) {
		if (sender instanceof Player) {
			sender.sendMessage(ChatColor.RED + msg);
		} else {
			sender.sendMessage(msg);
		}
	}

	protected void noPermission(CommandSender sender) {
		sender.sendMessage(ChatColor.RED + "You do not have access to that command.");
	}

	protected boolean testPermission(CommandSender sender, String perm) {
		String permission = "slaphomebrew." + perm;
		if (!(sender instanceof Player) || sender.hasPermission(permission))
			return true;
		return false;
	}
}
