package me.naithantu.SlapHomebrew.Commands;

import me.naithantu.SlapHomebrew.SlapHomebrew;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SpawnCommand extends AbstractCommand {

	private static String resourceWorldName = null;
	
	public SpawnCommand(CommandSender sender, String[] args, SlapHomebrew plugin) {
		super(sender, args, plugin);
		if (resourceWorldName == null) {
			for (World targetWorld : plugin.getServer().getWorlds()) {
				if (targetWorld.getName().contains("resource")) {
					resourceWorldName = targetWorld.getName();
				}
			}
		}
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
				teleportToSpawn(targetPlayer, "world_resource11", "the resource world.", -90F);
				break;
			case "sonic":
				plugin.getSonic().teleportSonic(targetPlayer.getName());
				this.msg(targetPlayer, "You have been teleported to the sonic racetrack!");
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
