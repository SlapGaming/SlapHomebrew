package me.naithantu.SlapHomebrew.Commands.Staff.VIP;

import me.naithantu.SlapHomebrew.SlapHomebrew;
import me.naithantu.SlapHomebrew.Commands.AbstractCommand;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class VipmarkCommand extends AbstractCommand {

	public VipmarkCommand(CommandSender sender, String[] args, SlapHomebrew plugin) {
		super(sender, args, plugin);
	}

	public boolean handle() {
		if (!(sender instanceof Player)) {
			this.badMsg(sender, "You need to be in-game to do that.");
			return true;
		}

		Player player = (Player) sender;
		if (!testPermission(player, "vip.mark")) {
			this.noPermission(sender);
			return true;
		}
		
		return true;
	}
}
