package me.naithantu.SlapHomebrew.Listeners.Player;

import me.naithantu.SlapHomebrew.Controllers.Lottery;
import me.naithantu.SlapHomebrew.Listeners.AbstractListener;
import me.naithantu.SlapHomebrew.PlayerExtension.PlayerControl;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryClickEvent;

public class PlayerInventoryEvent extends AbstractListener {
	
	private Lottery lottery;
	
	public PlayerInventoryEvent(Lottery lottery) {
		this.lottery = lottery;
	}
	
	@EventHandler
	public void playerChangedInventory(InventoryClickEvent event) {
		if (!(event.getWhoClicked() instanceof Player)) { //Check if a player
			return;
		}
		Player targetPlayer = (Player) event.getWhoClicked();
		String playerName = targetPlayer.getName();
		
		//Check if player still has lottery prices
		if (lottery.inStoredPrices(playerName)) {
			String worldName = targetPlayer.getWorld().getName();
			if (!worldName.equals("world_sonic") && !worldName.equals("world_creative")) {
				if (targetPlayer.getInventory().firstEmpty() != -1) {
					targetPlayer.getInventory().addItem(lottery.getStoredPrice(playerName));
					lottery.removeStoredPrice(playerName);
				}				
			}
		}	
		
		//Set last activity
		PlayerControl.getPlayer(targetPlayer).moved();
	}
	
}
