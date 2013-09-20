package me.naithantu.SlapHomebrew.Listeners.Player;

import me.naithantu.SlapHomebrew.SlapHomebrew;
import me.naithantu.SlapHomebrew.Commands.Basics.BlockfaqCommand;
import me.naithantu.SlapHomebrew.Commands.Staff.MessageCommand;
import me.naithantu.SlapHomebrew.Controllers.AwayFromKeyboard;
import me.naithantu.SlapHomebrew.Controllers.Jails;
import me.naithantu.SlapHomebrew.Controllers.PlayerLogger;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class PlayerChatListener implements Listener {
	private SlapHomebrew plugin;
	private AwayFromKeyboard afk;
	private Jails jails;
	private PlayerLogger playerLogger;
	
	public PlayerChatListener(SlapHomebrew plugin, AwayFromKeyboard afk, Jails jails, PlayerLogger playerLogger){
		this.plugin = plugin;
		this.afk = afk;
		this.jails = jails;
		this.playerLogger = playerLogger;
	}
	@EventHandler
	public void onPlayerChat(AsyncPlayerChatEvent event) {
		String serverMessage;
		Player player = event.getPlayer();
		String playerName = player.getName();
				
		//Block chat if not moved yet
		if (!playerLogger.hasMoved(playerName)) {
			if (event.getMessage().matches("connected with .* using MineChat")) {
				player.kickPlayer("MineChat is not allowed on this server.");
				event.setCancelled(true);
				return;
			}
			event.setCancelled(true);
			playerLogger.sendNotMovedMessage(player);
			return;
		}		
		
		//Block chat while in jail.
		if (jails.isInJail(playerName)) {
			if (!jails.isAllowedToChat(playerName)) {
				event.setCancelled(true);
				player.sendMessage(ChatColor.GRAY + "You are jailed. Use /timeleft to check your time left in jail.");
				return;
			}
		}
		
		String message = event.getMessage().toLowerCase();
		
		//Block "lag" messages
		if (message.equals("lag") || message.equals("lagg")) {
			event.setCancelled(true);
			player.chat("/lag");
			return;
		}
		
		if (!BlockfaqCommand.chatBotBlocks.contains(player.getName())) { //TODO
			if (message.contains("i") && message.contains("can") && message.contains("member") || message.contains("how") && message.contains("get") && message.contains("member")
					|| message.contains("how") && message.contains("become") && message.contains("member")) {
				serverMessage = plugin.getConfig().get("chatmessages.member").toString();
				serverMessage = ChatColor.translateAlternateColorCodes('&', serverMessage);
				player.sendMessage(serverMessage);
				event.setCancelled(true);
			}
			if (message.contains("how") && message.contains("can") && message.contains("vip") || message.contains("how") && message.contains("become") && message.contains("vip")
					|| message.contains("how") && message.contains("get") && message.contains("vip") || message.contains("can") && message.contains("use") && message.contains("tpa")
					|| message.contains("can") && message.contains("get") && message.contains("tp") || message.contains("do") && message.contains("have") && message.contains("tpa")) {
				serverMessage = plugin.getConfig().get("chatmessages.vip").toString();
				serverMessage = ChatColor.translateAlternateColorCodes('&', serverMessage);
				player.sendMessage(serverMessage);
				event.setCancelled(true);
			}
			if (message.contains("where") && message.contains("can") && message.contains("build") || message.contains("where") && message.contains("to") && message.contains("build")) {
				serverMessage = plugin.getConfig().get("chatmessages.build").toString();
				serverMessage = ChatColor.translateAlternateColorCodes('&', serverMessage);
				player.sendMessage(serverMessage);
				event.setCancelled(true);
			}
			if (message.contains("how") && message.contains("protect") && message.contains("house") || message.contains("how") && message.contains("worldguard") && message.contains("house")
					|| message.contains("how") && message.contains("protect") && message.contains("chest") || message.contains("how") && message.contains("lock") && message.contains("chest")
					|| message.contains("how") && message.contains("lock") && message.contains("chests") || message.contains("how") && message.contains("claim") && message.contains("plot")) {
				serverMessage = plugin.getConfig().get("chatmessages.worldguard").toString();
				serverMessage = ChatColor.translateAlternateColorCodes('&', serverMessage);
				player.sendMessage(serverMessage);
			}
			if (message.contains("do") && message.contains("you") && message.contains("lockette") || message.contains("does") && message.contains("server") && message.contains("lockette")) {
				serverMessage = plugin.getConfig().get("chatmessages.lockette").toString();
				serverMessage = ChatColor.translateAlternateColorCodes('&', serverMessage);
				player.sendMessage(serverMessage);
				event.setCancelled(true);
			}
			if (message.contains("how") && message.contains("use") && message.contains("shop") || message.contains("cant") && message.contains("use") && message.contains("shop")
					|| message.contains("how") && message.contains("buy") && message.contains("shop") || message.contains("why") && message.contains("can't") && message.contains("buy")
					|| message.contains("why") && message.contains("cant") && message.contains("buy")) {
				serverMessage = plugin.getConfig().get("chatmessages.shop").toString();
				serverMessage = ChatColor.translateAlternateColorCodes('&', serverMessage);
				player.sendMessage(serverMessage);
				event.setCancelled(true);
			}
			if (message.contains("how") && message.contains("make") && message.contains("money") && message.contains("i") || message.contains("how") && message.contains("do")
					&& message.contains("money") && message.contains("i") || message.contains("how") && message.contains("can") && message.contains("sell") || message.contains("how")
					&& message.contains("do") && message.contains("sell")) {
				serverMessage = plugin.getConfig().get("chatmessages.money").toString();
				serverMessage = ChatColor.translateAlternateColorCodes('&', serverMessage);
				player.sendMessage(serverMessage);
				event.setCancelled(true);
			}
			if (message.contains("how") && message.contains("check") && message.contains("worldguard") || message.contains("how") && message.contains("check") && message.contains("wg")
					|| message.contains("how") && message.contains("check") && message.contains("zones") || message.contains("how") && message.contains("check") && message.contains("zone")) {
				serverMessage = plugin.getConfig().get("chatmessages.checkwg").toString();
				serverMessage = ChatColor.translateAlternateColorCodes('&', serverMessage);
				player.sendMessage(serverMessage);
				event.setCancelled(true);
			}
			if (message.contains("how") && message.contains("i") && message.contains("pay") || message.contains("how") && message.contains("do") && message.contains("pay")) {
				serverMessage = plugin.getConfig().get("chatmessages.pay").toString();
				serverMessage = ChatColor.translateAlternateColorCodes('&', serverMessage);
				player.sendMessage(serverMessage);
				event.setCancelled(true);
			}
		}
		if (plugin.getMessages().contains(player.getName())) {
			message = event.getMessage();
			player.sendMessage(ChatColor.GOLD + "[SLAP] " + ChatColor.WHITE + "The new message has " + MessageCommand.messageName + " as name and " + message + " as message.");
			plugin.getMessages().remove(player.getName());
			plugin.getConfig().set("messages." + MessageCommand.messageName, message);
			event.setCancelled(true);
			plugin.saveConfig();
		}
		
		//Check for AFK
		if (afk.isAfk(playerName)) {
			afk.leaveAfk(playerName);
		}
		
		//DoubleMsg
		if (player.hasPermission("slaphomebrew.staff") && !event.isCancelled()) {
			message = event.getMessage();
			if (playerLogger.hasMessage(playerName)) {
				playerLogger.sendSecondMessage(playerName, message);
				event.setCancelled(true);
			} else {
				int l = message.length();
				if (l > 10) {
					if (message.substring(l - 3).equals("*--")) {
						playerLogger.setFirstMessage(playerName, message);
						event.setCancelled(true);
					}
				}
			}
		}
	}
}
