package me.naithantu.SlapHomebrew.Listeners;

import me.naithantu.SlapHomebrew.SlapHomebrew;

import org.bukkit.entity.Minecart;
import org.bukkit.event.Listener;
import org.bukkit.event.vehicle.VehicleDamageEvent;
import org.bukkit.event.vehicle.VehicleExitEvent;
import org.bukkit.event.EventHandler;

public class VehicleListener implements Listener {

	@EventHandler
	public void onVehicleDamage(VehicleDamageEvent event) {
		if (event.getVehicle() instanceof Minecart) {
			if (SlapHomebrew.mCarts.contains(event.getVehicle().getUniqueId())) {
				event.setCancelled(true);
			}
		}
	}

	@EventHandler
	public void onVehicleExit(VehicleExitEvent event) {
		if (event.getVehicle() instanceof Minecart) {
			if (SlapHomebrew.mCarts.contains(event.getVehicle().getUniqueId())) {
				SlapHomebrew.mCarts.remove(event.getVehicle().getUniqueId());
				event.getVehicle().remove();
			}
		}
	}
}
