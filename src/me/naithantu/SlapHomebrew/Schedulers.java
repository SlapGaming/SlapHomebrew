package me.naithantu.SlapHomebrew;

import java.util.Collection;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class Schedulers {
	SlapHomebrew plugin;
	
	public Schedulers(SlapHomebrew plugin){
		this.plugin = plugin;
		removeInvisibility();
		checkFlags();
	}
	
	public void removeInvisibility() {
		Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {
			public void run() {
				for (Player player : Bukkit.getServer().getOnlinePlayers()) {
					Collection<PotionEffect> potionEffects = player.getActivePotionEffects();
					for (PotionEffect effect : potionEffects) {
						if (effect.getType().equals(PotionEffectType.INVISIBILITY)) {
							player.sendMessage(ChatColor.GOLD + "[SLAP]" + ChatColor.WHITE + " Invisibility potions are not allowed, potion effect removed!");
							player.removePotionEffect(PotionEffectType.INVISIBILITY);
						}
					}
				}
			}
		}, 0, 20);
	}
	
	public void checkFlags(){
		Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {
			public void run() {
				for (Player player : Bukkit.getServer().getOnlinePlayers()) {
					Location location = player.getLocation();
					if(Util.hasFlag(plugin, location, Flag.POTION)){
						String flag = Util.getFlag(plugin, location, Flag.POTION);
						String flagPotion = flag.replace("flag:potion(", "").replace(")", "");
						String name = flagPotion.split("_")[0];
						int power = Integer.parseInt(flagPotion.split("_")[1]);
						PotionEffect potion = Util.getPotionEffect(name, 15, power);
						for(PotionEffect potionEffect: player.getActivePotionEffects()){
							if(potionEffect.getType().equals(potion.getType())){
								player.removePotionEffect(potionEffect.getType());
							}
						}
						player.addPotionEffect(potion);
					}
				}
			}
		}, 0, 20);
		
	}
}
