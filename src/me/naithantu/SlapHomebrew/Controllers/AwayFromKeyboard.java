package me.naithantu.SlapHomebrew.Controllers;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import me.naithantu.SlapHomebrew.Controllers.PlayerLogging.AFKLogger;

import me.naithantu.SlapHomebrew.Util.Util;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class AwayFromKeyboard extends AbstractController {
	
	private Map<String, String> afkReasons;
	
	private HashSet<String> preventAFK;
	
	public AwayFromKeyboard(){
		afkReasons = new HashMap<>();
		preventAFK = new HashSet<>();
	}

    /**
     * A player goes AFK
     * @param player The player
     * @param reason The reason
     */
    public void goAfk(Player player, String reason){
        //Get the UUID & The name
        String UUID = player.getUniqueId().toString();
        String playername = player.getName();

        //Put the AFK Reason in the map
    	afkReasons.put(UUID, reason);

        //Check if no reason was given
    	boolean withReason = !reason.equals("AFK");
        //Check if Semi-AFK
        boolean semiAFK = reason.equals("Semi-AFK");

        //Log going AFK
    	AFKLogger.logPlayerGoesAFK(UUID, (withReason && !semiAFK ? reason : null));

        //Create broadcast
        String broadcast = ChatColor.WHITE + playername + " is now ";
        if (semiAFK) {
            broadcast += "Semi-AFK.";
        } else {
            broadcast += "AFK.";
            if (withReason) {
                broadcast += " Reason: " + reason;
            }
        }
        //=> Broadcast message
        Util.broadcast(broadcast);
    }

    /**
     * A player leaves AFK
     * @param player The player
     */
    public void leaveAfk(Player player){
        //Get UUID
        String UUID = player.getUniqueId().toString();

        //Get reason & remove it
        String reason = afkReasons.get(UUID);
    	afkReasons.remove(UUID);

        //Log the leave
    	AFKLogger.logPlayerLeftAFK(UUID);

        //Check if Semi-AFK
        if (reason.equals("Semi-AFK")) {
            player.sendMessage("You are no longer Semi-AFK.");
        } else {
            Util.broadcast(ChatColor.WHITE + player.getName() + " is no longer AFK.");
        }
    }

    /**
     * Check if a player is AFK
     * @param player The player
     * @return is AFK
     */
    public boolean isAfk(Player player){
        return afkReasons.containsKey(player.getUniqueId().toString());
    }

    /**
     * Get the AFK reason for a player
     * @param player The player
     * @return the reason or null
     */
    public String getAfkReason(Player player){
    	return afkReasons.get(player.getUniqueId().toString());
    }


    /**
     * Send an AFK reason to the player
     * @param sender The sender
     * @param afkPlayer The target player
     */
    public void sendAfkReason(Player sender, Player afkPlayer){
    	String reason = getAfkReason(afkPlayer);
        Util.badMsg(sender, afkPlayer.getName() + " might not respond. Reason: " + reason);
    }

    /**
     * Remove the player's AFK
     * @param player The player
     */
    public void removeAfk(Player player){
        String UUID = player.getUniqueId().toString();
    	if (afkReasons.containsKey(UUID)) {
    		AFKLogger.logPlayerLeftAFK(UUID);
    		afkReasons.remove(UUID);
    	}
    }

    /**
     * Add a player to the prevent AFK list
     * @param player The player
     */
    public void setPreventAFK(Player player) {
    	preventAFK.add(player.getUniqueId().toString());
    }

    /**
     * Remove a player from the prevent AFK list
     * @param player The player
     */
    public void removeFromPreventAFK(Player player) {
    	preventAFK.remove(player.getUniqueId().toString());
    }

    /**
     * Check if a player is currently in the prevent AFK list
     * @param player The player
     * @return is in the prevent AFK list
     */
    public boolean hasPreventAFK(Player player) {
    	return preventAFK.contains(player.getUniqueId().toString());
    }
    
    @Override
    public void shutdown() {
    	preventAFK.clear();
        afkReasons.clear();
    }
	

}
