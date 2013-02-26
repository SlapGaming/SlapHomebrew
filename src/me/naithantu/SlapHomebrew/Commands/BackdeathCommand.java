package me.naithantu.SlapHomebrew.Commands;

import me.naithantu.SlapHomebrew.SlapHomebrew;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class BackdeathCommand extends AbstractVipCommand {

	public BackdeathCommand(CommandSender sender, String[] args, SlapHomebrew plugin) {
		super(sender, args, plugin);
	}

	public boolean handle() {
		if (!(sender instanceof Player)) {
			this.badMsg(sender, "You need to be in-game to do that!");
			return true;
		}

		if (!testPermission(sender, "backdeath")) {
			this.noPermission(sender);
			return true;
		}

		Player player = (Player) sender;
		if (SlapHomebrew.backDeath.containsKey(player.getName())) {
			player.teleport(SlapHomebrew.backDeath.get(player.getName()));
			this.msg(sender, "You have been warped to your death location!");
		}
		return true;
	}
}
