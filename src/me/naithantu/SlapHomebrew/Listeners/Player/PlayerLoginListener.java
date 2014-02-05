package me.naithantu.SlapHomebrew.Listeners.Player;

import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerLoginEvent.Result;

import me.naithantu.SlapHomebrew.Controllers.Whitelist;
import me.naithantu.SlapHomebrew.Listeners.AbstractListener;

public class PlayerLoginListener extends AbstractListener {

	private Whitelist whitelist;
	
	public PlayerLoginListener(Whitelist whitelist) {
		this.whitelist = whitelist;
	}
	
	@EventHandler
	public void onPlayerLogin(PlayerLoginEvent event) {
		if (whitelist.isWhitelistOn()) { //If whitelist is on
			String playername = event.getPlayer().getName();
			if (!whitelist.isWhitelisted(playername)) { //If not whitelisted
				event.disallow(Result.KICK_WHITELIST, whitelist.getWhitelistMessage()); //Disallow
			}
		}
	}

}
