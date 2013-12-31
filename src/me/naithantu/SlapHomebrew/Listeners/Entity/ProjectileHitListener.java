package me.naithantu.SlapHomebrew.Listeners.Entity;

import me.naithantu.SlapHomebrew.Listeners.AbstractListener;

import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.ProjectileHitEvent;

public class ProjectileHitListener extends AbstractListener {
	
	@EventHandler
	public void onProjectileHit(ProjectileHitEvent event) {
		Entity projectile = event.getEntity();
		if(projectile.hasMetadata("retrobow")){
			projectile.remove();
		}
	}
}
