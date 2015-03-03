package me.naithantu.SlapHomebrew.Controllers;

import com.earth2me.essentials.Essentials;
import com.earth2me.essentials.User;
import me.naithantu.SlapHomebrew.Runnables.ApplyGathererTask;
import me.naithantu.SlapHomebrew.Storage.YamlStorage;
import me.naithantu.SlapHomebrew.Util.Util;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import ru.tehkode.permissions.PermissionUser;
import ru.tehkode.permissions.bukkit.PermissionsEx;

import java.util.ArrayList;
import java.util.logging.Logger;

public class ApplyChecker extends AbstractController {
	
	private ApplyGathererTask gathererThread;
	private ArrayList<String[]> failedThreads;
	
	private YamlStorage applyThreadStorage;
	private FileConfiguration applyThreadConfig;
	
	private Essentials ess;
	private TabController tabController;
	
	public ApplyChecker(Essentials ess, TabController tabController){
		this.ess = ess;
		this.tabController = tabController;
		boolean devServer = false;
		FileConfiguration pluginConfig = plugin.getConfig();
		if (pluginConfig.contains("devserver")) {
			devServer = pluginConfig.getBoolean("devserver");
		} else {
			pluginConfig.set("devserver", false);
			plugin.saveConfig();
		}
		applyThreadStorage = plugin.getApplyThreadStorage();
    	applyThreadConfig = applyThreadStorage.getConfig();
    	failedThreads = new ArrayList<String[]>();
    	if (!devServer) {
	    	try {
	    		gathererThread = new ApplyGathererTask(this, applyThreadConfig, applyThreadStorage);
	    		gathererThread.runTaskTimerAsynchronously(plugin, 600, 1200);
	    	} catch (Exception e) {
	    		getLogger().info("Apply Forum Gatherer task failed.");
	    	}
    	} else {
    		getLogger().info("[ApplyChecker] Running a dev server, ApplyChecker is disabled.");
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
			Player tabPlayer = plugin.getServer().getPlayer(name);
			if (tabPlayer != null) {
				tabController.playerSwitchGroup(tabPlayer);
			}
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
	
    @Override
    public void shutdown() {
    	if (gathererThread != null) gathererThread.cancel();
    	applyThreadStorage.saveConfig();
    }
    
    
}
