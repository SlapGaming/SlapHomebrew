package me.naithantu.SlapHomebrew.Listeners.Player;

import java.util.List;
import java.util.logging.Level;

import me.naithantu.SlapHomebrew.Controllers.AwayFromKeyboard;
import me.naithantu.SlapHomebrew.Controllers.Extras;
import me.naithantu.SlapHomebrew.Controllers.Flag;
import me.naithantu.SlapHomebrew.Listeners.AbstractListener;
import me.naithantu.SlapHomebrew.PlayerExtension.PlayerControl;
import me.naithantu.SlapHomebrew.Util.Util;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerMoveEvent;

public class PlayerMoveListener extends AbstractListener {

	private Extras extras;
	private AwayFromKeyboard afk;

	public PlayerMoveListener(Extras extras, AwayFromKeyboard afk) {
		this.extras = extras;
		this.afk = afk;
	}

	@EventHandler
	public void onPlayerMove(PlayerMoveEvent event) {
		Player player = event.getPlayer();
		String playername = player.getName();

		Location from = event.getFrom();
		Location to = event.getTo();
		// Check if player actually moved (not just looking around)
		if (from.getX() != to.getX() || from.getY() != to.getY() || from.getZ() != to.getZ()) {

			// Allow next double jump if player hit the ground.
			if (player.getWorld().getName().equals("world_start")) {
				if (extras.getHasJumped().contains(playername)) {
					Entity playerEntity = (Entity) player;
					if (playerEntity.isOnGround()) {
						List<String> hasJumped = extras.getHasJumped();
						hasJumped.remove(playername);
						player.setAllowFlight(true);
					}
				}
			}

			// Handle custom flags that were defined in the member area.
			List<Flag> fromFlags = Util.getFlags(plugin, from);
			List<Flag> toFlags = Util.getFlags(plugin, to);
			if (toFlags.contains(Flag.TELEPORT)) {
				String flag = Util.getFlag(plugin, to, Flag.TELEPORT);
				String flagCoordinates = flag.replace("flag:teleport(", "").replace(")", "");
				String[] flagLocation = flagCoordinates.split(":");
				if (flagLocation.length < 3) {
					plugin.getLogger().log(Level.SEVERE, "Improperly defined teleport flag! " + to.getX() + ":" + to.getY() + ":" + to.getZ());
					return;
				}
				try {
					Location teleportLocation = new Location(to.getWorld(), Double.parseDouble(flagLocation[0]), Double.parseDouble(flagLocation[1]), Double.parseDouble(flagLocation[2]));
					if (flagLocation.length > 3) {
						teleportLocation.setYaw(Float.parseFloat(flagLocation[3]));
						if (flagLocation.length > 4) {
							teleportLocation.setPitch(Float.parseFloat(flagLocation[4]));
						}
					}
					player.teleport(teleportLocation);
				} catch (NumberFormatException e) {
					plugin.getLogger().log(Level.SEVERE, "Improperly defined teleport flag! " + to.getX() + ":" + to.getY() + ":" + to.getZ());
					return;
				}
			}

			if (toFlags.contains(Flag.COMMAND) && !fromFlags.contains(Flag.COMMAND)) {
				String flag = Util.getFlag(plugin, event.getTo(), Flag.COMMAND);
				String flagCommand = flag.replace("flag:command(", "").replace(")", "");
				String command = flagCommand.replaceAll("_", " ");
				// Add proper player names to command.
				command = command.replaceAll("<player>", playername);
				plugin.getLogger().log(Level.INFO, command);
				Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
			} else if (fromFlags.contains(Flag.COMMAND_LEAVE) && !toFlags.contains(Flag.COMMAND_LEAVE)) {
				String flag = Util.getFlag(plugin, event.getFrom(), Flag.COMMAND_LEAVE);
				String flagCommand = flag.replace("flag:command_leave(", "").replace(")", "");
				String command = flagCommand.replaceAll("_", " ");
				// Add proper player names to command.
				command = command.replaceAll("<player>", playername);
				plugin.getLogger().log(Level.INFO, command);
				Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
			}

			// Remove AFK
			if (afk.isAfk(player)) {
				afk.leaveAfk(player);
			}
		}

		//Player moved
		PlayerControl.getPlayer(player).moved();
	}
}
