package me.naithantu.SlapHomebrew.Listeners.Player;

import me.naithantu.SlapHomebrew.Controllers.Lottery;
import me.naithantu.SlapHomebrew.Controllers.Mail;
import me.naithantu.SlapHomebrew.Listeners.AbstractListener;

import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerChangedWorldEvent;

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
		if (lottery.inStoredPrices(player.getName())) {
			String targetWorld = player.getWorld().getName();
			if (!targetWorld.equals("world_sonic") && !targetWorld.equals("world_creative")) {
				if (player.getInventory().firstEmpty() != -1) {
					player.getInventory().addItem(lottery.getStoredPrice(player.getName()));
					lottery.removeStoredPrice(player.getName());
				}
			}
		}

		//Allow flight for double jumping in start world.
		if (player.getWorld().getName().equals("world_start")) {
			player.setAllowFlight(true);
		} else if (event.getFrom().getName().equals("world_start") && !player.getWorld().getName().equals("world_start") && player.getGameMode() != GameMode.CREATIVE) {
			player.setAllowFlight(false);
		}
		
		//Check if the player has new mail
		mail.hasNewMail(player);
	}

}
