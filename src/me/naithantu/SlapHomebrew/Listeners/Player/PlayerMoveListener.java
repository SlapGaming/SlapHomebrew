package me.naithantu.SlapHomebrew.Listeners.Player;

import java.util.List;
import java.util.logging.Level;

import me.naithantu.SlapHomebrew.SlapHomebrew;
import me.naithantu.SlapHomebrew.Controllers.AwayFromKeyboard;
import me.naithantu.SlapHomebrew.Controllers.Extras;
import me.naithantu.SlapHomebrew.Controllers.Flag;
import me.naithantu.SlapHomebrew.Util.Util;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

public class PlayerMoveListener implements Listener {
	SlapHomebrew plugin;
	Extras extras;
	AwayFromKeyboard afk;

	public PlayerMoveListener(SlapHomebrew plugin, Extras extras, AwayFromKeyboard afk) {
		this.plugin = plugin;
		this.extras = extras;
		this.afk = afk;
	}

	@EventHandler
	public void onPlayerMove(PlayerMoveEvent event) {
		Player player = event.getPlayer();
		Location location = player.getLocation();
		if(player.getWorld().getName().equals("world_start")){
			if(extras.getHasJumped().contains(player.getName())){
				Entity playerEntity = (Entity) player;
				if(playerEntity.isOnGround()){
					List<String> hasJumped = extras.getHasJumped();
					hasJumped.remove(player.getName());
					player.setAllowFlight(true);
				}
			}
		}
		
		//Teleport players to location defined in the member flag upon entering a region.
		if (Util.hasFlag(plugin, location, Flag.TELEPORT)) {
			String flag = Util.getFlag(plugin, location, Flag.TELEPORT);
			String flagCoordinates = flag.replace("flag:teleport(", "").replace(")", "");
			String[] flagLocation = flagCoordinates.split(":");
			if(flagLocation.length < 3){
				plugin.getLogger().log(Level.SEVERE, "Improperly defined teleport flag! " + location.getX() + ":" + location.getY() + ":" + location.getZ());
				return;
			}
			try{
				Location teleportLocation = new Location(location.getWorld(), Double.parseDouble(flagLocation[0]), Double.parseDouble(flagLocation[1]), Double.parseDouble(flagLocation[2]));
				if(flagLocation.length > 3){
					teleportLocation.setYaw(Float.parseFloat(flagLocation[3]));
					if(flagLocation.length > 4){
						teleportLocation.setPitch(Float.parseFloat(flagLocation[4]));
					}
				}
				player.teleport(teleportLocation);
			}catch(NumberFormatException e){
				plugin.getLogger().log(Level.SEVERE, "Improperly defined teleport flag! " + location.getX() + ":" + location.getY() + ":" + location.getZ());
				return;
			}
		}
		
		if(Util.hasFlag(plugin, event.getTo(), Flag.COMMAND) && !Util.hasFlag(plugin, event.getFrom(), Flag.COMMAND)) {
			String flag = Util.getFlag(plugin, event.getTo(), Flag.COMMAND);
			String flagCommand = flag.replace("flag:command(", "").replace(")", "");
			String command = flagCommand.replaceAll("_", " ");
			//Add proper player names to command.
			command = command.replaceAll("<player>", player.getName());
			System.out.println(command);
			Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
		} else if (Util.hasFlag(plugin, event.getFrom(), Flag.COMMAND_LEAVE) && !Util.hasFlag(plugin, event.getTo(), Flag.COMMAND_LEAVE)) {
			String flag = Util.getFlag(plugin, event.getFrom(), Flag.COMMAND_LEAVE);
			String flagCommand = flag.replace("flag:command_leave(", "").replace(")", "");
			String command = flagCommand.replaceAll("_", " ");
			//Add proper player names to command.
			command = command.replaceAll("<player>", player.getName());
			System.out.println(command);
			Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
		}
		
		if (afk.isAfk(player.getName())) {
			if (event.getFrom().getBlockX() != event.getTo().getBlockX() || event.getFrom().getBlockZ() != event.getTo().getBlockZ() || event.getFrom().getBlockY() != event.getTo().getBlockY()) {
				afk.leaveAfk(player.getName());
			}
		}
	}
}
