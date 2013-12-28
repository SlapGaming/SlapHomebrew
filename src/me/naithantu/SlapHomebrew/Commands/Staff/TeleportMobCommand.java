package me.naithantu.SlapHomebrew.Commands.Staff;

import java.util.HashMap;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_7_R1.entity.CraftEntity;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import me.naithantu.SlapHomebrew.SlapHomebrew;
import me.naithantu.SlapHomebrew.Commands.AbstractCommand;
import me.naithantu.SlapHomebrew.Commands.Exception.CommandException;
import me.naithantu.SlapHomebrew.Commands.Exception.UsageException;
import me.naithantu.SlapHomebrew.Util.Util;

public class TeleportMobCommand extends AbstractCommand {

	private static HashMap<String, String> teleportMap;
	
	public TeleportMobCommand(CommandSender sender, String[] args, SlapHomebrew plugin) {
		super(sender, args, plugin);
		if (teleportMap == null) {
			teleportMap = new HashMap<>();
		}
	}

	@Override
	public boolean handle() throws CommandException {
		Player p = getPlayer(); //Check for player & Test perm
		testPermission("teleportmob");
		
		String fullUsage = "teleportmob <MassMove [to Player] [Radius] | SingleMove [to Player] | StopSingleMove>";
		if (args.length == 0) throw new UsageException(fullUsage); //Show usage
		
		Player toPlayer;
		
		switch (args[0].toLowerCase()) {
		case "massmove": case "mm":
			if (args.length != 3) throw new UsageException("teleportmob massmove [to Player] [Radius]"); //Check usage
			removeIfContains(p, true);
			toPlayer = getOnlinePlayer(args[1], false); //Get player to teleport to
			Location toLocation = toPlayer.getLocation().add(0, 0.2, 0); //Get to location
			
			int radius = parseInt(args[2]); //Parse radius
			if (radius <= 0 || radius > 25) throw new CommandException("Invalid radius! (Max 25)");
						
			int teleportedEntities = 0;
			double dRadius = (double) radius;
			List<Entity> foundEntities = p.getNearbyEntities(dRadius, dRadius, dRadius); //Find entities
			for (Entity e : foundEntities) { //loop thru found Entities
				if (e instanceof LivingEntity && !(e instanceof Player)) { //Check if not an entity or a player
					teleportMob((LivingEntity) e, toLocation); //Teleport mob
					teleportedEntities++;
				}
			}
			hMsg("Teleported " + teleportedEntities + " livingEntities.");
			break;
			
		case "singlemove": case "sm":
			if (args.length != 2) throw new UsageException("teleportmob singlemove [to Player]"); //Check usage
			removeIfContains(p, false);
			toPlayer = getOnlinePlayer(args[1], false); //Get player
			teleportMap.put(p.getName(), toPlayer.getName()); //Add to teleporter map
			hMsg("Click the LivingEntities you want to move. Disable with: /teleportmob StopSingleMove");
			break;
			
		case "stopsinglemove": case "ssm":
			removeIfContains(p, true);
			break;
			
		default:
			throw new UsageException(fullUsage);
		}
		return true;
	}
	
	/**
	 * Get if a player is a single mob mover
	 * @param player The player
	 * @return is mover
	 */
	public static boolean isInMap(String player) {
		if (teleportMap == null) return false;
		return teleportMap.containsKey(player);
	}
	
	/**
	 * Get the player to teleport the mob to
	 * @param fromPlayer From the player
	 * @return The name of the 'toPlayer'
	 */
	public static String getToPlayerName(String fromPlayer) {
		return teleportMap.get(fromPlayer);
	}
	
	/**
	 * The player the mob has to be teleported to went offline
	 * @param fromPlayer
	 */
	public static void toPlayerWentOffline(String fromPlayer) {
		teleportMap.remove(fromPlayer);
	}

	private void removeIfContains(Player p, boolean msg) {
		if (teleportMap.containsKey(p.getName())) {
			teleportMap.remove(p.getName());
			if (msg) p.sendMessage(Util.getHeader() + "Singlemove mobs has been disabled.");
		}
	}
	
	/**
	 * Teleport a mob to the given location
	 * @param e The mob
	 * @param toLocation The location
	 */
	public static void teleportMob(LivingEntity e, Location toLocation) {
		if (e.getWorld() == toLocation.getWorld()) {
			e.teleport(toLocation);
		} else {
			CraftEntity craftEntity = (CraftEntity) e;
			craftEntity.getHandle().teleportTo(toLocation, false);
		}
	}
	
	
	
}
