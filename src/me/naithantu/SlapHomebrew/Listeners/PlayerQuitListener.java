package me.naithantu.SlapHomebrew.Listeners;

import java.text.SimpleDateFormat;
import java.util.Date;

import me.naithantu.SlapHomebrew.AwayFromKeyboard;
import me.naithantu.SlapHomebrew.Jails;
import me.naithantu.SlapHomebrew.Util;
import me.naithantu.SlapHomebrew.Storage.YamlStorage;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerQuitListener implements Listener {
	
	private YamlStorage timeStorage;
	private AwayFromKeyboard afk;
	private Jails jails;

	public PlayerQuitListener(YamlStorage timeStorage, AwayFromKeyboard afk, Jails jails) {
		this.timeStorage = timeStorage;
		this.afk = afk;
		this.jails = jails;
	}

	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event) {
		Player player = event.getPlayer();
		if (player.hasPermission("slaphomebrew.staff")) {
			String date = new SimpleDateFormat("MMM-d HH:mm:ss z").format(new Date());
			date = date.substring(0, 1).toUpperCase() + date.substring(1);
			Util.dateIntoTimeConfig(date, player.getName() + " logged off.", timeStorage);
			if (!isOnlineStaff(player.getName()))
				Util.dateIntoTimeConfig(date, "There is no staff online!", timeStorage);
		}
		
		//Remove from AFK
		afk.removeAfk(player.getName());
		
		//Check if player is in jail
		if (jails.isInJail(player.getName())) {
			jails.switchToOfflineJail(player);
		}
		
	}

	private boolean isOnlineStaff(String leavingPlayer) {
		int onlineStaff = 0;
		for (Player player : Bukkit.getServer().getOnlinePlayers()) {
			if (player.hasPermission("slaphomebrew.staff") && !player.getName().equals(leavingPlayer)) {
				onlineStaff++;
			}
		}
		if (onlineStaff > 0)
			return true;
		return false;
	}
}
