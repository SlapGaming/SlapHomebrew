package me.naithantu.SlapHomebrew.Listeners.Entity;

import java.util.HashSet;

import me.naithantu.SlapHomebrew.Controllers.Flag;
import me.naithantu.SlapHomebrew.Controllers.Horses;
import me.naithantu.SlapHomebrew.Listeners.AbstractListener;
import me.naithantu.SlapHomebrew.Util.Util;

import nl.stoux.SlapPlayers.SlapPlayers;
import org.bukkit.Location;
import org.bukkit.entity.Animals;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Horse;
import org.bukkit.entity.NPC;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class EntityDamageByEntityListener extends AbstractListener {
	
	Horses horses;

	public EntityDamageByEntityListener(Horses horses) {
		this.horses = horses;
	}

	@EventHandler
	public void onEntityDamageByEvent(EntityDamageByEntityEvent event) {
		//Entity protector on WG
		Entity damager = event.getDamager();
		if(damager instanceof Projectile){
			Projectile projectile = (Projectile) damager;
			if (projectile.getShooter() instanceof Entity) {
				damager = (Entity) projectile.getShooter();
			}
		}
		
		if (damager != null && damager instanceof Player) {
			Entity entity = event.getEntity();
			
			if (event.getEntity() instanceof Player) {
				String world = event.getEntity().getWorld().getName();
				if (world.equals("world_pvp")) {
					HashSet<String> pvpWorld = plugin.getExtras().getPvpWorld();
					if (pvpWorld.contains(((Player) event.getEntity()).getName())) {
						Util.msg((Player) entity, "You have been attacked! Teleport cancelled!");
						pvpWorld.remove(((Player) event.getEntity()).getName());
					}
				}
			}
			
			if (entity instanceof Animals || entity instanceof NPC) {
				Location entityLoc = event.getEntity().getLocation();
				Player player = (Player) damager;
				if (!player.hasPermission("slaphomebrew.staff") && !Util.hasFlag(plugin, entityLoc, Flag.NOMOBPROTECT) && !plugin.getworldGuard().canBuild(player, entityLoc)) {
					event.setCancelled(true);
					player.sendMessage(Util.getHeader() + "You may not attack animals or villagers here!");
				}
			}
		}
		
		//Horse protection
		if (event.getEntityType() == EntityType.HORSE && damager instanceof Player) {
			Horse horse = (Horse) event.getEntity();

            Player player = (Player) damager;
            //Check if the player isn't a staff member
            if (!Util.testPermission(player, "horse.staff")) {
                //Check if the horse has an owner
                String horseUUID = horse.getUniqueId().toString();
                if (horses.hasOwner(horseUUID)) {
                    //Get the ID of the damager
                    int damagerID = SlapPlayers.getUUIDController().getProfile(player).getID();

                    //Get the ID of the owner
                    int ownerID = horses.getOwnerID(horseUUID);

                    //Check if the owner = the player
                    if (ownerID != damagerID) {
                        //=> Player not allowed to attack horse that isn't theirs
                        Util.badMsg(player, "You are not allowed to attack this horse.");
                        event.setCancelled(true);
                    }
                }
            }
		}

        //Armor stand protection
        if (event.getEntityType() == EntityType.ARMOR_STAND && damager instanceof Player) {
            Location armorStandLoc = event.getEntity().getLocation();

            //Check if the player has access to that area
            Player player = (Player) damager;
            if (!plugin.getworldGuard().canBuild(player, armorStandLoc)) {
                event.setCancelled(true);
                Util.badMsg(player, "You do not have access to that Armor Stand.");
            }
        }
		
		
	}
}
