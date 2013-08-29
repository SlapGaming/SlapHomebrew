package me.naithantu.SlapHomebrew.Listeners.Player;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.FoodLevelChangeEvent;

public class FoodLevelChangeListener implements Listener {

	@EventHandler
	public void onFoodLevelChange(FoodLevelChangeEvent event){
		if(event.getEntity().getWorld().getName().equalsIgnoreCase("world_sonic"))
			event.setCancelled(true);
	}
}
