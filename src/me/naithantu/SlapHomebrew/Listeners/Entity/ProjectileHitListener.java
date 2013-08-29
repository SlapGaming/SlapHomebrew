package me.naithantu.SlapHomebrew.Listeners.Entity;

import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;

public class ProjectileHitListener implements Listener{
	@EventHandler
	public void onProjectileHit(ProjectileHitEvent event) {
		Entity projectile = event.getEntity();
		if(projectile.hasMetadata("retrobow")){
			projectile.remove();
		}
	}
}
