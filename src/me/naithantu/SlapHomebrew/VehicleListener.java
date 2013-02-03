package me.naithantu.SlapHomebrew;

import org.bukkit.entity.Minecart;
import org.bukkit.event.Listener;
import org.bukkit.event.vehicle.VehicleDamageEvent;
import org.bukkit.event.vehicle.VehicleExitEvent;
import org.bukkit.event.EventHandler;



public class VehicleListener implements Listener {
	
	SlapHomebrew plugin;
	public static Minecart mEnter;
	
	VehicleListener(SlapHomebrew instance)
	{
	    plugin = instance;
	 }
	@EventHandler
	public void onVehicleDamage(VehicleDamageEvent event) {
		if (event.getVehicle() instanceof Minecart) {
			if(SlapHomebrew.mCarts.contains(event.getVehicle().getUniqueId())){
				event.setCancelled(true);
			}			
		}
	}

	@EventHandler
	public void onVehicleExit(VehicleExitEvent event) {
		if (event.getVehicle() instanceof Minecart) {
			if(SlapHomebrew.mCarts.contains(event.getVehicle().getUniqueId())){
				SlapHomebrew.mCarts.remove(event.getVehicle().getUniqueId());
				event.getVehicle().remove();
			}
		}
	}
}
