package me.naithantu.SlapHomebrew.Listeners.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import me.naithantu.SlapHomebrew.Controllers.AwayFromKeyboard;
import me.naithantu.SlapHomebrew.Controllers.ChatChannels;
import me.naithantu.SlapHomebrew.Controllers.Jails;
import me.naithantu.SlapHomebrew.Controllers.Mention;
import me.naithantu.SlapHomebrew.Controllers.MessageFactory;
import me.naithantu.SlapHomebrew.Controllers.PlayerLogger;
import me.naithantu.SlapHomebrew.Listeners.AbstractListener;
import me.naithantu.SlapHomebrew.Util.Log;
import me.naithantu.SlapHomebrew.Util.Util;

import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import ru.tehkode.permissions.PermissionUser;
import ru.tehkode.permissions.bukkit.PermissionsEx;

public class PlayerChatListener extends AbstractListener {
	
	private AwayFromKeyboard afk;
	private Jails jails;
	private PlayerLogger playerLogger;
	private ChatChannels chatChannels;
	private Mention mention;
	
    private HashMap<String, MessageFactory> messagePlayers;
    
    private Pattern pattern;
	
	public PlayerChatListener(AwayFromKeyboard afk, Jails jails, PlayerLogger playerLogger, ChatChannels chatChannels, Mention mention){
		this.afk = afk;
		this.jails = jails;
		this.playerLogger = playerLogger;
		this.chatChannels = chatChannels;
		this.mention = mention;
		
        this.messagePlayers = plugin.getMessages().getMessagePlayers();
        pattern = Pattern.compile("@{1}\\w+");
	}
	
	@EventHandler
	public void onPlayerChat(AsyncPlayerChatEvent event) {
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
		
		//Set last activity
		playerLogger.setLastActivity(playerName);
		
		String ucMessage = event.getMessage();
		String message = event.getMessage().toLowerCase();
		
		//Block "lag" messages
		if (message.equals("lag") || message.equals("lagg")) {
			event.setCancelled(true);
			player.chat("/lag");
			return;
		}
		
		//Listener for /message
		if (messagePlayers.containsKey(player.getName())) {
            MessageFactory messageFactory = messagePlayers.get(player.getName());
			message = event.getMessage();
            event.setCancelled(true);
            if(message.equals("*")){
                player.sendMessage(ChatColor.GOLD + "[SLAP] " + ChatColor.WHITE + "The new message has " + messageFactory.getMessageName() + " as name and " + messageFactory.getMessage() + " as message.");
                messagePlayers.remove(player.getName());
                plugin.getMessageStorage().getConfig().set("messages." + messageFactory.getMessageName(), messageFactory.getMessage());
                plugin.getMessageStorage().saveConfig();
            } else {
                messageFactory.addMessage(message);
                player.sendMessage(ChatColor.GOLD + "[SLAP] " + ChatColor.WHITE + "Added text to message, type '*' to save message!");
            }
            return;
		}
		
		//Chat into channel
		if (chatChannels.isPlayerInChannel(playerName)) {
			chatChannels.playerInChannel(player, event.getMessage());
			event.setCancelled(true);
			return;
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
		
		//@Person: Check Event is not Cancelled, if it contains @, If the player is allowed to do this permission wise & check if the player is not banned
		if (!event.isCancelled() && ucMessage.contains("@") && Util.testPermission(player, "mention") && !mention.isBanned(playerName)) { 
			Matcher matcher = pattern.matcher(message); //Match the sentence
			
			ArrayList<Object> messageParts = null; //Null sets, so they don't get created if not needed
			HashSet<Player> notifyPlayers = null;
			
			int start = 0;
			
			while (matcher.find()) { //Find all occurences
				String name = message.substring(matcher.start() + 1, matcher.end()); //Get Name (Without the @)
				Player p = plugin.getServer().getPlayer(name); //Get player
				if (p != null) { //Check if it exists
					
					if (messageParts == null) { //Check if created
						 notifyPlayers= new HashSet<>(); //New set for players
						 messageParts = new ArrayList<>(); //New set for split up message
					}
					
					if (matcher.start() != 0) { //Check if not starts with @[name]
						messageParts.add(message.substring(start, matcher.start())); //Add part of String as part
					}
					
					messageParts.add(p); //Add player as part
					notifyPlayers.add(p); //Add player to HashSet for notification sounds
					
					start = matcher.end(); //Set new start
				}
			}
			if (messageParts != null && message.length() > start) { //If more letters && Message has been split
				messageParts.add(message.substring(start));  				
			}
			
			if (messageParts != null) { //If message has been split
				event.setCancelled(true); //Cancel event, everyone gets a different message
				
				//Determine namePrefix;
				String name = "<";
				PermissionUser pexUser = PermissionsEx.getUser(player); //Get PexUser
				if (pexUser != null) { //If existing user
					if (pexUser.getPrefix() != null) { //If has prefix
						name += pexUser.getPrefix(); //Add prefix
					}
				}
				name += player.getName();
				if (pexUser != null) { //If existing user
					if (pexUser.getSuffix() != null) {
						name += pexUser.getSuffix();
					}
				}
				name += "> ";
				name = ChatColor.translateAlternateColorCodes('&', name); //Transform -> ChatColors
				
				//Make annoying sound for people who got notified
				for (Player p : notifyPlayers) {
					if (!mention.hasSoundOff(p.getName())) { //Check if player has annoying sounds on
						p.playSound(p.getLocation(), Sound.ITEM_PICKUP, 10, 1);
						p.playSound(p.getLocation(), Sound.ORB_PICKUP, 10, 1);
					}
				}
				
				//Log
				Log.info("Chat:" + name + " " + event.getMessage());
				
				for (Player p : Util.getOnlinePlayers()) { //Send message to all players
					String sendMessage = name;
					boolean isSender = (p == player); //Check if the player = the sender
					for (Object o : messageParts) {
						if (o instanceof String) {
							sendMessage += (String) o;
						} else if (o instanceof Player) {
							Player mentionedPlayer = (Player) o;
							if (mentionedPlayer == p) { //The reciever of this message is the person who is mentioned
								sendMessage += ChatColor.YELLOW;
							} else if (isSender) { //The reciever of this message is the sender
								sendMessage += ChatColor.GRAY + "" + ChatColor.ITALIC;
							} else { //The reciever of this message is not involved
								sendMessage += ChatColor.GRAY;
							}
							sendMessage += "@" + mentionedPlayer.getName() + ChatColor.RESET;
						}
					}
					p.sendMessage(sendMessage);
				}
			}
		}
	}
	
	
}
