package me.naithantu.SlapHomebrew.Listeners.Player;

import me.naithantu.SlapHomebrew.Controllers.AwayFromKeyboard;
import me.naithantu.SlapHomebrew.Controllers.Jails;
import me.naithantu.SlapHomebrew.Controllers.PlayerLogger;
import me.naithantu.SlapHomebrew.Controllers.TabController;
import me.naithantu.SlapHomebrew.Listeners.AbstractListener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerQuitListener extends AbstractListener {
	
	private AwayFromKeyboard afk;
	private Jails jails;
	private PlayerLogger playerLogger;
	private TabController tabController;

	public PlayerQuitListener(AwayFromKeyboard afk, Jails jails, PlayerLogger playerLogger, TabController tabController) {
		this.afk = afk;
		this.jails = jails;
		this.playerLogger = playerLogger;
		this.tabController = tabController;
	}

	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event) {
		Player player = event.getPlayer();
		String playername = player.getName();
		
		//Remove from AFK
		afk.removeAfk(playername);
		
		//Check if player is in jail
		if (jails.isInJail(playername)) {
			jails.switchToOfflineJail(player);
		}
				
		//Leave tab
		tabController.playerQuit(player);
		
		//Remove from minechatChecker
		playerLogger.removeFromMoved(playername);
		
		//Remove from last activity
		playerLogger.removeFromLastActivity(playername);
		
	}
}
