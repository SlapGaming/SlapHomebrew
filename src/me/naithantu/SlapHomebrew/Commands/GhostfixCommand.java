package me.naithantu.SlapHomebrew.Commands;

import me.naithantu.SlapHomebrew.SlapHomebrew;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;


public class GhostfixCommand extends AbstractCommand {
	public GhostfixCommand(CommandSender sender, String[] args, SlapHomebrew plugin) {
		super(sender, args, plugin);
	}

	public boolean handle() {
		if (!testPermission(sender, "staff")) {
			this.noPermission(sender);
			return true;
		}

		if (args.length != 1) {
			this.badMsg(sender, "Usage: /ghostfix [player]");
			return false;
		}
		
		final Player player = plugin.getServer().getPlayer(args[0]);
		if (player == null) {
			this.badMsg(sender, "Player not found.");
			return true;
		}
		
		player.teleport(player.getLocation(), TeleportCause.PLUGIN);
		this.msg(player, "Your client was out of sync with the server.");
		return true;
		
	}

}
