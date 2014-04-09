package me.naithantu.SlapHomebrew.Listeners;

import me.naithantu.SlapHomebrew.Util.Util;
import nl.stoux.slapbridged.bukkit.events.BridgedPlayerChatEvent;
import nl.stoux.slapbridged.bukkit.events.BridgedPlayerJoinEvent;
import nl.stoux.slapbridged.bukkit.events.BridgedPlayerQuitEvent;
import nl.stoux.slapbridged.bukkit.events.BridgedServerConnectsEvent;
import nl.stoux.slapbridged.bukkit.events.BridgedServerDisconnectsEvent;
import nl.stoux.slapbridged.bukkit.events.ChatChannelEvent;
import nl.stoux.slapbridged.bukkit.events.NewModreqEvent;
import nl.stoux.slapbridged.bukkit.events.ServerJoinGridEvent;
import nl.stoux.slapbridged.bukkit.events.ServerQuitGridEvent;

import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;

public class SlapBridgedListener extends AbstractListener {
	
	//Third party
	@EventHandler
	public void onChatChannelMessage(ChatChannelEvent event) {
		plugin.getChatChannels().sendToChannel(event.getChannel(), event.getPlayerName().getPlayername(), event.getMessage());
	}
	
	@EventHandler
	public void onCrossServerModreq(NewModreqEvent event) {
		Util.messagePermissionHolders("staff", ChatColor.GREEN + "New Modreq on: " + ChatColor.translateAlternateColorCodes('&', event.getServer().getChatPrefix()) + ChatColor.GREEN + " | By: " + event.getPlayer().getPlayername() + " | Request: " + event.getRequest());
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
	
	
	
	//Chat
	@EventHandler
	public void onBridgedPlayerChat(BridgedPlayerChatEvent event) {
		//TODO Deal with @Mention
	}
}
