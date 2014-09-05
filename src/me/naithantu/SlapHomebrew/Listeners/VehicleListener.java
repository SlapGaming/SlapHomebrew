package me.naithantu.SlapHomebrew.Listeners;

import me.naithantu.SlapHomebrew.Controllers.Horses;

import me.naithantu.SlapHomebrew.PlayerExtension.UUIDControl;
import me.naithantu.SlapHomebrew.Util.Util;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Player;
import org.bukkit.entity.Vehicle;
import org.bukkit.event.EventHandler;
import org.bukkit.event.vehicle.VehicleDamageEvent;
import org.bukkit.event.vehicle.VehicleDestroyEvent;
import org.bukkit.event.vehicle.VehicleEnterEvent;
import org.bukkit.event.vehicle.VehicleExitEvent;

public class VehicleListener extends AbstractListener {
	
	private Horses horses;
	
	public VehicleListener(Horses horses) {
		this.horses = horses;
	}
	
	@EventHandler
	public void onVehicleDamage(VehicleDamageEvent event) {
		if (event.getVehicle().hasMetadata("slapVehicle")) {
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void onVehicleDestroy(VehicleDestroyEvent event) {
		if (event.getVehicle().hasMetadata("slapVehicle")) {
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void onVehicleExit(VehicleExitEvent event) {
		Vehicle vehicle = event.getVehicle();
		if (vehicle.hasMetadata("slapVehicle")) {
			vehicle.remove();
		}
	}
	
	@EventHandler
	public void enterVehicle(VehicleEnterEvent event) {
		if (event.getVehicle() instanceof Horse && event.getEntered() instanceof Player) {
			Player player = (Player) event.getEntered();
			Horse targetHorse = (Horse) event.getVehicle();
			if (targetHorse.isTamed()) {
                //Get the UUID of the horse
                String horseUUID = targetHorse.getUniqueId().toString();
				if (horses.hasOwner(horseUUID)) {
                    //Allow if staff
                    if (Util.testPermission(player, "horse.staff")) return;

                    int userID = UUIDControl.getUserID(player);
                    //Allow if owner
                    if (horses.getOwnerID(horseUUID) == userID) return;
                    //Allow if on allowed list
                    if (horses.getAllowedUserIDs(horseUUID).contains(userID)) return;

                    //Not allowed
                    Util.badMsg(player, "You are not allowed to ride this horse.");
                    event.setCancelled(true);
				} else {
                    //Horse isn't owned yet
                    horses.onTameEvent(player, targetHorse);
				}
			}
		}
	}
	
}
