package me.naithantu.SlapHomebrew.Listeners.Player;

import java.text.SimpleDateFormat;
import java.util.Date;

import me.naithantu.SlapHomebrew.Controllers.AwayFromKeyboard;
import me.naithantu.SlapHomebrew.Controllers.Jails;
import me.naithantu.SlapHomebrew.Controllers.PlayerLogger;
import me.naithantu.SlapHomebrew.Controllers.TabController;
import me.naithantu.SlapHomebrew.Storage.YamlStorage;
import me.naithantu.SlapHomebrew.Util.Util;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerQuitListener implements Listener {
	
	private YamlStorage timeStorage;
	private AwayFromKeyboard afk;
	private Jails jails;
	private PlayerLogger playerLogger;
	private TabController tabController;

	public PlayerQuitListener(YamlStorage timeStorage, AwayFromKeyboard afk, Jails jails, PlayerLogger playerLogger, TabController tabController) {
		this.timeStorage = timeStorage;
		this.afk = afk;
		this.jails = jails;
		this.playerLogger = playerLogger;
		this.tabController = tabController;
	}

	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event) {
		Player player = event.getPlayer();
		String playername = player.getName();
		if (player.hasPermission("slaphomebrew.staff")) {
			String date = new SimpleDateFormat("MMM-d HH:mm:ss z").format(new Date());
			date = date.substring(0, 1).toUpperCase() + date.substring(1);
			Util.dateIntoTimeConfig(date, playername + " logged off.", timeStorage);
			if (!isOnlineStaff(playername))
				Util.dateIntoTimeConfig(date, "There is no staff online!", timeStorage);
		}
		
		//Remove from AFK
		afk.removeAfk(playername);
		
		//Check if player is in jail
		if (jails.isInJail(playername)) {
			jails.switchToOfflineJail(player);
		}
		
		//Log logout time
		playerLogger.setLogoutTime(playername);
		
		//Leave tab
		tabController.playerQuit(player);
		
		//Remove from minechatChecker
		playerLogger.removeFromMoved(playername);
		
		//Remove from last activity
		playerLogger.setLastActivity(playername);
		
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
