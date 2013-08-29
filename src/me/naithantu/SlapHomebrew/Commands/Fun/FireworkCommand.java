package me.naithantu.SlapHomebrew.Commands.Fun;

import me.naithantu.SlapHomebrew.SlapHomebrew;
import me.naithantu.SlapHomebrew.Commands.AbstractCommand;
import me.naithantu.SlapHomebrew.Controllers.FireworkShow;
import me.naithantu.SlapHomebrew.Util.Util;

import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class FireworkCommand extends AbstractCommand {
	
	private static FireworkShow show;

	public FireworkCommand(CommandSender sender, String[] args, SlapHomebrew plugin) {
		super(sender, args, plugin);
		if (show == null) {
			show = plugin.getFireworkShow();
		}
	}

	@Override
	public boolean handle() {
		if (!testPermission(sender, "fireworkshow")) {
			noPermission(sender);
			return true;
		}
		
		if (!(sender instanceof Player)) {
			badMsg(sender, "You need to be in-game to do that.");
		}
		
		Player player = (Player) sender;
		
		if (args.length < 1) {
			if (show.isTeleportAllowed()) {
				player.teleport(new Location(plugin.getServer().getWorld("world_survival2"), -4700, 65, -4608));
			} else {
				badMsg(player, "There is no firework show running.");
			}
			return true;
		} else {
			if (!testPermission(sender, "fireworkshowcontroller")) {
				noPermission(sender);
				return true;
			}			
			switch (args[0].toLowerCase()) {
			case "run":
				if (show.isShowRunning()) {
					badMsg(sender, "Show is already running.");
				} else {
					show.launch();
				}
				break;
			case "toggle":
				if (show.toggleTeleportAllowed()) {
					sender.sendMessage(Util.getHeader() + "Teleport to fireworkshow allowed.");
				} else {
					sender.sendMessage(Util.getHeader() + "Teleport to fireworkshow not allowed.");
				}
				break;
			default:
				return false;
			}
		}
		return true;
	}
	

}
