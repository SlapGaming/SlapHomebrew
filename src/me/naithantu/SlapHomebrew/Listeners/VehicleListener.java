package me.naithantu.SlapHomebrew.Listeners;

import me.naithantu.SlapHomebrew.Vehicles;

import org.bukkit.entity.Boat;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Vehicle;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.vehicle.VehicleDamageEvent;
import org.bukkit.event.vehicle.VehicleDestroyEvent;
import org.bukkit.event.vehicle.VehicleExitEvent;

public class VehicleListener implements Listener {
	Vehicles vehicles;

	public VehicleListener(Vehicles vehicles) {
		this.vehicles = vehicles;
	}

	@EventHandler
	public void onVehicleDamage(VehicleDamageEvent event) {
		Vehicle vehicle = event.getVehicle();
		if (vehicle instanceof Minecart) {
			if (vehicles.isMinecart((Minecart) vehicle)) {
				event.setCancelled(true);
			}
		} else if (vehicle instanceof Boat) {
			if(vehicles.isBoat((Boat) vehicle)) {
				event.setCancelled(true);
			}
		}
	}
	
	@EventHandler
	public void onVehicleDestroy(VehicleDestroyEvent event){
		Vehicle vehicle = event.getVehicle();
		if(vehicle instanceof Boat && vehicles.isBoat((Boat) vehicle)){
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void onVehicleExit(VehicleExitEvent event) {
		Vehicle vehicle = event.getVehicle();
		if (vehicle instanceof Minecart) {
			Minecart minecart = (Minecart) vehicle;
			if (vehicles.isMinecart(minecart)) {
				vehicles.removeMinecart(minecart);
				vehicle.remove();
			}
		} else if (vehicle instanceof Boat){
			Boat boat = (Boat) vehicle;
			if(vehicles.isBoat(boat)) {
				vehicles.removeBoat(boat);
				vehicle.remove();
			}
		}
	}
}
