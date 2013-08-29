package me.naithantu.SlapHomebrew.Commands;

import me.naithantu.SlapHomebrew.SlapHomebrew;
import me.naithantu.SlapHomebrew.Util.Util;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public abstract class AbstractVipCommand extends AbstractCommand {

	public AbstractVipCommand(CommandSender sender, String[] args, SlapHomebrew plugin) {
		super(sender, args, plugin);
	}
	
	@Override
	public void msg(CommandSender sender, String msg) {
		sender.sendMessage(ChatColor.DARK_AQUA + "[VIP] " + ChatColor.WHITE + msg);
	}
	
	public boolean testVipPermission(CommandSender sender, String permission) {
		return Util.testPermission(sender, "vip." + permission);
	}
	
	public void noVipPermission(CommandSender sender) {
		sender.sendMessage(ChatColor.DARK_AQUA + "[VIP] " + ChatColor.WHITE + "You need to be a vip to do that.");
	}
	
}
