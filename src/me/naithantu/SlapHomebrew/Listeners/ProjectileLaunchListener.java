package me.naithantu.SlapHomebrew.Listeners;

import me.naithantu.SlapHomebrew.Util.Util;

import org.bukkit.entity.Player;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileLaunchEvent;

public class ProjectileLaunchListener implements Listener {
	
	@EventHandler
	public void onProjectileLaunch(ProjectileLaunchEvent event) {
		if(event.getEntity().getShooter() instanceof Player) {
			Player player = (Player) event.getEntity().getShooter();
			String worldName = event.getEntity().getLocation().getWorld().getName();
			
			//Cancel throwing potions in creative world.
			if (worldName.equals("world_creative")) {
				if(event.getEntity() instanceof ThrownPotion && !player.hasPermission("slaphomebrew.staff")){
					Util.msg(player, "You're not allowed to throw potions in this world!");
					event.setCancelled(true);
				}
			}
		}
	}
}
