package me.naithantu.SlapHomebrew.Commands.AFK;

import me.naithantu.SlapHomebrew.SlapHomebrew;
import me.naithantu.SlapHomebrew.Commands.AbstractCommand;
import me.naithantu.SlapHomebrew.Controllers.AwayFromKeyboard;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class AfkResetCommand extends AbstractCommand {
	
	private static AwayFromKeyboard afk = null;
	
	public AfkResetCommand(CommandSender sender, String[] args, SlapHomebrew plugin) {
		super(sender, args, plugin);
		if (afk == null) {
			afk = plugin.getAwayFromKeyboard();
		}
	}

	public boolean handle() {
		if (sender.hasPermission("slaphomebrew.staff")) {
			if (args.length > 0) {
				Player targetPlayer = plugin.getServer().getPlayer(args[0]);
				if (targetPlayer != null) {
					if (afk.isAfk(targetPlayer.getName())) {
						if (!afk.getAfkReason(targetPlayer.getName()).equals("AFK")) {
							afk.resetAfkReason(targetPlayer.getName());
							targetPlayer.sendMessage(ChatColor.RED + "Your AFK reason has been reset because it was inappropriate/not allowed.");
							sender.sendMessage(ChatColor.RED + "The AFK reason of " + targetPlayer.getName() + " has been reset.");
						} else {
							sender.sendMessage(ChatColor.RED + "This persons AFK reason is already reset/default.");
						}
					} else {
						sender.sendMessage(ChatColor.RED + "This person is currently not AFK.");
					}
				} else {
					sender.sendMessage(ChatColor.RED + "This person does not exist.");
				}
			} else {
				return false;
			}	
		} else {
			badMsg(sender, "You don't have permission for that!");
		}
		return true;
	}
	
}