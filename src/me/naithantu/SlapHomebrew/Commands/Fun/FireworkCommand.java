package me.naithantu.SlapHomebrew.Commands.Fun;

import me.naithantu.SlapHomebrew.Commands.AbstractCommand;
import me.naithantu.SlapHomebrew.Commands.Exception.CommandException;
import me.naithantu.SlapHomebrew.Commands.Exception.UsageException;
import me.naithantu.SlapHomebrew.Controllers.FireworkShow;
import me.naithantu.SlapHomebrew.PlayerExtension.SlapPlayer;

import org.bukkit.Location;
import org.bukkit.command.CommandSender;

public class FireworkCommand extends AbstractCommand {

	public FireworkCommand(CommandSender sender, String[] args) {
		super(sender, args);
	}

	@Override
	public boolean handle() throws CommandException {
		SlapPlayer player = getSlapPlayer();
		testPermission("fireworkshow");
		
		//Get FireworkShow controller
		FireworkShow show = plugin.getFireworkShow();
		
		if (args.length < 1) {
			if (show.isTeleportAllowed()) {
				//Set back location
				player.getTeleporter().setBackLocation(player.p().getLocation());
				//Teleport player
				player.p().teleport(new Location(plugin.getServer().getWorld("world_survival2"), -4700, 65, -4608));
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
