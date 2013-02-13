package me.naithantu.SlapHomebrew.Commands;

import me.naithantu.SlapHomebrew.Bump;
import me.naithantu.SlapHomebrew.SlapHomebrew;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class BumpdoneCommand extends AbstractCommand {
	SlapHomebrew plugin;
	Bump bump;

	public BumpdoneCommand(CommandSender sender, String args[], SlapHomebrew plugin) {
		super(sender, args);
		this.plugin = plugin;
		bump = plugin.getBump();
	}

	public boolean handle() {
		if (!testPermission(sender, "bump")) {
			this.noPermission(sender);
			return true;
		}

		if (!bump.getBumpIsDone()) {
			bump.bump();
			plugin.addBumpDone(sender.getName());
			this.msg(sender, "Thanks for bumping! :)");
			for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
				if (!onlinePlayer.getName().equals(sender.getName()) && onlinePlayer.hasPermission("slaphomebrew.bump")) {
					this.msg(sender, sender.getName() + " has bumped!");
				}
			}
		} else {
			this.badMsg(sender, "Someone else is already bumping!");
		}
		return true;
	}
}
