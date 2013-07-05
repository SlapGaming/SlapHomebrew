package me.naithantu.SlapHomebrew.Commands;

import me.naithantu.SlapHomebrew.SlapHomebrew;

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
		Player targetPlayer = (Player)sender;
		
		if (args.length == 0) {
			teleportToSpawn(targetPlayer, "world_start", "the lobby world");
		} else {
			switch (args[0].toLowerCase()) {
			case "old": case "oldsurvival":
				teleportToSpawn(targetPlayer, "world", "the old survival world.");
				break;
			case "new": case "newsurvival":
				teleportToSpawn(targetPlayer, "world_survival2", "the new survival world.");
				break;
			case "creative": case "c":
				teleportToSpawn(targetPlayer, "world_creative", "the creative world.");
				break;
			case "nether": case "thenether":
				teleportToSpawn(targetPlayer, "world_nether", "the nether.");
				break;
			case "end": case "theend":
				teleportToSpawn(targetPlayer, "world_the_end", "the end.");
				break;
			case "pvp":
				teleportToSpawn(targetPlayer, "world_pvp", "the PVP world.");
				break;
			case "resource": case "rw":
				teleportToSpawn(targetPlayer, resourceWorldName, "the resource world.");
				break;
			case "sonic":
				plugin.getSonic().teleportSonic(targetPlayer.getName());
				this.msg(targetPlayer, "You have been teleported to the sonic racetrack!");
				break;
			default:
				teleportToSpawn(targetPlayer, "world_start", "the lobby world");
			}			
		}		
		return true;
	}
	
	private void teleportToSpawn(Player targetPlayer, String worldname, String teleportString) {
		targetPlayer.teleport(plugin.getServer().getWorld(worldname).getSpawnLocation());
		msg(targetPlayer, "You have been teleported to " + teleportString);
	}

}
