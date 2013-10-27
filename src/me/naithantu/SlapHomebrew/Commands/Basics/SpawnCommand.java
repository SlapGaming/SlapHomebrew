package me.naithantu.SlapHomebrew.Commands.Basics;

import me.naithantu.SlapHomebrew.SlapHomebrew;
import me.naithantu.SlapHomebrew.Commands.AbstractCommand;

import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SpawnCommand extends AbstractCommand {

	private static String resourceWorldName = null;
	
	public SpawnCommand(CommandSender sender, String[] args, SlapHomebrew plugin) {
		super(sender, args, plugin);
	}
	
	public static void setResourceWorldName(String name) {
		resourceWorldName = name;
	}

	@Override
	public boolean handle() {
		if (!(sender instanceof Player)) {
			super.badMsg(sender, "You need to be in-game to do that.");
			return true;
		}
		
		if (!testPermission(sender, "spawn")) {
			this.noPermission(sender);
			return true;
		}
		
		Player targetPlayer = (Player) sender;
		
		if (args.length == 0) {
			teleportToSpawn(targetPlayer, "world_start", "the lobby world", -180F);
		} else {
			switch (args[0].toLowerCase()) {
			case "old": case "oldsurvival":
				teleportToSpawn(targetPlayer, "world", "the old survival world.", -90F);
				break;
			case "new": case "newsurvival":
				teleportToSpawn(targetPlayer, "world_survival2", "the new survival world.", -90F);
				break;
			case "creative": case "c":
				teleportToSpawn(targetPlayer, "world_creative", "the creative world.", 90F);
				break;
			case "nether": case "thenether":
				teleportToSpawn(targetPlayer, "world_nether", "the nether.", 90F);
				break;
			case "end": case "theend":
				teleportToSpawn(targetPlayer, "world_the_end", "the end.", 0F);
				break;
			case "pvp":
				teleportToSpawn(targetPlayer, "world_pvp", "the PVP world.", -90F);
				break;
			case "resource": case "rw":
				teleportToSpawn(targetPlayer, resourceWorldName, "the resource world.", -90F);
				break;
			case "games": case "sonic": case "game":
				teleportToSpawn(targetPlayer, "world_sonic", "the games world.", 0F);
				break;
			default:
				teleportToSpawn(targetPlayer, "world_start", "the lobby world", -180F);
			}			
		}		
		return true;
	}
	
	private void teleportToSpawn(Player targetPlayer, String worldname, String teleportString, Float yaw) {
		Location loc = plugin.getServer().getWorld(worldname).getSpawnLocation();
		loc.setYaw(yaw);
		targetPlayer.teleport(loc);
		msg(targetPlayer, "You have been teleported to " + teleportString);
	}

}
