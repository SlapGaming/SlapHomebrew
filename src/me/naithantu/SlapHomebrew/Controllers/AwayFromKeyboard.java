package me.naithantu.SlapHomebrew.Controllers;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class AwayFromKeyboard extends AbstractController {

	private PlayerLogger playerLogger;
	
	private Map<String, String> afkReasons;
	private Map<String, Long> afkTimes;
	
	private HashSet<String> preventAFK;
	
	public AwayFromKeyboard(PlayerLogger playerLogger){
		this.playerLogger = playerLogger;
		afkReasons = new HashMap<>();
		afkTimes = new HashMap<>();
		preventAFK = new HashSet<>();
	}
	
    public void goAfk(String player, String reason){
    	if (afkReasons.containsKey(player)){
    		afkReasons.remove(player);
    	}
    	afkReasons.put(player, reason);
    	afkTimes.put(player, System.currentTimeMillis());
    	if (!reason.equals("AFK")) {
    		plugin.getServer().broadcastMessage(ChatColor.WHITE + player + " is now AFK. Reason: " + reason);
    	} else {
    		plugin.getServer().broadcastMessage(ChatColor.WHITE + player + " is now AFK.");
    	}
    }
    
    public void leaveAfk(String player){
    	afkReasons.remove(player);
    	long time = afkTimes.get(player);
    	playerLogger.addAFKTime(player, System.currentTimeMillis() - time);
    	afkTimes.remove(player);
    	plugin.getServer().broadcastMessage(ChatColor.WHITE + player + " is no longer AFK");
    }
    
    public boolean isAfk(String player){
    	boolean returnBool = false;
    	if (afkReasons.get(player) != null) {
    		returnBool = true;
    	}
    	return returnBool;
    }
    
    public String getAfkReason(String player){
    	return afkReasons.get(player);
    }
    
    public void sendAfkReason(Player sender, String afkPerson){
    	String reason = getAfkReason(afkPerson);
    	if (reason.equals("AFK")){
    		sender.sendMessage(ChatColor.RED + afkPerson + " might not respond. Reason: " + ChatColor.WHITE + "Away From Keyboard");
    	} else {
    		sender.sendMessage(ChatColor.RED + afkPerson + " might not respond. Reason: " + ChatColor.WHITE + reason);
    	}
    }
    
    public void resetAfkReason(String afkPerson) {
    	if (afkReasons.containsKey(afkPerson)) {
    		afkReasons.remove(afkPerson);
    	}
    	afkReasons.put(afkPerson, "AFK");
    }
    
    public void removeAfk(String afkPerson){
    	if (afkReasons.containsKey(afkPerson)) {
    		afkReasons.remove(afkPerson);
    	}
    }
    
    public void setPreventAFK(String player) {
    	preventAFK.add(player);
    }
    
    public void removeFromPreventAFK(String player) {
    	preventAFK.remove(player);
    }
    
    public boolean hasPreventAFK(String player) {
    	return preventAFK.contains(player);
    }
    
    @Override
    public void shutdown() {
    	//Not needed
    }
	

}
