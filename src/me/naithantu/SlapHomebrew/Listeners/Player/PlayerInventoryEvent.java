package me.naithantu.SlapHomebrew.Listeners.Player;

import me.naithantu.SlapHomebrew.Controllers.Lottery;
import me.naithantu.SlapHomebrew.Listeners.AbstractListener;
import me.naithantu.SlapHomebrew.PlayerExtension.PlayerControl;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

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
		String UUID = targetPlayer.getUniqueId().toString();

		//Check if player still has lottery prices
        if (lottery.hasStoredPrice(UUID)) {
            //Get the price
            ItemStack price = lottery.getStoredPrice(UUID);
            //=> Try to give the price
            lottery.givePrice(targetPlayer, price, false);
        }
		
		//Set last activity
		PlayerControl.getPlayer(targetPlayer).moved();
	}
	
}
