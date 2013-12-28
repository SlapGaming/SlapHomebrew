package me.naithantu.SlapHomebrew.Commands.Fun;

import me.naithantu.SlapHomebrew.SlapHomebrew;
import me.naithantu.SlapHomebrew.Commands.AbstractCommand;
import me.naithantu.SlapHomebrew.Commands.Exception.CommandException;
import me.naithantu.SlapHomebrew.Commands.Exception.UsageException;
import me.naithantu.SlapHomebrew.Controllers.FireworkShow;

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
	public boolean handle() throws CommandException {
		Player player = getPlayer();
		testPermission("fireworkshow");
		
		if (args.length < 1) {
			if (show.isTeleportAllowed()) {
				player.teleport(new Location(plugin.getServer().getWorld("world_survival2"), -4700, 65, -4608));
			} else {
				throw new CommandException("here is no firework show running.");
			}
		} else {
			testPermission("fireworkshowcontroller");		
			switch (args[0].toLowerCase()) {
			case "run":
				if (show.isShowRunning()) {
					throw new CommandException("Show is already running.");
				} else {
					show.launch();
				}
				break;
				
			case "toggle":
				hMsg("Teleport to fireworkshow " + (show.toggleTeleportAllowed() ? "allowed." : "not allowed."));
				break;
			default:
				throw new UsageException("fireworkshow toggle/run");
			}
		}
		return true;
	}
	

}
