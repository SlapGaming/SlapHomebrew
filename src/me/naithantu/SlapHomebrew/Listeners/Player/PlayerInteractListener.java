package me.naithantu.SlapHomebrew.Listeners.Player;

import me.naithantu.SlapHomebrew.Commands.SlapCommand;
import me.naithantu.SlapHomebrew.Controllers.Horses;
import me.naithantu.SlapHomebrew.Controllers.Jails;
import me.naithantu.SlapHomebrew.Controllers.PlayerLogger;
import me.naithantu.SlapHomebrew.Listeners.AbstractListener;
import me.naithantu.SlapHomebrew.Util.Util;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LeashHitch;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;

public class PlayerInteractListener extends AbstractListener {
	
	private Horses horses;
	private Jails jails;
	private PlayerLogger playerLogger;

	public PlayerInteractListener(Horses horses, Jails jails, PlayerLogger playerLogger) {
		this.horses = horses;
		this.jails = jails;
		this.playerLogger = playerLogger;
	}
	
	@SuppressWarnings("deprecation")
	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event) {
		final Player player = event.getPlayer();
		final String playername = player.getName();
		
		//Set last activity
		playerLogger.setLastActivity(playername);
		
		//Block if in jail
		if (jails.isInJail(playername)) {
			event.setCancelled(true);
			return;
		}
		
		if (SlapCommand.retroBow.contains(playername)) {
			Arrow arrow = player.launchProjectile(Arrow.class);
			arrow.setMetadata("retrobow", new FixedMetadataValue(plugin, true));
		}
		
		if (event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
			Material clickedBlock = null;
			try {
				clickedBlock = event.getClickedBlock().getType();
				
				if (clickedBlock == Material.NOTE_BLOCK || clickedBlock == Material.DIODE_BLOCK_ON || clickedBlock == Material.DIODE_BLOCK_OFF || clickedBlock == Material.CAKE_BLOCK || clickedBlock == Material.LEASH) {
					if (!(plugin.getworldGuard().canBuild(event.getPlayer(), event.getClickedBlock()))) {
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
									if (leashFence instanceof LeashHitch) {
										if (leashFence.getLocation().equals(loc)) {
											horses.placedLeashOnFence(leashFence.getUniqueId().toString(), playername);
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
					if (!(plugin.getworldGuard().canBuild(event.getPlayer(), event.getClickedBlock()))) {
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

					if (totalEntities >= 50) {
						event.setCancelled(true);
						player.sendMessage(Util.getHeader() + "There are too many mobs in this area, you may not spawn more!");
						return;
					}
				}
			}
		}		
	}
}
