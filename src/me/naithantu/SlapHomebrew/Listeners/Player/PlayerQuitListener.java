package me.naithantu.SlapHomebrew.Listeners.Player;

import me.naithantu.SlapHomebrew.Controllers.*;
import me.naithantu.SlapHomebrew.Listeners.AbstractListener;
import me.naithantu.SlapHomebrew.PlayerExtension.PlayerControl;
import me.naithantu.SlapHomebrew.PlayerExtension.SlapPlayer;
import me.naithantu.SlapHomebrew.Util.Util;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerQuitListener extends AbstractListener {
	
	private AwayFromKeyboard afk;
	private Jails jails;
	private TabController tabController;
	private ChatChannels chatChannels;
	private Homes homes;

	public PlayerQuitListener(AwayFromKeyboard afk, Jails jails, TabController tabController, ChatChannels chatChannels, Homes homes) {
		this.afk = afk;
		this.jails = jails;
		this.tabController = tabController;
		this.chatChannels = chatChannels;
		this.homes = homes;
	}

	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event) {
		Player player = event.getPlayer();
		SlapPlayer slapPlayer = PlayerControl.getPlayer(player);
		String playername = player.getName();
		
		//Remove from AFK
		afk.removeAfk(player);
		
		//Check if player is in jail
		if (jails.isJailed(player.getUniqueId().toString())) {
			jails.jailedPlayerLogsOut(player);
		}
				
		//Leave tab
		tabController.playerQuit(player);
		
		//Leave homes
		homes.playerQuit(player);
		
		//Remove from ChatChannels
		if (chatChannels.isPlayerInChannel(playername)) {
			chatChannels.playerLeaveChannel(player, false);
		}
		
		//Ragequit
		if (slapPlayer.isRageQuit()) {
			event.setQuitMessage(ChatColor.YELLOW + player.getName() + " left in a fit of rage."); //Change quit message
		}
		
		//Remove player from PlayerControl
		PlayerControl.getInstance().removeSlapPlayer(player);

        //Send TabHeader
        Util.runLater(new Runnable() {
            @Override
            public void run() {
                //Send online update
                Util.sendTabHeader();
            }
        }, 1);

    }
}
