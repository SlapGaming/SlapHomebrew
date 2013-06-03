package me.naithantu.SlapHomebrew.Listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;

public class DamageListener implements Listener {

	@EventHandler
	public void onEntityDamage(EntityDamageEvent event) {
		if (event.getEntity() instanceof Player) {
			Player player = (Player) event.getEntity();
			if(player.getWorld().getName().equalsIgnoreCase("world_sonic")){
				if(player.getInventory().getBoots() != null){
					ItemStack boots = player.getInventory().getBoots();
					boots.setDurability((short) 0);
				}
			}
		}
	}
}
