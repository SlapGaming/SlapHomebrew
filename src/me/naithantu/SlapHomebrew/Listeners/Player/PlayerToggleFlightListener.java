package me.naithantu.SlapHomebrew.Listeners.Player;

import me.naithantu.SlapHomebrew.Controllers.Extras;
import me.naithantu.SlapHomebrew.Listeners.AbstractListener;
import org.bukkit.GameMode;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.util.Vector;

import java.util.List;

public class PlayerToggleFlightListener extends AbstractListener {
	
	private Extras extras;
	
	public PlayerToggleFlightListener(Extras extras){
		this.extras = extras;
	}
	

	@EventHandler
	public void setFlyOnJump(PlayerToggleFlightEvent event) {
		Player player = event.getPlayer();
		if (player.getWorld().getName().equals("world_start")) {
			if (event.getPlayer().getGameMode() != GameMode.CREATIVE) {
				if(!extras.getHasJumped().contains(player.getName())){
					player.setFlying(false);
					player.playSound(player.getLocation(), Sound.SHOOT_ARROW , 5, -500);
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
