package me.naithantu.SlapHomebrew;

import java.util.ArrayList;
import java.util.logging.Logger;

import me.naithantu.SlapHomebrew.Runnables.ApplyGathererTask;
import me.naithantu.SlapHomebrew.Storage.YamlStorage;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import com.earth2me.essentials.Essentials;
import com.earth2me.essentials.User;

import ru.tehkode.permissions.PermissionUser;
import ru.tehkode.permissions.bukkit.PermissionsEx;

public class ApplyChecker {
	
	private SlapHomebrew plugin;

	private ApplyGathererTask gathererThread;
	private ArrayList<String[]> failedThreads;
	
	private YamlStorage applyThreadStorage;
	private FileConfiguration applyThreadConfig;
	
	private Essentials ess;
	
	private boolean disabled;
	
	public ApplyChecker(SlapHomebrew plugin, Essentials ess){
		this.plugin = plugin;
		this.ess = ess;
		applyThreadStorage = plugin.getApplyThreadStorage();
    	applyThreadConfig = applyThreadStorage.getConfig();
    	if (applyThreadConfig.contains("disabled")) {
    		disabled = applyThreadConfig.getBoolean("disabled");
    	} else {
    		applyThreadConfig.set("disabled", false);
    		applyThreadStorage.saveConfig();
    		disabled = false;
    	}
    	failedThreads = new ArrayList<String[]>();
    	if (!disabled) {
	    	try {
	    		gathererThread = new ApplyGathererTask(this, applyThreadConfig, applyThreadStorage);
	    		gathererThread.runTaskTimerAsynchronously(plugin, 600, 1200);
	    	} catch (Exception e) {
	    		getLogger().info("Apply Forum Gatherer task failed.");
	    	}
    	}
	}
		
	public void forumsDown(){
		getLogger().info("[ApplyThread] Forums Down? - Retrying in a minute..");
	}
	
	public boolean findUser(String username){
		boolean returnBool = false;
		User newPlayer = ess.getUserMap().getUser(username);
		if (newPlayer != null) {
			promoteUser(newPlayer.getName());
			returnBool = true;
		}
		return returnBool;
	}
	
	public void promoteUser(String name){
		PermissionUser user = PermissionsEx.getUser(name);
		String[] groupNames = user.getGroupsNames();
		if (groupNames[0].contains("builder")) {
			String[] memberGroup = { "Member" };
			user.setGroups(memberGroup);
			plugin.getServer().broadcastMessage(Util.getHeader() + ChatColor.GREEN + name + ChatColor.WHITE + " is promoted to member, congratulations!");
			plugin.getServer().broadcastMessage(Util.getHeader() + "Not a member yet? Go to " + ChatColor.GREEN + "www.slapgaming.com/apply" + ChatColor.WHITE + " to apply!");
		}
	}
    
    public boolean warnMods(String[] strings){
    	boolean staffFound = false;
    	for (Player tempPlayer : plugin.getServer().getOnlinePlayers()) {
    		if (tempPlayer.hasPermission("slaphomebrew.staff")) {
    			staffFound = true;
    			tempPlayer.sendMessage(ChatColor.GOLD + "[SLAP] " + ChatColor.WHITE + "User: " + strings[1] + " applied on the forums and failed. " + strings[0] + " GO DO STUFF.");
    			tempPlayer.sendMessage(ChatColor.GOLD + "[SLAP] " + ChatColor.WHITE + "http://forums.slapgaming.com/showthread.php?" + strings[2]);
    		}
    	}
    	if (!staffFound) {
    		if (!failedThreads.contains(strings)) {
    			failedThreads.add(strings);
    		}
    	} else {
    		if (failedThreads.contains(strings)) {
    			failedThreads.remove(strings);
    		}
    	}
    	return staffFound;
    }
    
    public void warnModsFailedThreads(){
    	if (failedThreads.size() == 1) {
    		String[] strings = failedThreads.get(0);
    		warnMods(strings);
    	} else if (failedThreads.size() > 1) {
    		int xCount = 0; boolean staffFound = true;
    		while (staffFound && xCount < failedThreads.size()){
    			String[] strings = failedThreads.get(xCount);
    			staffFound = warnMods(strings);
    		}
    	}
    }
    
    public Logger getLogger(){
    	return plugin.getLogger();
    }
	

}
