package me.naithantu.SlapHomebrew.Commands.Staff;

import me.naithantu.SlapHomebrew.SlapHomebrew;
import me.naithantu.SlapHomebrew.Commands.AbstractCommand;
import me.naithantu.SlapHomebrew.Commands.Exception.CommandException;
import me.naithantu.SlapHomebrew.Controllers.Bump;
import me.naithantu.SlapHomebrew.Util.Util;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class BumpdoneCommand extends AbstractCommand {
	SlapHomebrew plugin;
	Bump bump;

	public BumpdoneCommand(CommandSender sender, String args[], SlapHomebrew plugin) {
		super(sender, args, plugin);
		this.plugin = plugin;
		bump = plugin.getBump();
	}

	public boolean handle() throws CommandException {
		testPermission("bump"); //Test perm
		if (bump.getBumpIsDone()) throw new CommandException("Someone else has already bumped."); //Check if someone already bumped
		
		bump.bump(sender.getName());
		String bumpString = ChatColor.GREEN + "[Bump] " + ChatColor.WHITE + sender.getName() + " is going to bump! :D";
		for (Player onlinePlayer : plugin.getServer().getOnlinePlayers()) {
			if (Util.testPermission(onlinePlayer, "bump")) {
				onlinePlayer.sendMessage(bumpString);
			}
		}
		return true;
	}
}
