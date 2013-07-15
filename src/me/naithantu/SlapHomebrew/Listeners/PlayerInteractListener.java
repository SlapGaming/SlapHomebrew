package me.naithantu.SlapHomebrew.Listeners;

import me.naithantu.SlapHomebrew.Horses;
import me.naithantu.SlapHomebrew.SlapHomebrew;
import me.naithantu.SlapHomebrew.Util;
import me.naithantu.SlapHomebrew.Commands.SlapCommand;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_6_R2.entity.CraftHanging;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;

public class PlayerInteractListener implements Listener {
	SlapHomebrew plugin;
	Horses horses;

	public PlayerInteractListener(SlapHomebrew plugin, Horses horses) {
		this.plugin = plugin;
		this.horses = horses;
	}

	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event) {
		final Player player = event.getPlayer();
		if (SlapCommand.retroBow.contains(player.getName())) {
			Arrow arrow = player.launchProjectile(Arrow.class);
			arrow.setMetadata("retrobow", new FixedMetadataValue(plugin, true));
		}
		
		if (event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
			Material clickedBlock = null;
			try {
				clickedBlock = event.getClickedBlock().getType();
				
				if (clickedBlock == Material.NOTE_BLOCK || clickedBlock == Material.DIODE_BLOCK_ON || clickedBlock == Material.DIODE_BLOCK_OFF || clickedBlock == Material.CAKE_BLOCK || clickedBlock == Material.LEASH) {
					if (!(plugin.getWorldGuard().canBuild(event.getPlayer(), event.getClickedBlock()))) {
						event.setCancelled(true);
					}
				}
				
				if (clickedBlock == Material.FENCE) {
					if (event.getItem().getType() == Material.LEASH) {
						final Location loc = event.getClickedBlock().getLocation();
						loc.setX(loc.getX() + 0.5);
						loc.setY(loc.getY() + 0.5);
						loc.setZ(loc.getZ() + 0.5);
						Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
							
							@Override
							public void run() {
								for (Entity leashFence : player.getWorld().getEntities()) {
									if (leashFence instanceof CraftHanging) {
										if (leashFence.getLocation().equals(loc)) {
											horses.placedLeashOnFence(leashFence.getUniqueId().toString(), player.getName());
										}
									}
								}
								
							}
						}, 1);
					}
				}
				
			} catch (NullPointerException e) {
				System.out.println("Nullpointer caught " + event.getClickedBlock());
			}


			if(event.getPlayer().getWorld().getName().equals("world_creative")){
				ItemStack itemInHand = event.getItem();
				if (itemInHand != null && itemInHand.getType() == Material.MONSTER_EGG) {
					//Block all monster spawns.
					if (itemInHand.getData().getData() < 90) {
						event.setCancelled(true);
						player.sendMessage(Util.getHeader() + "You may not spawn hostile mobs!");
						return;
					}

					//Block if player has no build rights.
					if (!(plugin.getWorldGuard().canBuild(event.getPlayer(), event.getClickedBlock()))) {
						event.setCancelled(true);
						player.sendMessage(Util.getHeader() + "You may not spawn mobs in this area!");
						return;
					}

					//Check number of mobs around.
					int totalEntities = 0;
					for (Entity nearbyEntity : player.getNearbyEntities(50, 50, 50)) {
						if (nearbyEntity instanceof Creature) {
							totalEntities++;
						}
					}

					if (totalEntities > 50) {
						event.setCancelled(true);
						player.sendMessage(Util.getHeader() + "There are too many mobs in this area, you may not spawn more!");
						return;
					}
				}
			}
		}		
	}
}
