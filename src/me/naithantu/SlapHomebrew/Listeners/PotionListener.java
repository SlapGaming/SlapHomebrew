package me.naithantu.SlapHomebrew.Listeners;

import org.bukkit.entity.Witch;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class PotionListener extends AbstractListener {
	@EventHandler
	public void blockPotionDamage(PotionSplashEvent event) {
		String worldName = event.getEntity().getLocation().getWorld().getName();
		//If in a pvp world, don't cancel.
		if (worldName.equals("world_pvp") || worldName.equals("world_nether")) {
			return;
		}
		//If caused by witch, don't cancel.
		if (event.getEntity().getShooter() instanceof Witch) {
			return;
		}
		//If damage potion, cancel!
		for (PotionEffect effect : event.getPotion().getEffects()) {
			if (effect.getType().equals(PotionEffectType.POISON) || effect.getType().equals(PotionEffectType.HARM)) {
				event.setCancelled(true);
			}
		}
	}
}
