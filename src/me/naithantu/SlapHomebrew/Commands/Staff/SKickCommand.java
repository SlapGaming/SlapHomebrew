package me.naithantu.SlapHomebrew.Commands.Staff;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.naithantu.SlapHomebrew.SlapHomebrew;
import me.naithantu.SlapHomebrew.Commands.AbstractCommand;
import me.naithantu.SlapHomebrew.Util.Util;

public class SKickCommand extends AbstractCommand {

	public SKickCommand(CommandSender sender, String[] args, SlapHomebrew plugin) {
		super(sender, args, plugin);
	}

	@Override
	public boolean handle() {
		if (!testPermission(sender, "skick")) {
			noPermission(sender);
			return true;
		}
		if (args.length < 1) {
			badMsg(sender, "Usage: /sKick [player] <Reason>");
			return true;
		}
		Player player = plugin.getServer().getPlayer(args[0]);
		if (player == null) {
			badMsg(sender, "No player found with the name: " + args[0]);
			return true;
		}
		String reason;
		if (args.length == 1) {
			reason = "You have been kicked!";
		} else {
			reason = args[1];
			int x = 2;
			while (x < args.length) {
				reason = reason + " " + args[x];
				x++;
			}
		}
		sender.sendMessage(Util.getHeader() + player.getName() + " has been kicked");
		player.kickPlayer(reason);
		return true;
	}
	
}
