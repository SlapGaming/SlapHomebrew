package me.naithantu.SlapHomebrew.Listeners.Player;

import me.naithantu.SlapHomebrew.Commands.SlapCommand;
import me.naithantu.SlapHomebrew.Controllers.Jails;
import me.naithantu.SlapHomebrew.Controllers.SpartaPads;
import me.naithantu.SlapHomebrew.Controllers.SpartaPads.SpartaPad;
import me.naithantu.SlapHomebrew.Listeners.AbstractListener;
import me.naithantu.SlapHomebrew.PlayerExtension.PlayerControl;
import me.naithantu.SlapHomebrew.Util.Util;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;

public class PlayerInteractListener extends AbstractListener {

	private Jails jails;
	private SpartaPads spartaPads;

	public PlayerInteractListener(Jails jails, SpartaPads spartaPads) {
		this.jails = jails;
		this.spartaPads = spartaPads;
	}
	
	@SuppressWarnings("deprecation")
	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event) {
		final Player player = event.getPlayer();
		final String playername = player.getName();
		
		//Set last activity
		PlayerControl.getPlayer(player).moved();
		
		//Block if in jail
		if (jails.isJailed(player.getUniqueId().toString())) {
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

                //Deny changing a mob spawner
                if (clickedBlock == Material.MOB_SPAWNER) {
                    if (player.getItemInHand().getType() == Material.MONSTER_EGG || player.getItemInHand().getType() == Material.MONSTER_EGG) {
                        if (!player.isOp()) {
                            Util.badMsg(player, "You are not allowed to change a mob spawner.");
                            event.setCancelled(true);
                        }
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
		} else if (event.getAction() == Action.PHYSICAL) { //If Physical action
			Material material = event.getClickedBlock().getType();
			if (material == Material.WOOD_PLATE || material == Material.STONE_PLATE || material == Material.IRON_PLATE || material == Material.GOLD_PLATE) { //Check if on a plate
				Location blockLocation = event.getClickedBlock().getLocation();
				if (spartaPads.isSpartaPad(blockLocation)) { //If a spartaPad
					SpartaPad pad = spartaPads.getSpartaPad(blockLocation);
					pad.launch(player); //Launch the player
				}
			}
		}
	}
}
