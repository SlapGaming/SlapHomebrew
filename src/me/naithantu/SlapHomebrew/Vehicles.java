package me.naithantu.SlapHomebrew;

import java.util.HashSet;
import java.util.UUID;

import org.bukkit.entity.Boat;
import org.bukkit.entity.Minecart;

public class Vehicles {
	HashSet<UUID> minecarts = new HashSet<UUID>();
	HashSet<UUID> boats = new HashSet<UUID>();

	public Vehicles() {
	}

	public void addMinecart(Minecart minecart) {
		minecarts.add(minecart.getUniqueId());
	}
	
	public void removeMinecart(Minecart minecart){
		minecarts.remove(minecart.getUniqueId());
	}

	public boolean isMinecart(Minecart minecart) {
		if (minecarts.contains(minecart.getUniqueId()))
			return true;
		return false;
	}

	public void addBoat(Boat boat) {
		boats.add(boat.getUniqueId());
	}
	
	public void removeBoat(Boat boat){
		boats.remove(boat.getUniqueId());
	}

	public boolean isBoat(Boat boat) {
		if (boats.contains(boat.getUniqueId()))
			return true;
		return false;
	}
}
