package me.naithantu.SlapHomebrew.Commands.Staff;

import java.util.HashMap;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import me.naithantu.SlapHomebrew.SlapHomebrew;
import me.naithantu.SlapHomebrew.Commands.AbstractCommand;
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
	public boolean handle() {
		if (!testPermission(sender, "teleportmob")) {
			noPermission(sender);
			return true;
		}
		
		if (!(sender instanceof Player)) {
			badMsg(sender, "You need to be in-game to do that!");
			return true;
		}
		
		Player p = (Player) sender;
		
		if (args.length == 0) {
			badMsg(sender, "Usage: /teleportmob <MassMove [to Player] [Radius] | SingleMove [to Player] | StopSingleMove>");
			return true;
		}
		
		Player toPlayer;
		
		switch (args[0].toLowerCase()) {
		case "massmove": case "mm":
			if (args.length != 3) {
				badMsg(sender, "Usage: /teleportmob massmove [to Player] [Radius]");
				return true;
			}
			
			removeIfContains(p, true);
						
			toPlayer = plugin.getServer().getPlayer(args[1]);
			if (toPlayer == null) {
				badMsg(sender, "Player not found!");
			}
			Location toLocation = toPlayer.getLocation().add(0, 0.2, 0);
			
			int radius;
			try {
				radius = Integer.parseInt(args[2]);
				if (radius < 1 || radius > 25) {
					throw new NumberFormatException();
				}
			} catch (NumberFormatException e) {
				badMsg(sender, "Invalid radius! (Max 25)");
				return true;
			}
			
			int teleportedEntities = 0;
			double dRadius = (double) radius;
			List<Entity> foundEntities = p.getNearbyEntities(dRadius, dRadius, dRadius);
			for (Entity e : foundEntities) {
				if (e instanceof LivingEntity && !(e instanceof Player)) {
					LivingEntity le = (LivingEntity) e;
					le.teleport(toLocation);
					teleportedEntities++;
				}
			}
			
			p.sendMessage(Util.getHeader() + "Teleported " + teleportedEntities + " livingEntities.");
			break;
		case "singlemove": case "sm":
			removeIfContains(p, false);
			
			toPlayer = plugin.getServer().getPlayer(args[1]);
			if (toPlayer == null) {
				badMsg(sender, "Player not found!");
			}
			
			teleportMap.put(p.getName(), toPlayer.getName());
			p.sendMessage(Util.getHeader() + "Click the LivingEntities you want to move. Disable with: /teleportmob StopSingleMove");
			break;
		case "stopsinglemove": case "ssm":
			removeIfContains(p, true);
			break;
			
		default:
			badMsg(sender, "Usage: /teleportmob <MassMove [to Player] [Radius] | SingleMove [to Player] | StopSingleMove>");
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
	
}
