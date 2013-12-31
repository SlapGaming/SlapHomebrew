package me.naithantu.SlapHomebrew.Listeners;

import me.naithantu.SlapHomebrew.Controllers.Horses;

import org.bukkit.ChatColor;
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
				if (horses.hasOwner(targetHorse.getUniqueId().toString())) {
					boolean allowed = horses.enterHorse(targetHorse.getUniqueId().toString(), (Player) event.getEntered());
					if (!allowed && !player.hasPermission("slaphomebrew.staff")) {
						player.sendMessage(ChatColor.RED + "You are not allowed to ride " + horses.getOwner(targetHorse.getUniqueId().toString()) + "'s horse.");
						event.setCancelled(true);
					}
				} else {
					player.sendMessage(ChatColor.GOLD + "[SLAP] " + ChatColor.WHITE + "This horse is not claimed yet. Use '/horse claim' to claim it (if it's yours).");
				}
			}
		}
	}
	
}
