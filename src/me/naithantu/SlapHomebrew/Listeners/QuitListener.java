package me.naithantu.SlapHomebrew.Listeners;

import java.text.SimpleDateFormat;
import java.util.Date;

import me.naithantu.SlapHomebrew.SlapHomebrew;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class QuitListener implements Listener {
	SlapHomebrew plugin;

	public QuitListener(SlapHomebrew plugin) {
		this.plugin = plugin;
	}

	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event) {
		Player player = event.getPlayer();
		if (player.hasPermission("slaphomebrew.staff")) {
			String date = new SimpleDateFormat("MMM-d HH:mm:ss z").format(new Date());
			date = date.substring(0, 1).toUpperCase() + date.substring(1);
			addToConfig(date, player.getName() + " logged off.");
			if (!isOnlineStaff(player.getName()))
				addToConfig(date, "There is no staff online!");
			plugin.saveTimeConfig();
		}
	}

	boolean isOnlineStaff(String leavingPlayer) {
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
	
	void addToConfig(String date, String message){
		int i = 1;
		while(plugin.getTimeConfig().contains(date)){
			date += " (" + i + ")";
			i++;
		}
		plugin.getTimeConfig().set(date, message);
	}
}
