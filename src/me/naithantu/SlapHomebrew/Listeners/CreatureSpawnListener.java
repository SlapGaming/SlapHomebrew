package me.naithantu.SlapHomebrew.Listeners;

import me.naithantu.SlapHomebrew.Flag;
import me.naithantu.SlapHomebrew.SlapHomebrew;
import me.naithantu.SlapHomebrew.Util;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Wither;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.metadata.FixedMetadataValue;

public class CreatureSpawnListener implements Listener {
	SlapHomebrew plugin;

	public CreatureSpawnListener(SlapHomebrew plugin) {
		this.plugin = plugin;
	}
	@EventHandler
	public void onCreatureSpawn(CreatureSpawnEvent event) {
		if (event.getLocation().getWorld().getName().equals("world_creative")) {
			if (event.getSpawnReason() == SpawnReason.EGG) {
				event.setCancelled(true);
			}
		}
		
		Entity entity = event.getEntity();

		if (entity instanceof Wither) {
			//Always allow if the allowwitherspawn flag is used.
			if (Util.hasFlag(plugin, event.getLocation(), Flag.ALLOWWITHERSPAWN)) {
				Location location = entity.getLocation();
				//Add 2 to y to get the location of the center wither skull.
				location.add(0, 2, 0);
				Block block = location.getBlock();
				if (block.hasMetadata("slapWitherSkull")) {
					String witherCreator = block.getMetadata("slapWitherSkull").get(0).asString();
					entity.setMetadata("slapWither", new FixedMetadataValue(plugin, witherCreator));
					System.out.println("[SLAP] Created slap wither, creator: " + witherCreator);
				}
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
