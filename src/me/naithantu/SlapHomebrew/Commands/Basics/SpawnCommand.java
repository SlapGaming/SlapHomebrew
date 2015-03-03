package me.naithantu.SlapHomebrew.Commands.Basics;

import me.naithantu.SlapHomebrew.Commands.AbstractCommand;
import me.naithantu.SlapHomebrew.Commands.Exception.CommandException;
import me.naithantu.SlapHomebrew.PlayerExtension.SlapPlayer;
import me.naithantu.SlapHomebrew.Util.Util;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;

import java.util.List;

public class SpawnCommand extends AbstractCommand {

	private SlapPlayer p;
	private static String resourceWorldName = null;
	
	public SpawnCommand(CommandSender sender, String[] args) {
		super(sender, args);
	}
	
	/**
	 * Set the name of the current resource world
	 * @param name The name of the rw-world
	 */
	public static void setResourceWorldName(String name) {
		resourceWorldName = name;
	}
	
	/**
	 * Get the name of the current resource world
	 * @return the name
	 */
	public static String getResourceWorldName() {
		return resourceWorldName;
	}

	@Override
	public boolean handle() throws CommandException {
		p = getSlapPlayer();
		testPermission("spawn");
				
		if (args.length == 0) {
			teleportToSpawn("world_start", "the lobby world", -180F);
		} else {
			switch (args[0].toLowerCase()) {
			case "old": case "oldsurvival":
				teleportToSpawn("world", "old survival world.", -90F);
				break;
			case "new": case "newsurvival": case "1.7": case "7": case "1.8": case "8":
				teleportToSpawn("world_survival3", "1.7/1.8 survival world.", -90F);
				break;
			case "creative": case "c":
				teleportToSpawn("world_creative", "creative world.", 90F);
				break;
			case "nether": case "thenether":
				teleportToSpawn("world_nether", "nether.", 90F);
				break;
			case "end": case "theend":
				teleportToSpawn("world_the_end", "end.", 0F);
				break;
			case "pvp":
				teleportToSpawn("world_pvp", "PVP world.", -90F);
				break;
			case "resource": case "rw": case "resourceworld":
				teleportToSpawn(resourceWorldName, "resource world.", -90F);
				break;
			case "games": case "sonic": case "game": case "mini": case "mini-games": case "minigames":
				teleportToSpawn("world_sonic", "games world.", 0F);
				break;
            case "projects": case "project": case "builds": case "build":
                if (Util.testPermission(sender, "projects.access")) {
                    teleportToSpawn("world_projects", "projects world.", 0F);
                    break;
                }
			default:
				teleportToSpawn("world_start", "lobby world.", -180F);
			}			
		}		
		return true;
	}
	
	/**
	 * Teleport the player to the spawn of a world
	 * @param worldname The code-wise name of the world
	 * @param teleportString The common-speak name of the world 
	 * @param yaw The yaw the player should be at
	 * @throws CommandException if world is disabled
	 */
	private void teleportToSpawn(String worldname, String teleportString, Float yaw) throws CommandException {
		try {
			//Get SpawnLocation
			Location loc = plugin.getServer().getWorld(worldname).getSpawnLocation();
			loc.setYaw(yaw);
			
			//Set BackLocation
			p.getTeleporter().setBackLocation(p.p().getLocation());
			
			//Teleport player
			p.p().teleport(loc);
			hMsg("You have been teleported to the " + teleportString);
		} catch (NullPointerException e) {
			throw new CommandException("Sorry! Teleporting to that world is currently disabled.");
		}
	}
	
	/**
	 * TabComplete on this command
	 * @param sender The sender of the command
	 * @param args given arguments
	 * @return List of options
	 */
	public static List<String> tabComplete(CommandSender sender, String[] args) {
		if (args.length == 1) {
			return filterResults(
				createNewList("oldsurvival", "newsurvival", "creative", "resourceworld", "games", "pvp", "nether", "end"),
				args[0]
			);
		}
		return null;
	}

}
