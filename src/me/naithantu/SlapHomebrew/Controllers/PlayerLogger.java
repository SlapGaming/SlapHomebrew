package me.naithantu.SlapHomebrew.Controllers;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.TimeZone;

import me.naithantu.SlapHomebrew.Controllers.TabController.TabGroup;
import me.naithantu.SlapHomebrew.Storage.YamlStorage;
import me.naithantu.SlapHomebrew.Util.Util;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import ru.tehkode.permissions.PermissionUser;
import ru.tehkode.permissions.bukkit.PermissionsEx;

public class PlayerLogger extends AbstractController {

	private YamlStorage logYML;
	private FileConfiguration logConfig;
	
	private SimpleDateFormat format;
	private SimpleDateFormat onlineFormat;
	
	
	private HashMap<String, Boolean> minechatMoved;
	
	private HashMap<String, Long> lastActivity;
	
	private HashMap<String, String> doubleMessage;
	
	private HashSet<String> commandSpy;
	
	private HashSet<String> doingCommand;
	private HashSet<String> suicides;
	
	public PlayerLogger() {
		logYML = new YamlStorage(plugin, "playerlog");
		logConfig = logYML.getConfig();
		onlineFormat = new SimpleDateFormat("dd:HH:mm:ss");
		onlineFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
		format = new SimpleDateFormat("dd-MM-yyyy");
		format.setTimeZone(TimeZone.getTimeZone("GMT"));
		minechatMoved = new HashMap<>();
		doubleMessage = new HashMap<>();
		lastActivity = new HashMap<>();
		
		commandSpy = new HashSet<>();
		List<String> list = logConfig.getStringList("commandspy");
		for (String player : list) {
			commandSpy.add(player);
		}
		
		suicides = new HashSet<>();
		doingCommand = new HashSet<>();
		
		
		onEnable();
	}
	
	private void save(){
		logYML.saveConfig();
	}
	
	public void onEnable(){
		for (Player onlinePlayer : plugin.getServer().getOnlinePlayers()) {
			setMoved(onlinePlayer.getName(), true);
		}
	}
	
    @Override
    public void shutdown() {
    	//Not needed
    }
	
	/*
	 * Minechat prevention
	 */
	public boolean hasMoved(String playername) {
		if (minechatMoved == null || playername == null) return false;
		return minechatMoved.get(playername);
	}
	
	public void setMoved(String playername, boolean moved) {
		minechatMoved.put(playername, moved);
	}
	
	public void joinedMinechatChecker(Player p) {
		if (!p.hasPermission("slaphomebrew.staff")) {
			setMoved(p.getName(), false);
		} else {
			setMoved(p.getName(), true); //Staff
		}
	}
	
	public void removeFromMoved(String playername) {
		minechatMoved.remove(playername);
	}
	
	public void sendNotMovedMessage(Player p) {
		p.sendMessage(ChatColor.GRAY + "You're not allowed to do commands/chat until you have moved.");
	}
	
	public boolean inMovedHashMap(String playername) {
		if (minechatMoved.containsKey(playername)) {
			return true;
		} else {
			return false;
		}
	}
	
	
	/*
	 * Last Activity
	 */
	public void setLastActivity(String player) {
		lastActivity.put(player, System.currentTimeMillis());
	}
	
	public long getLastActivity(String player) {
		Long a = lastActivity.get(player);
		if (a == null) return 0;
		else return a;
	}
	
	public void removeFromLastActivity(String player) {
		lastActivity.remove(player);
	}
	
	
	/*
	 * Double message
	 */
	public void setFirstMessage(String player, String message) {
		PermissionUser user = PermissionsEx.getUser(player);
		String tag = "<" + user.getPrefix() + player + ChatColor.WHITE + "> ";
		doubleMessage.put(player, tag + message.replace("*--", " "));
	}
	
	public void sendSecondMessage(String player, String message) {
		plugin.getServer().broadcastMessage(ChatColor.translateAlternateColorCodes('&', doubleMessage.get(player) + message));
		doubleMessage.remove(player);
	}
	
	public boolean hasMessage(String player) {
		return doubleMessage.containsKey(player);
	}
	
	/*
	 * CommandSpy
	 */
	public void addCommandSpy(String player) {
		if (commandSpy.contains(player)) return;
		commandSpy.add(player);
		List<String> list = logConfig.getStringList("commandspy");
		list.add(player);
		logConfig.set("commandspy", list);
		save();
	}
	
	public void removeFromCommandSpy(String player) {
		if (!commandSpy.contains(player)) return;
		commandSpy.remove(player);
		List<String> list = logConfig.getStringList("commandspy");
		list.remove(player);
		logConfig.set("commandspy", list);
		save();		
	}
	
	public boolean isCommandSpy(String player) {
		return commandSpy.contains(player);
	}
	
	public void sendToCommandSpies(String player, String command, boolean social) {
		for (String spyname : commandSpy) {
			Player spy = plugin.getServer().getPlayer(spyname);
			if (spy != null) {
				if (!spyname.equals(player)) {
					if (social) spy.sendMessage(ChatColor.GRAY + "[Social] " + player + ": " + command);
					else spy.sendMessage(ChatColor.GRAY + "[CS] " + player + ": " + command);
				}
			}
		}
	}
	
	/*
	 * SuperAdmin control
	 */
	public boolean setSuperAdminGroup(String player, String group) {
		try {
			TabGroup tGroup = TabController.TabGroup.valueOf(group);
			if (tGroup == null) return false;
			logConfig.set("grouptab." + player, tGroup.toString());
			save();
			return true;
		} catch (Exception e) {
			return false;
		}
	}
	
	public TabGroup getSuperAdminGroup(String player) {
		String tabGroup = logConfig.getString("grouptab." + player);
		if (tabGroup == null) return null;
		return TabGroup.valueOf(tabGroup);
	}

	/*
	 * Suicides
	 */
	/**
	 * A player commits suicide
	 * @param playername
	 */
	public void commitsSuicide(String playername) {
		suicides.add(playername);
	}
	
	/**
	 * Check if a player has committed suicide
	 * @param playername The player
	 * @return committed suicide
	 */
	public boolean hasCommittedSuicide(String playername) {
		if (suicides.contains(playername)) {
			suicides.remove(playername);
			return true;
		}
		return false;
	}
	
	

	public boolean isRunningCommand(Player p) {
		if (doingCommand.contains(p.getName())) {
			Util.badMsg(p, "Your previous command is still running.");
			return true;
		} else {
			return false;
		}
	}
	
	
	
}
