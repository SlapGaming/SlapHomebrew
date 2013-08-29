package me.naithantu.SlapHomebrew.Listeners.Player;

import me.naithantu.SlapHomebrew.Controllers.Lottery;

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
