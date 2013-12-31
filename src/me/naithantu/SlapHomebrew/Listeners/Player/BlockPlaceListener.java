package me.naithantu.SlapHomebrew.Listeners.Player;

import me.naithantu.SlapHomebrew.Controllers.Flag;
import me.naithantu.SlapHomebrew.Listeners.AbstractListener;
import me.naithantu.SlapHomebrew.Util.Util;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.metadata.FixedMetadataValue;

public class BlockPlaceListener extends AbstractListener {
	
	@EventHandler
	public void onBlockPlace(BlockPlaceEvent event) {
		// Add metadata to skulls placed in the wither arena to find out who
		// made a wither.
		Block block = event.getBlock();
		if (block.getType() == Material.SKULL) {
			if (Util.hasFlag(plugin, block.getLocation(), Flag.ALLOWWITHERSPAWN)) {
				block.setMetadata("slapWitherSkull", new FixedMetadataValue(
						plugin, event.getPlayer().getName()));
			}
		}
	}

	@SuppressWarnings("deprecation")
	@EventHandler
	public void onLogPlace(BlockPlaceEvent event) {
		Material material = event.getItemInHand().getType();
		if (material == Material.LOG || material == Material.LOG_2) {
			int materialData = event.getItemInHand().getData().getData();
			Block block = event.getBlock();
			switch (materialData) {
			case 12:
				block.setData((byte) 12);
				break;
			case 13:
				block.setData((byte) 13);
				break;
			case 14:
				block.setData((byte) 14);
				break;
			case 15:
				block.setData((byte) 15);
				break;
			default:
				break;
			}
		}
	}
}
