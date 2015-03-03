package me.naithantu.SlapHomebrew.Listeners.Player;

import me.naithantu.SlapHomebrew.Listeners.AbstractListener;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.event.world.PortalCreateEvent;

public class PlayerPortalListener extends AbstractListener {

	@EventHandler
	public void onPortalTeleport(PlayerPortalEvent event){
		if(event.getCause() == TeleportCause.END_PORTAL && event.getFrom().getWorld().getName().equals("world_the_end")){
			Location spawn = Bukkit.getServer().getWorld("world_start").getSpawnLocation();
			event.setTo(spawn);
		}
	}
	
	
	@EventHandler
	public void onPortalCreate(PortalCreateEvent event){
		event.setCancelled(true);
	}
}
