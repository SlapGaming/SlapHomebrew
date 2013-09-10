package me.naithantu.SlapHomebrew.Listeners.Entity;

import me.naithantu.SlapHomebrew.SlapHomebrew;
import me.naithantu.SlapHomebrew.Controllers.Flag;
import me.naithantu.SlapHomebrew.Util.Util;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Ocelot;
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

		Entity entity = event.getEntity();
		SpawnReason reason = event.getSpawnReason();

		//Block certain mob spawns in creative world.
		if (event.getLocation().getWorld().getName().equals("world_creative")) {
			if (reason == SpawnReason.EGG) {
				event.setCancelled(true);
			} else if (reason == SpawnReason.NATURAL) {
				if (entity instanceof Ocelot) {
					event.setCancelled(true);
				}
			} else if (reason != SpawnReason.NATURAL) {
				//Block mob spawning via dispenser if there are too many mobs nearby in the creative world.
				int totalEntities = 0;
				for (Entity nearbyEntity : entity.getNearbyEntities(50, 50, 50)) {
					if (nearbyEntity instanceof Creature) {
						totalEntities++;
					}
				}

				if (totalEntities >= 50) {
					event.setCancelled(true);
				}
			}
		}

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
			} else if (reason.equals(SpawnReason.BUILD_WITHER)) {
				event.setCancelled(true);
			}
		}
	}
}
