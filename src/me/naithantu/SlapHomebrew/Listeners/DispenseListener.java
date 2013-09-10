package me.naithantu.SlapHomebrew.Listeners;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDispenseEvent;

public class DispenseListener implements Listener {
	@EventHandler
	public void onBlockDispenseEvent(BlockDispenseEvent event) {
		if(!event.getBlock().getWorld().getName().equals("world_creative")){
			if(event.getItem().getType() == Material.LAVA_BUCKET || event.getItem().getType() == Material.TNT){
				event.setCancelled(true);
			}
		}
		
		Material type = event.getItem().getType();
		if (type == Material.FIREBALL || type == Material.FIRE || type == Material.FLINT_AND_STEEL) {
			event.setCancelled(true);
		}
		
	}
}
