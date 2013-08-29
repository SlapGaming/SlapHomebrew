package me.naithantu.SlapHomebrew.Commands.Basics;

import java.util.HashSet;

import me.naithantu.SlapHomebrew.SlapHomebrew;
import me.naithantu.SlapHomebrew.Commands.AbstractCommand;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class BlockfaqCommand extends AbstractCommand {
	public static HashSet<String> chatBotBlocks = new HashSet<String>();

	public BlockfaqCommand(CommandSender sender, String[] args, SlapHomebrew plugin) {
		super(sender, args, plugin);
	}

	public boolean handle() {
		if (!(sender instanceof Player)) {
			this.badMsg(sender, "You need to be in-game to do that!");
			return true;
		}

		if (!testPermission(sender, "blockfaq")) {
			this.noPermission(sender);
			return true;
		}

		Player player = (Player) sender;
		if (player.hasPermission("slaphomebrew.blockfaq")) {
			if (chatBotBlocks.contains(player.getName())) {
				chatBotBlocks.remove(player.getName());
				player.sendMessage(ChatColor.RED + "[FAQ] " + ChatColor.DARK_AQUA + "FAQ messages are no longer being blocked!");
			} else {
				chatBotBlocks.add(player.getName());
				player.sendMessage(ChatColor.RED + "[FAQ] " + ChatColor.DARK_AQUA + "All FAQ messages for you will be blocked from now on!");
			}
		} else {
			player.sendMessage(ChatColor.RED + "You do not have access to that command.");
		}
		return true;
	}
}
