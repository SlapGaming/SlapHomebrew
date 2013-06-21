package me.naithantu.SlapHomebrew.Listeners;

import me.naithantu.SlapHomebrew.Flag;
import me.naithantu.SlapHomebrew.SlapHomebrew;
import me.naithantu.SlapHomebrew.Util;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Animals;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Wither;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.metadata.FixedMetadataValue;

import com.sk89q.worldguard.protection.regions.ProtectedRegion;

public class CreatureSpawnListener implements Listener {
	SlapHomebrew plugin;

	public CreatureSpawnListener(SlapHomebrew plugin) {
		this.plugin = plugin;
	}
	@EventHandler
	public void onCreatureSpawn(CreatureSpawnEvent event) {
		if (event.getLocation().getWorld().getName().equals("world_creative")) {
			if (event.getSpawnReason() == SpawnReason.SPAWNER_EGG) {
				event.setCancelled(true);
			}
			
			if(event.getSpawnReason() == SpawnReason.EGG){
				event.setCancelled(true);
			}
		}
		
		//Block egg spawning in creative world
		/*if (event.getLocation().getWorld().getName().equals("world_creative")) {
			if (event.getSpawnReason() == SpawnReason.SPAWNER_EGG) {
				if (event.getEntity() instanceof Animals) {
					Player player = null;
					World world = event.getLocation().getWorld();
					boolean canBuild = false;
					for (Entity entity : event.getEntity().getNearbyEntities(20, 20, 20)) {
						if (entity instanceof Player) {
							if (plugin.getWorldGuard().canBuild((Player) entity, entity.getLocation())) {
								player = (Player) entity;
								canBuild = true;
								break;
							}
						}
					}

					if (canBuild) {
						int totalEntities = 0;
						for (ProtectedRegion region : plugin.getWorldGuard().getRegionManager(event.getLocation().getWorld()).getApplicableRegions(event.getLocation())) {		
							int x1 = region.getMinimumPoint().getBlockX() >> 4;
							int z1 = region.getMinimumPoint().getBlockZ() >> 4;
							int x2 = region.getMaximumPoint().getBlockX() >> 4;
							int z2 = region.getMaximumPoint().getBlockZ() >> 4;
						
							player.sendMessage("Testing region: " + region.getId());

							Chunk chunk = world.getChunkAt(player.getLocation());
							for(Entity entity: chunk.getEntities()){
								if(entity instanceof Creature) {
						    		player.sendMessage("test In worldguard: " + region.getId() + ". " + entity.getType().getName());
								}
					    	}
							
							for(int x = x1; x < x2; x+= 16) {
							    for(int z = z1; z < z2; z+= 16) {
									player.sendMessage("X: " + x + " Z: " + z);
							    	for(Entity entity: world.getChunkAt(x,z).getEntities()){
										if(entity instanceof Creature) {
								    		player.sendMessage("In worldguard: " + region.getId() + ". " + entity.getType().getName());
										}
							    	}
							        totalEntities += world.getChunkAt(x, z).getEntities().length;
							    }
							}
						}
						
						player.sendMessage("There are " + totalEntities + " in this region.");

						if(totalEntities < 50){
							return;
						}
					}
					event.setCancelled(true);
				}
			}
		}*/

		if (event.getEntity() instanceof Wither) {
			//Always allow if the allowwitherspawn flag is used.
			if (Util.hasFlag(plugin, event.getLocation(), Flag.ALLOWWITHERSPAWN)) {
				Location location = event.getEntity().getLocation();
				//Add 2 to y to get the location of the center wither skull.
				location.add(0, 2, 0);
				Block block = location.getBlock();
				if (block.hasMetadata("slapWitherSkull")) {
					String witherCreator = block.getMetadata("slapWitherSkull").get(0).asString();
					event.getEntity().setMetadata("slapWither", new FixedMetadataValue(plugin, witherCreator));
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
