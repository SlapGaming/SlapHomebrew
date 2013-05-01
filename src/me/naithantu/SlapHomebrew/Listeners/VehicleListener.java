package me.naithantu.SlapHomebrew.Listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.vehicle.VehicleDamageEvent;
import org.bukkit.event.vehicle.VehicleDestroyEvent;

public class VehicleListener implements Listener {
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

	/* VehicleExitEvent removed until it is fixed. This is now handled via the playerinteractentityevent
	@EventHandler
	public void onVehicleExit(VehicleExitEvent event) {
		Vehicle vehicle = event.getVehicle();
		if (vehicle.hasMetadata("slapVehicle")) {
			vehicle.remove();
		}
	}*/
}
