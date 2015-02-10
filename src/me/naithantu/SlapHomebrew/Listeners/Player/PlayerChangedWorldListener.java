package me.naithantu.SlapHomebrew.Listeners.Player;

import me.naithantu.SlapHomebrew.Controllers.Lottery;
import me.naithantu.SlapHomebrew.Controllers.Mail;
import me.naithantu.SlapHomebrew.Listeners.AbstractListener;

import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.inventory.ItemStack;

public class PlayerChangedWorldListener extends AbstractListener {

	private Lottery lottery;
	private Mail mail;

	public PlayerChangedWorldListener(Lottery lottery, Mail mail) {
		this.lottery = lottery;
		this.mail = mail;
	}
	
	@EventHandler
	public void playerChangedWorld(PlayerChangedWorldEvent event) {
		Player player = event.getPlayer();
        String UUID = player.getUniqueId().toString();

        //Check if the player still has lottery price
        if (lottery.hasStoredPrice(UUID)) {
            //Get the price
            ItemStack price = lottery.getStoredPrice(UUID);
            //=> Try to give the price
            lottery.givePrice(player, price, false);
        }

		//Allow flight for double jumping in start world.
		if (player.getWorld().getName().equals("world_start")) {
			player.setAllowFlight(true);
		} else if (event.getFrom().getName().equals("world_start") && !player.getWorld().getName().equals("world_start") && player.getGameMode() != GameMode.CREATIVE) {
			player.setAllowFlight(false);
		}
		
		//Check if the player has new mail
		mail.hasNewMail(player);

        //Reset the player time
        player.resetPlayerTime();
	}

}
