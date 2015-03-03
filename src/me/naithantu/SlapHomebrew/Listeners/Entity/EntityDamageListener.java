package me.naithantu.SlapHomebrew.Listeners.Entity;

import me.naithantu.SlapHomebrew.Controllers.Jails;
import me.naithantu.SlapHomebrew.Listeners.AbstractListener;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent;

public class EntityDamageListener extends AbstractListener {

	private Jails jails;
	
	public EntityDamageListener(Jails jails) {
		this.jails = jails;
	}
	
	@EventHandler
	public void onEntityDamage(EntityDamageEvent event) {
		if (event.getEntity() instanceof Player) {
			//Block if in jail
			if (jails.isJailed(((Player)event.getEntity()).getUniqueId().toString())) {
				event.setCancelled(true);
				return;
			}
		}
	}
}
