package me.naithantu.SlapHomebrew.Listeners.Player;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import me.naithantu.SlapHomebrew.Controllers.*;
import me.naithantu.SlapHomebrew.Controllers.MessageStringer.MessageCombiner;
import me.naithantu.SlapHomebrew.Listeners.AbstractListener;
import me.naithantu.SlapHomebrew.PlayerExtension.PlayerControl;
import me.naithantu.SlapHomebrew.PlayerExtension.SlapPlayer;
import me.naithantu.SlapHomebrew.Util.Log;
import me.naithantu.SlapHomebrew.Util.Util;

import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import ru.tehkode.permissions.PermissionUser;
import ru.tehkode.permissions.bukkit.PermissionsEx;

public class PlayerChatListener extends AbstractListener {
	
	//Singleton Instance
	private static PlayerChatListener instance;
	
	private AwayFromKeyboard afk;
	private Jails jails;
	private ChatChannels chatChannels;
	private Mention mention;
    private MuteController muteController;
	    
    private static Pattern pattern;
	
	public PlayerChatListener(AwayFromKeyboard afk, Jails jails, ChatChannels chatChannels, Mention mention, MuteController muteController){
		this.afk = afk;
		this.jails = jails;
		this.chatChannels = chatChannels;
		this.mention = mention;
        this.muteController = muteController;
		
		//Create pattern
        pattern = Pattern.compile("@{1}\\w+");
        
        //Set this as instance
        instance = this;
	}
	
	@EventHandler
	public void onPlayerChat(AsyncPlayerChatEvent event) {
		Player player = event.getPlayer();
		SlapPlayer slapPlayer = PlayerControl.getPlayer(player);
		String playerName = player.getName();
        String UUID = player.getUniqueId().toString();

        //Set last activity
        slapPlayer.active();

		//Block chat if not moved yet
		if (!slapPlayer.hasMoved()) {
			if (event.getMessage().matches("connected with .* using MineChat")) { //If message is saying something like "Connected with Minechat"
				syncKick(player, "MineChat is not allowed on this server."); //Kick the player
				event.setCancelled(true);
				return;
			}
			event.setCancelled(true);
			slapPlayer.sendNotMovedMessage();
			return;
		}		
		
		//Block chat while in jail.
		if (jails.isJailed(UUID)) {
			if (!jails.isAllowedToChat(UUID)) {
				event.setCancelled(true);
				player.sendMessage(ChatColor.GRAY + "You are jailed. Use /timeleft to check your time left in jail.");
				return;
			}
		}

        //Block chat if muted
        if (muteController.isMuted(player.getUniqueId().toString())) {
            event.setCancelled(true);
            player.sendMessage(ChatColor.GRAY + "You are muted. Use /muted for more info.");
            return;
        }

		String ucMessage = event.getMessage();
		String message = event.getMessage().toLowerCase();
		
		//Block "lag" messages
		if (message.equals("lag") || message.equals("lagg")) {
			event.setCancelled(true);
			player.chat("/lag");
			return;
		}
		
		//Block ryuuga from saying :S
		if (playerName.equals("ryuuga")) {
			if (message.contains(":s")) {
				syncKick(player, ":S");
				event.setCancelled(true);
				return;
			}
		}
		
		//Listener for /message
		if (slapPlayer.isCombiningMessage()) {
            event.setCancelled(true);
            MessageCombiner combiner = slapPlayer.getMessageCombiner();
            if(combiner.isEnding(message)){ //Stopping the message
            	combiner.finish();
            	slapPlayer.removeMessageCombiner();
            } else { //Add text
                combiner.addText(ucMessage);
                combiner.notifyHowToEnd();
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
		if (afk.isAfk(player)) {
			afk.leaveAfk(player);
		}
	}

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onMentionChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        String playerName = player.getName();
        String ucMessage = event.getMessage();

        //@Person: Check Event is not Cancelled, if it contains @, If the player is allowed to do this permission wise & check if the player is not banned
        if (!event.isCancelled() && ucMessage.contains("@") && Util.testPermission(player, "mention") && !mention.isBanned(playerName)) {

                //=> Normal version should handle it
                Matcher matcher = pattern.matcher(ucMessage); //Match the sentence

                ArrayList<Object> messageParts = null; //Null sets, so they don't get created if not needed
                HashSet<Player> notifyPlayers = null;

                int start = 0;

                while (matcher.find()) { //Find all occurences
                    String name = ucMessage.substring(matcher.start() + 1, matcher.end()); //Get Name (Without the @)
                    Player p = plugin.getServer().getPlayer(name); //Get player
                    if (p != null) { //Check if it exists

                        if (messageParts == null) { //Check if created
                            notifyPlayers= new HashSet<>(); //New set for players
                            messageParts = new ArrayList<>(); //New set for split up message
                        }

                        if (matcher.start() != 0) { //Check if not starts with @[name]
                            messageParts.add(ucMessage.substring(start, matcher.start())); //Add part of String as part
                        }

                        messageParts.add(p); //Add player as part
                        notifyPlayers.add(p); //Add player to HashSet for notification sounds

                        start = matcher.end(); //Set new start
                    }
                }
                if (messageParts != null && ucMessage.length() > start) { //If more letters && Message has been split
                    messageParts.add(ucMessage.substring(start));
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

                    //Check if colorize
                    boolean colorize = Util.testPermission(player, "staff");

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
                        p.sendMessage(colorize ? ChatColor.translateAlternateColorCodes('&', sendMessage) : sendMessage); //Send message (Colorize if needed)
                    }
                }

        }
    }

    /**
     * Kick a player in Sync
     * @param player The player
     * @param kickReason The reason
     */
    private static void syncKick(final Player player, final String kickReason) {
        Util.run(new Runnable() {
            @Override
            public void run() {
                if (player.isOnline()) {
                    player.kickPlayer(kickReason);
                }
            }
        });
    }
	
	@Override
	public void disable() {
		//Remove static
		pattern = null;
	}
	
	/**
	 * Get the instance
	 * @return the instance
	 */
	public static PlayerChatListener getInstance() {
		return instance;
	}
	
	
}
