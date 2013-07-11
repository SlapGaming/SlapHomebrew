package me.naithantu.SlapHomebrew.Listeners;

import me.naithantu.SlapHomebrew.Horses;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Horse;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityTameEvent;

public class AnimalTameListener implements Listener {
	
	private Horses horses;
	
	public AnimalTameListener(Horses horses) {
		this.horses = horses;
	}

	@EventHandler
	public void tameEvent(EntityTameEvent event) {
		Entity entity = event.getEntity();
		if (entity instanceof Horse) {
			horses.tamedHorse(entity.getUniqueId().toString(), event.getOwner().getName());
		}
	}

}
