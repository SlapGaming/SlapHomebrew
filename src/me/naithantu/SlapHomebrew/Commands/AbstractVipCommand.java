package me.naithantu.SlapHomebrew.Commands;

import me.naithantu.SlapHomebrew.SlapHomebrew;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public abstract class AbstractVipCommand extends AbstractCommand {

	public AbstractVipCommand(CommandSender sender, String[] args, SlapHomebrew plugin) {
		super(sender, args, plugin);
	}
	
	@Override
	public void msg(CommandSender sender, String msg) {
		if (sender instanceof Player) {
			sender.sendMessage(ChatColor.DARK_AQUA + "[VIP] " + ChatColor.WHITE + msg);
		} else {
			sender.sendMessage("[VIP] " + msg);
		}
	}
}
