package me.naithantu.SlapHomebrew.Listeners;

import me.naithantu.SlapHomebrew.Lottery;

import org.bukkit.Bukkit;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

public class PlayerInventoryEvent implements Listener {
	
	Lottery lottery;
	
	public PlayerInventoryEvent(Lottery lottery) {
		this.lottery = lottery;
	}
	
	@EventHandler
	public void playerChangedInventory(InventoryClickEvent event) {
		HumanEntity targetPlayer = event.getWhoClicked();
		String playerName = targetPlayer.getName();
		Bukkit.getLogger().info(targetPlayer.getName() + "World change");
		if (lottery.inStoredPrices(playerName)) {
			String worldName = targetPlayer.getWorld().getName();
			if (!worldName.equals("world_sonic") && !worldName.equals("world_creative")) {
				if (targetPlayer.getInventory().firstEmpty() != -1) {
					targetPlayer.getInventory().addItem(lottery.getStoredPrice(playerName));
					lottery.removeStoredPrice(playerName);
				}				
			}
		}	
	}
	
}
