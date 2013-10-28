package me.naithantu.SlapHomebrew.Listeners.Entity;

import me.naithantu.SlapHomebrew.Controllers.Jails;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;

public class EntityDamageListener implements Listener {

	private Jails jails;
	
	public EntityDamageListener(Jails jails) {
		this.jails = jails;
	}
	
	@EventHandler
	public void onEntityDamage(EntityDamageEvent event) {
		if (event.getEntity() instanceof Player) {
			//Block if in jail
			if (jails.isInJail(((Player)event.getEntity()).getName())) {
				event.setCancelled(true);
				return;
			}
		}
	}
}
