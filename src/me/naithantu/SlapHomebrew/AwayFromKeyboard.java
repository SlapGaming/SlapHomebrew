package me.naithantu.SlapHomebrew;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class AwayFromKeyboard {

	private SlapHomebrew plugin;
	private Map<String, String> afkReasons;
	
	public AwayFromKeyboard(SlapHomebrew plugin){
		this.plugin = plugin;
		afkReasons = new HashMap<String, String>();
	}
	
    public void goAfk(String player, String reason){
    	if (afkReasons.containsKey(player)){
    		afkReasons.remove(player);
    	}
    	afkReasons.put(player, reason);
    	for (Player targetPlayer : plugin.getServer().getOnlinePlayers()) {
    		if (targetPlayer.hasPermission("slaphomebrew.staff") && !reason.equals("AFK")) {
    			targetPlayer.sendMessage(ChatColor.WHITE + player + " is now AFK. Reason: " + reason);
    		} else {
    			targetPlayer.sendMessage(ChatColor.WHITE + player + " is now AFK");
    		}
    	}
    }
    
    public void leaveAfk(String player){
    	afkReasons.remove(player);
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
	

}
