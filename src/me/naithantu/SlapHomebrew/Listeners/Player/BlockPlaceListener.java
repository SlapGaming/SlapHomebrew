package me.naithantu.SlapHomebrew.Listeners.Player;

import me.naithantu.SlapHomebrew.SlapHomebrew;
import me.naithantu.SlapHomebrew.Controllers.Flag;
import me.naithantu.SlapHomebrew.Util.Util;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.metadata.FixedMetadataValue;

public class BlockPlaceListener implements Listener {
	SlapHomebrew plugin;

	public BlockPlaceListener(SlapHomebrew plugin) {
		this.plugin = plugin;
	}

	@EventHandler
	public void onBlockPlace(BlockPlaceEvent event) {
		//Add metadata to skulls placed in the wither arena to find out who made a wither.
		Block block = event.getBlock();
		if (block.getType() == Material.SKULL) {
			if (Util.hasFlag(plugin, block.getLocation(), Flag.ALLOWWITHERSPAWN)) {
				block.setMetadata("slapWitherSkull", new FixedMetadataValue(plugin, event.getPlayer().getName()));
			}
		}
	}
}
