package me.naithantu.SlapHomebrew.Commands;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public abstract class AbstractCommandHandler {
	
	abstract public boolean handle();
	
	public void msg(CommandSender sender, String msg){
		if(sender instanceof Player){
			sender.sendMessage(ChatColor.GOLD + "[SLAP] " + msg);
		}else{
			sender.sendMessage("[SLAP] " + msg);
		}
	}
	
	public void badMsg(CommandSender sender, String msg){
		if(sender instanceof Player){
			sender.sendMessage(ChatColor.RED + msg);
		}else{
			sender.sendMessage(msg);
		}
	}
	
	public void noPermission(CommandSender sender){
		sender.sendMessage(ChatColor.RED + "You do not have access to that command.");
	}
}
