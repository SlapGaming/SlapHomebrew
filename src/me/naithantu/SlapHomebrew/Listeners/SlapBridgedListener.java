package me.naithantu.SlapHomebrew.Listeners;

import me.naithantu.SlapHomebrew.Commands.Chat.MsgCommand;
import me.naithantu.SlapHomebrew.Listeners.Player.PlayerChatListener;
import me.naithantu.SlapHomebrew.PlayerExtension.PlayerControl;
import me.naithantu.SlapHomebrew.PlayerExtension.SlapPlayer;
import me.naithantu.SlapHomebrew.Util.Util;
import nl.stoux.slapbridged.bukkit.SlapBridged;
import nl.stoux.slapbridged.bukkit.events.BridgedPlayerJoinEvent;
import nl.stoux.slapbridged.bukkit.events.BridgedPlayerMeEvent;
import nl.stoux.slapbridged.bukkit.events.BridgedPlayerMentionEvent;
import nl.stoux.slapbridged.bukkit.events.BridgedPlayerMessageEvent;
import nl.stoux.slapbridged.bukkit.events.BridgedPlayerQuitEvent;
import nl.stoux.slapbridged.bukkit.events.BridgedPlayerWaveEvent;
import nl.stoux.slapbridged.bukkit.events.BridgedServerConnectsEvent;
import nl.stoux.slapbridged.bukkit.events.BridgedServerDisconnectsEvent;
import nl.stoux.slapbridged.bukkit.events.ChatChannelEvent;
import nl.stoux.slapbridged.bukkit.events.ModreqEvent;
import nl.stoux.slapbridged.bukkit.events.ModreqEvent.ModreqType;
import nl.stoux.slapbridged.bukkit.events.ServerJoinGridEvent;
import nl.stoux.slapbridged.bukkit.events.ServerQuitGridEvent;
import nl.stoux.slapbridged.objects.OtherPlayer;
import nl.stoux.slapbridged.objects.OtherServer;

import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;

public class SlapBridgedListener extends AbstractListener {
	
	//Third party
	@EventHandler
	public void onChatChannelMessage(ChatChannelEvent event) {
		plugin.getChatChannels().sendToChannel(event.getChannel(), event.getPlayerName().getPlayername(), event.getMessage());
	}
	
	@EventHandler
	public void onCrossServerModreq(ModreqEvent event) {
		String message = ChatColor.GREEN + "";
		
		//Modreq type
		switch (event.getType()) {
		case NEW:
			message = "New Modreq (ID: #" + event.getFollowID() + ")";
			break;
		case CLAIM:
			message = "Modreq (ID: #" + event.getFollowID() + ") claimed by " + event.getByMod();
			break;
		case DONE:
			message = "Modreq (ID: #" + event.getFollowID() + ") finished by " + event.getByMod();
			break;
		}
		
		//Add on server
		message += " on: " + Util.colorize(event.getServer().getChatPrefix()) + ChatColor.GREEN;
		
		//Add optional text based on type
		if (event.getType() == ModreqType.NEW) {
			message += ChatColor.GREEN + " | By: " + event.getPlayer().getPlayername() + " | Request: " + event.getRequest();
		} else {
			message += ".";
		}
		
		//Message permission holders
		Util.messagePermissionHolders("staff", message); 
	}
	
	//Player connection
	@EventHandler
	public void onBridgedPlayerJoin(BridgedPlayerJoinEvent event) {
		plugin.getTabController().otherServerActivity();
	}
	@EventHandler
	public void onBridgedPlayerQuit(BridgedPlayerQuitEvent event) {
		plugin.getTabController().otherServerActivity();
	}
	@EventHandler
	public void onBridgedServerConnect(BridgedServerConnectsEvent event) {
		plugin.getTabController().otherServerActivity();
	}
	@EventHandler
	public void onBridgedServerDisconnect(BridgedServerDisconnectsEvent event) {
		plugin.getTabController().otherServerActivity();
	}
	@EventHandler
	public void onJoinGrid(ServerJoinGridEvent event) {
		plugin.getTabController().otherServerActivity();
	}
	@EventHandler
	public void onQuitGrid(ServerQuitGridEvent event) {
		plugin.getTabController().otherServerActivity();
	}
	
