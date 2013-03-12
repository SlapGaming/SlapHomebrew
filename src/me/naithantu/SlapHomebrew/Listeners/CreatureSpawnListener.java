package me.naithantu.SlapHomebrew.Listeners;

import me.naithantu.SlapHomebrew.SlapHomebrew;
import me.naithantu.SlapHomebrew.Util;

import org.bukkit.entity.Wither;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;

public class CreatureSpawnListener implements Listener {
	SlapHomebrew plugin;

	public CreatureSpawnListener(SlapHomebrew plugin) {
		this.plugin = plugin;
	}
	@EventHandler
	public void onWitherSpawn(CreatureSpawnEvent event) {
		if (event.getEntity() instanceof Wither) {
			//Always allow if the allowwitherspawn flag is used.
			if (Util.hasFlag(plugin, event.getLocation(), "allowwitherspawn")) {
				return;
			}
			//If flag isn't used, only allow creation in the nether world.
			if (!event.getLocation().getWorld().getName().equalsIgnoreCase("world_nether")) {
				event.setCancelled(true);
			} else if (event.getSpawnReason().equals(SpawnReason.BUILD_WITHER)) {
				event.setCancelled(true);
			}
		}
	}
}
