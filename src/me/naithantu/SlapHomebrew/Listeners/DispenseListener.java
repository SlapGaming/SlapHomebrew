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
		
		if (event.getItem().getType() == Material.FIREBALL) {
			event.setCancelled(true);
		}
	}
}
