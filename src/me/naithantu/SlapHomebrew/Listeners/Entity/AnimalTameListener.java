package me.naithantu.SlapHomebrew.Listeners.Entity;

import me.naithantu.SlapHomebrew.Controllers.Horses;
import me.naithantu.SlapHomebrew.Listeners.AbstractListener;

import me.naithantu.SlapHomebrew.Util.Util;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityTameEvent;

public class AnimalTameListener extends AbstractListener {
	
	private Horses horses;
	
	public AnimalTameListener(Horses horses) {
		this.horses = horses;
	}

	@EventHandler
	public void tameEvent(EntityTameEvent event) {
		Entity entity = event.getEntity();
		if (entity instanceof Horse) {
            horses.onTameEvent((Player) event.getOwner(), (Horse) event.getEntity());
            Util.msg((Player) event.getOwner(), "This horse is now yours!");
        }
	}

}
