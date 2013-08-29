package me.naithantu.SlapHomebrew.Commands;

import me.naithantu.SlapHomebrew.SlapHomebrew;
import me.naithantu.SlapHomebrew.Util.Util;

import org.bukkit.command.CommandSender;

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
		Util.msg(sender, msg);
	}

	protected void badMsg(CommandSender sender, String msg) {
		Util.badMsg(sender, msg);
	}

	protected void noPermission(CommandSender sender) {
		Util.noPermission(sender);
	}

	protected boolean testPermission(CommandSender sender, String perm) {
		return Util.testPermission(sender, perm);
	}
}
