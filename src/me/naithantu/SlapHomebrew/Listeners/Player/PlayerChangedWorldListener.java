package me.naithantu.SlapHomebrew.Listeners.Player;

import me.naithantu.SlapHomebrew.SlapHomebrew;
import me.naithantu.SlapHomebrew.Controllers.Lottery;
import me.naithantu.SlapHomebrew.Controllers.Mail;
import me.naithantu.SlapHomebrew.Controllers.PlayerLogger;
import me.naithantu.SlapHomebrew.Util.Util;

import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;

public class PlayerChangedWorldListener implements Listener {

	private SlapHomebrew plugin;
	private Lottery lottery;
	private Mail mail;
	private PlayerLogger playerLogger;

	public PlayerChangedWorldListener(SlapHomebrew plugin, Lottery lottery, Mail mail, PlayerLogger playerLogger) {
		this.lottery = lottery;
		this.mail = mail;
		this.playerLogger = playerLogger;
		this.plugin = plugin;
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void sonicWorldChange(PlayerChangedWorldEvent event) { 
		final Player player = event.getPlayer(); //Prevent inventory wipe -> Sonic / Sonic|Mini-Games items -> World
		if (!Util.testPermission(player, "gamesinventory")) {
			return;
		}
		String fromWorld = event.getFrom().getName();
		String targetWorld = player.getWorld().getName();
		if (fromWorld.equals("world_sonic") && !targetWorld.equals("world_sonic")) { //Leaving sonic
			Util.runLater(plugin, new Runnable() {
				
				@Override
				public void run() {
					playerLogger.fromSonicWorld(player);
				}
			}, 1);
		} else if (!fromWorld.equals("world_sonic") && targetWorld.equals("world_sonic")) { //Entering sonic
			playerLogger.toSonicWorld(player);
		}
		
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
