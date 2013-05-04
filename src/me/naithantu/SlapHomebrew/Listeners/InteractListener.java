package me.naithantu.SlapHomebrew.Listeners;

import me.naithantu.SlapHomebrew.SlapHomebrew;
import me.naithantu.SlapHomebrew.Commands.SlapCommand;

import org.bukkit.Material;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.metadata.FixedMetadataValue;

public class InteractListener implements Listener{
	SlapHomebrew plugin;
	
	public InteractListener(SlapHomebrew plugin){
		this.plugin = plugin;
	}
	
	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event) {
		Player player = event.getPlayer();
		if (SlapCommand.retroBow.contains(player.getName())) {
			Arrow arrow = player.launchProjectile(Arrow.class);
			arrow.setMetadata("retrobow", new FixedMetadataValue(plugin, true));
		}
		
		if(event.getAction().equals(Action.RIGHT_CLICK_BLOCK)){
			Material clickedBlock = event.getClickedBlock().getType();
			if(clickedBlock == Material.NOTE_BLOCK || clickedBlock == Material.DIODE_BLOCK_ON || clickedBlock == Material.DIODE_BLOCK_OFF){
				if(!(plugin.getWorldGuard().canBuild(event.getPlayer(), event.getClickedBlock()))){
					event.setCancelled(true);
				}
			}
		}
	}
}
