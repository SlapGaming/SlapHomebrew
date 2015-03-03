package me.naithantu.SlapHomebrew.Listeners.Player;

import me.naithantu.SlapHomebrew.Commands.Basics.HorseCommand;
import me.naithantu.SlapHomebrew.Commands.Exception.CommandException;
import me.naithantu.SlapHomebrew.Commands.Staff.TeleportMobCommand;
import me.naithantu.SlapHomebrew.Controllers.Horses;
import me.naithantu.SlapHomebrew.Listeners.AbstractListener;
import me.naithantu.SlapHomebrew.PlayerExtension.PlayerControl;
import me.naithantu.SlapHomebrew.PlayerExtension.SlapPlayer;
import me.naithantu.SlapHomebrew.Util.Util;
import nl.stoux.SlapPlayers.SlapPlayers;
import org.bukkit.Material;
import org.bukkit.entity.*;
import org.bukkit.entity.Horse.Variant;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEntityEvent;

public class PlayerInteractEntityListener extends AbstractListener {
	
	private Horses horses;
	
	public PlayerInteractEntityListener(Horses horses) {
		this.horses = horses;
	}
	
	@EventHandler
	public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
		Player player = event.getPlayer();
		SlapPlayer slapPlayer = PlayerControl.getPlayer(player);
		Entity clickedEntity = event.getRightClicked();
				
		//Set last activity
		slapPlayer.moved();
		
		//Horse info click
        if (horses.isOnInfoClickList(player.getUniqueId().toString())) {
            if (clickedEntity.getType() == EntityType.HORSE) {
                try {
                    //Do checks before sending info
                    Horse horse = (Horse) clickedEntity;
                    //=> Check if the horse is tamed
                    if (!horse.isTamed()) throw new CommandException("This horse isn't tamed yet!");

                    String horseUUID = horse.getUniqueId().toString();
                    //=> Check if the horse has an owner
                    if (!horses.hasOwner(horseUUID)) throw new CommandException("This horse has no owner yet! Mount it to claim it!");

                    //Send horse info
                    HorseCommand.sendHorseInfo(horses, player, horse);
                } catch (CommandException e) {
                    Util.badMsg(player, e.getMessage());
                }
            } else {
                Util.badMsg(player, "That isn't a horse...");
            }

            //Cancel the event
            event.setCancelled(true);
            return;
        }
				
		//Entity Ride Click
		if (slapPlayer.isRideOnRightClick()) {
			player.leaveVehicle(); //Leave vehicle if there is any
			if (clickedEntity instanceof LivingEntity) { //Check if a livingEntity 
				clickedEntity.setPassenger(player);
			} else {
				Util.badMsg(player, "This is not a Living Entity.");
			}
			slapPlayer.setRideOnRightClick(false);
			return;
		}
		
		//Singlemove Teleport mob
		if (slapPlayer.isTeleportingMob()) {
			event.setCancelled(true);
			Player toPlayer = slapPlayer.getTeleportingTo(); //Get player to which the mobs should be teleported
			if (toPlayer == null || !toPlayer.isOnline()) {
				slapPlayer.removeTeleportingMob();
				Util.badMsg(player, "The player to teleport to is offline! Single Move disabled.");
				return;
			}
			
			if (clickedEntity instanceof LivingEntity && !(clickedEntity instanceof Player)) {
				TeleportMobCommand.teleportMob((LivingEntity) clickedEntity, toPlayer.getLocation());
			} else {
				Util.badMsg(player, "This entity cannot be teleported!");
			}
			return;
		}
				
		//Ignored for staff
		if (!Util.testPermission(player, "horse.staff")) {
			//Check if on a horse
			if (clickedEntity.getType() == EntityType.HORSE) {
                //=> Get the horse info
				Horse horse = (Horse) event.getRightClicked();
                String horseUUID = horse.getUniqueId().toString();

				//Check if the horse has an owner
                if (horses.hasOwner(horseUUID)) {
                    //Check if the player is allowed to interact with it
                    int playerID = SlapPlayers.getUUIDController().getProfile(player).getID();
                    if (!horses.isAllowedOnHorse(horseUUID, playerID)) {
                        //=> Not allowed, cancel
                        event.setCancelled(true);
                        Util.badMsg(player, "You are not allowed to interact with this horse.");
                    }

                }
			}
		}
		
		//Horse egg on undead/skeleton horse = Nope
		if (clickedEntity.getType() == EntityType.HORSE) {
			Horse horse = (Horse) event.getRightClicked();
			if (horse.getVariant() == Variant.SKELETON_HORSE || horse.getVariant() == Variant.UNDEAD_HORSE) {
				if (player.getItemInHand().getType() == Material.MONSTER_EGG){ 
					if (!event.isCancelled()) {
						Util.badMsg(player, "This horse doesn't like what you are trying to do...");
						event.setCancelled(true);
					}
				}
			}
		}
		
		//Protect Armor Stands
        if (clickedEntity.getType() == EntityType.ARMOR_STAND) {
            if (!plugin.getworldGuard().canBuild(player, clickedEntity.getLocation())) {
                event.setCancelled(true);
                Util.badMsg(player, "You do not have access to that Armor Stand.");
            }
        }
		
	}
		
}
