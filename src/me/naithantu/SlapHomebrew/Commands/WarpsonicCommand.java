package me.naithantu.SlapHomebrew.Commands;

import me.naithantu.SlapHomebrew.SlapHomebrew;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class WarpsonicCommand extends AbstractCommand {
	public WarpsonicCommand(CommandSender sender, String[] args, SlapHomebrew plugin) {
		super(sender, args, plugin);
	}

	public boolean handle() {
		if (!(sender instanceof Player)) {
			this.badMsg(sender, "You need to be in-game to do that!");
			return true;
		}

		if (!testPermission(sender, "warpsonic")) {
			this.noPermission(sender);
			return true;
		}

		final Player player = (Player) sender;
		plugin.getSonic().teleportSonic(player.getName());
		this.msg(sender, "You have been teleported to the sonic racetrack!");
		return true;
	}
}
