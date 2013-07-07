package me.naithantu.SlapHomebrew.Listeners;

import me.naithantu.SlapHomebrew.Lottery;

import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;

public class PlayerChangedWorldListener implements Listener {

	Lottery lottery;

	public PlayerChangedWorldListener(Lottery lottery) {
		this.lottery = lottery;
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
	}

}