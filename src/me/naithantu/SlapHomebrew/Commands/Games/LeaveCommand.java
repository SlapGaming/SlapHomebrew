package me.naithantu.SlapHomebrew.Commands.Games;

import me.naithantu.SlapHomebrew.Commands.AbstractCommand;
import me.naithantu.SlapHomebrew.Commands.Exception.CommandException;
import me.naithantu.SlapHomebrew.Controllers.Extras;
import me.naithantu.SlapHomebrew.Util.Util;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashSet;

public class LeaveCommand extends AbstractCommand {

	public LeaveCommand(CommandSender sender, String[] args) {
		super(sender, args);
	}

	public boolean handle() throws CommandException {
		final Player player = getPlayer(); //Check if player, test permission & check correct world
		testPermission("leave");
		testWorld("world_pvp");
		
		Extras extras = plugin.getExtras();
		final HashSet<String> pvpTimer = extras.getPvpTimer();
		final HashSet<String> pvpWorld = extras.getPvpWorld();
		
		final String playername = player.getName();
		if (!pvpTimer.contains(playername)) { //Has not done /leave in last 10 seconds
			//Add to hashsets
			pvpTimer.add(playername);
			pvpWorld.add(playername);
			
			hMsg("You will be teleported to spawn in 10 seconds!");
			Util.runLater(new Runnable() {
				
				@Override
				public void run() {
					if (pvpWorld.contains(playername)) { //If not hit.
						pvpWorld.remove(playername);
						if (player.isOnline()) {
							player.teleport(Bukkit.getWorld("world_start").getSpawnLocation());
							hMsg("You have been teleported to spawn!");
						}
					}
					pvpTimer.remove(playername);
				}
			}, 200);
			
		} else {
			throw new CommandException("You are only allowed to do this once per 10 seconds.");
		}

		return true;
	}
}
