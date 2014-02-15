package me.naithantu.SlapHomebrew.Controllers;

import java.text.SimpleDateFormat;
import java.util.HashSet;
import java.util.List;
import java.util.TimeZone;

import me.naithantu.SlapHomebrew.Controllers.TabController.TabGroup;
import me.naithantu.SlapHomebrew.Storage.YamlStorage;
import me.naithantu.SlapHomebrew.Util.Util;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

public class PlayerLogger extends AbstractController {

	private YamlStorage logYML;
	private FileConfiguration logConfig;
	
	private SimpleDateFormat format;
	private SimpleDateFormat onlineFormat;
	
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
		
		commandSpy = new HashSet<>();
		List<String> list = logConfig.getStringList("commandspy");
		for (String player : list) {
			commandSpy.add(player);
		}
		
		suicides = new HashSet<>();
		doingCommand = new HashSet<>();
	}
	
	private void save(){
		logYML.saveConfig();
	}
	
    @Override
    public void shutdown() {
    	//Not needed
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
