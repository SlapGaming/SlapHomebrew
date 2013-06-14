package me.naithantu.SlapHomebrew.Listeners;

import java.util.List;

import me.naithantu.SlapHomebrew.Extras;

import org.bukkit.GameMode;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.util.Vector;

public class ToggleFlightListener implements Listener {
	Extras extras;
	
	public ToggleFlightListener(Extras extras){
		this.extras = extras;
	}
	

	@EventHandler
	public void setFlyOnJump(PlayerToggleFlightEvent event) {
		Player player = event.getPlayer();
		if (player.getWorld().getName().equals("world_start")) {
			if (event.isFlying() && event.getPlayer().getGameMode() != GameMode.CREATIVE) {
				if(!extras.getHasJumped().contains(player.getName())){
					player.setFlying(false);
					player.playSound(player.getLocation(), Sound.SHOOT_ARROW , 5, -5);
					Vector jump = player.getLocation().getDirection().multiply(0.4).setY(1.2);
					player.setVelocity(player.getVelocity().add(jump));
					List<String> hasJumped = extras.getHasJumped();
					hasJumped.add(player.getName());
					extras.setHasJumped(hasJumped);
					player.setAllowFlight(false);
					event.setCancelled(true);
				}
			}
		}
	}
}
