package me.naithantu.SlapHomebrew.Commands.Staff;

import java.util.List;

import me.naithantu.SlapHomebrew.Commands.AbstractCommand;
import me.naithantu.SlapHomebrew.Commands.Exception.CommandException;
import me.naithantu.SlapHomebrew.Commands.Exception.UsageException;
import me.naithantu.SlapHomebrew.PlayerExtension.PlayerControl;
import me.naithantu.SlapHomebrew.PlayerExtension.SlapPlayer;

import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_7_R1.entity.CraftEntity;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

public class TeleportMobCommand extends AbstractCommand {
	
	public TeleportMobCommand(CommandSender sender, String[] args) {
		super(sender, args);
	}

	@Override
	public boolean handle() throws CommandException {
		Player p = getPlayer(); //Check for player & Test perm
		testPermission("teleportmob");
		
		String fullUsage = "teleportmob <MassMove [to Player] [Radius] | SingleMove [to Player] | StopSingleMove>";
		if (args.length == 0) throw new UsageException(fullUsage); //Show usage
		
		Player toPlayer;
		
		//Get SlapPlayer
		SlapPlayer slapPlayer = PlayerControl.getPlayer(p);
		
		switch (args[0].toLowerCase()) {
		case "massmove": case "mm":
			if (args.length != 3) throw new UsageException("teleportmob massmove [to Player] [Radius]"); //Check usage
			
			//Disable SingleMoveMob if enabled
			if (slapPlayer.isTeleportingMob()) {
				slapPlayer.removeTeleportingMob();
				hMsg("SingleMove teleport has been disabled.");
			}
			
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
			slapPlayer.removeTeleportingMob(); //Reset SingleMoveMob, just in case.
			toPlayer = getOnlinePlayer(args[1], false); //Get player
			slapPlayer.setTeleportingMob(toPlayer);
			hMsg("Click the LivingEntities you want to move. Disable with: /teleportmob StopSingleMove");
			break;
			
		case "stopsinglemove": case "ssm":
			if (!slapPlayer.isTeleportingMob()) { //Check if moving mobs
				throw new CommandException("SingleMove teleport isn't enabled.");
			}
			slapPlayer.removeTeleportingMob(); //Remove
			hMsg("SingleMove teleport has been disabled."); //Notify
			break;
			
		default:
			throw new UsageException(fullUsage);
		}
		return true;
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
