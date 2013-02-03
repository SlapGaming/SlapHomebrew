package me.naithantu.SlapHomebrew;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.entity.Witch;
import org.bukkit.entity.Wither;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class BlockListener implements Listener {

	SlapHomebrew plugin;

	BlockListener(SlapHomebrew instance) {
		plugin = instance;
	}

	@EventHandler
	public void onBlockPlace(BlockPlaceEvent event) {
		Player player = event.getPlayer();
		if (SlapHomebrew.blackList.containsKey(event.getBlock().getTypeId())) {
			player.sendMessage(SlapHomebrew.blackList.get(event.getBlock().getTypeId()).toString());
		}
	}

	@EventHandler
	public void onWitherSpawn(CreatureSpawnEvent event) {
		if (event.getEntity() instanceof Wither) {
			if (!event.getLocation().getWorld().getName().equalsIgnoreCase("world_nether")) {
				event.setCancelled(true);
			} else if (event.getSpawnReason().equals(SpawnReason.BUILD_WITHER)) {
				event.setCancelled(true);
			}
		}
	}

	@EventHandler
	public void allowWitchDamage(PotionSplashEvent event) {
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
			if (effect.getType().equals(PotionEffectType.POISON)||effect.getType().equals(PotionEffectType.HARM)) {
				event.setCancelled(true);
			}
		}
	}
	
	@EventHandler
	public void noEnderPvpTp(PlayerTeleportEvent event){
		if(event.getPlayer().getWorld().getName().equals("world_pvp") && event.getCause().equals(TeleportCause.ENDER_PEARL)){
			event.getPlayer().sendMessage(ChatColor.GOLD + "[SLAP]" + ChatColor.WHITE + " Using ender pearls to teleport is not allowed in pvp!");
			event.setCancelled(true);
		}
	}
}