	//Social
	@EventHandler
	public void onPlayerMention(BridgedPlayerMentionEvent event) {
		PlayerChatListener.getInstance().bridgedMention(event.getPlayer(), event.getServer(), event.getMessage(), null);
	}
	
	@EventHandler
	public void onPlayerUsesMeCommand(BridgedPlayerMeEvent event) {
		//Create message start
		String start = ChatColor.GRAY + " ** ";
		//	=> Add player name
		start += getColoredPlayername(event.getPlayer()) + ChatColor.GRAY + " ";
		
		//Broadcast message
		Util.broadcast( 
			event.getPlayer().hasColoredChat() ? Util.colorize(start + event.getMessage()) : Util.colorize(start) + event.getMessage()
		);
	}
	
	@EventHandler
	public void onPlayerWave(BridgedPlayerWaveEvent event) {
		//Create message start
		String gray = ChatColor.GRAY.toString();
		String message = gray + " ** " + ChatColor.WHITE;
		
		//Add waving player
		message += getColoredPlayername(event.getWavingPlayer());
		
		//Mid-section
		message += ChatColor.GRAY + " waves to ";
		
		//Check if waving to a player or everyone
		if (event.isWaveToEveryone()) {
			//=> Waving to everyone
			message += ChatColor.GOLD + "Everyone";
		} else {
			//=> Waving to a player
			OtherPlayer other = SlapBridged.getAPI().getBridge().getThisServer().getPlayers().get(event.getWavedToPlayer()); //Check if on this server
			if (other == null) { //=> Otherplayer is on a different server
				for (OtherServer server : SlapBridged.getAPI().getOtherServers()) { //=> Loop thru servers
					other = server.getPlayers().get(event.getWavedToPlayer()); //Get player
					if (other != null) { //Found the player, break loop
						break;
					}
				}
			}
			
			//Double check
			if (other == null) {
				//=> Did not find the player. Abort.
				return;
			}
			
			//Add colored name
			message += getColoredPlayername(other);
		}
		
		//Last section & Broadcast
		message += ChatColor.GRAY + " **";
		Util.broadcast(message);
	}
	
	@EventHandler
	public void onPlayerSendMessage(BridgedPlayerMessageEvent event) {
		//Get final message (colored or not)
		String finalMessage = (event.isColorMessage() ? ChatColor.translateAlternateColorCodes('&', event.getMessage()) : event.getMessage());
		
		//Try to find sending player
		SlapPlayer fromPlayer = PlayerControl.getPlayer(event.getFromPlayer());
		if (fromPlayer != null) {
			fromPlayer.setLastReply(event.getToPlayer()); //Set last reply
			fromPlayer.sendMessage("[" + ChatColor.GOLD + "Me" + ChatColor.WHITE + " -> " + ChatColor.GOLD + event.getToPlayer() + ChatColor.WHITE + "] " + finalMessage);
		}
		
		//Try to find receiving player
		SlapPlayer toPlayer = PlayerControl.getPlayer(event.getToPlayer());
		if (toPlayer != null) {
			toPlayer.setLastReply(event.getFromPlayer());
			toPlayer.sendMessage("[" + ChatColor.GOLD + event.getFromPlayer() + ChatColor.WHITE + " -> " + ChatColor.GOLD + "Me" + ChatColor.WHITE + "] " + finalMessage);
		}
		
		//SocialSpy
		MsgCommand.socialSpy(event.getFromPlayer(), event.getToPlayer(), finalMessage);
	}
	
	/**
	 * Get the colored playername of a player
	 * @param player The player
	 */
	public String getColoredPlayername(OtherPlayer player) {
		String name;
		if (player.getPrefix() != null && player.getPrefix().length() > 1) {
			name = Util.colorize(player.getPrefix().substring(0, 2));
		} else {
			name = ChatColor.WHITE.toString();
		}
		name += player.getPlayername();
		return name;
	}
	
}
