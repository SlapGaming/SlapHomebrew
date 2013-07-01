package me.naithantu.SlapHomebrew.Listeners;

import me.naithantu.SlapHomebrew.Lottery;

import org.bukkit.Bukkit;
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
		Player targetPlayer = event.getPlayer();
		Bukkit.getLogger().info(targetPlayer.getName() + "World change");
		if (lottery.inStoredPrices(targetPlayer.getName())) {
			String targetWorld = targetPlayer.getWorld().getName();
			if (!targetWorld.equals("world_sonic") && !targetWorld.equals("world_creative")) {
				if (targetPlayer.getInventory().firstEmpty() != -1) {
					targetPlayer.getInventory().addItem(lottery.getStoredPrice(targetPlayer.getName()));
					lottery.removeStoredPrice(targetPlayer.getName());
				}				
			}
		}
	}
			
}

