package me.naithantu.SlapHomebrew.Controllers;

import me.naithantu.SlapHomebrew.Runnables.AFKChecker;
import me.naithantu.SlapHomebrew.Runnables.WeatherTask;
import me.naithantu.SlapHomebrew.Util.Util;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Collection;

public class Schedulers extends AbstractController {
	
	public Schedulers() {
		removeInvisibility();
		checkFlags();
		startWeather();
		startAFKChecker();
	}

	private void removeInvisibility() {
		Util.runTimer(new Runnable() {
			public void run() {
				for (Player player : Bukkit.getServer().getOnlinePlayers()) {
					Collection<PotionEffect> potionEffects = player.getActivePotionEffects();
					for (PotionEffect effect : potionEffects) {
						if (effect.getType().equals(PotionEffectType.INVISIBILITY)) {
							if (!(plugin.getExtras().getGhosts().contains(player.getName()))) {
								player.sendMessage(ChatColor.GOLD + "[SLAP]" + ChatColor.WHITE + " Invisibility potions are not allowed, potion effect removed!");
								player.removePotionEffect(PotionEffectType.INVISIBILITY);
							}
						}
					}
				}
			}
		}, 0, 20);
	}

	private void checkFlags() {
		Util.runTimer(new Runnable() {
			public void run() {
				for (Player player : Bukkit.getServer().getOnlinePlayers()) {
					Location location = player.getLocation();
					if (Util.hasFlag(plugin, location, Flag.POTION)) {
						String flag = Util.getFlag(plugin, location, Flag.POTION);
						String flagPotion = flag.replace("flag:potion(", "").replace(")", "");
						String name = flagPotion.split("_")[0];
						int power = Integer.parseInt(flagPotion.split("_")[1]);
						PotionEffect potion = Util.getPotionEffect(name, 15, power);
						for (PotionEffect potionEffect : player.getActivePotionEffects()) {
							if (potionEffect.getType().equals(potion.getType())) {
								player.removePotionEffect(potionEffect.getType());
							}
						}
						player.addPotionEffect(potion);
					}
				}
			}
		}, 0, 20);
	}
	
	private void startWeather() {
		Util.runTimer(new WeatherTask(plugin), 1200, 1200);
	}
	
	private void startAFKChecker() {
		Util.runTimer(new AFKChecker(plugin, plugin.getAwayFromKeyboard(), 5), 6000, 300);
	}
	
    @Override
    public void shutdown() {
    	//Not needed
    }
	
}
