package me.naithantu.SlapHomebrew.Controllers;

import java.util.HashSet;
import java.util.List;

import me.naithantu.SlapHomebrew.Storage.YamlStorage;
import me.naithantu.SlapHomebrew.Util.Util;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

public class PlayerLogger extends AbstractController {

	private YamlStorage logYML;
	private FileConfiguration logConfig;
		
	private HashSet<String> commandSpy;
	
	private HashSet<String> doingCommand;
	private HashSet<String> suicides;
	
	public PlayerLogger() {
		logYML = new YamlStorage(plugin, "playerlog");
		logConfig = logYML.getConfig();
		
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
	public void addCommandSpy(String UUID) {
		if (commandSpy.contains(UUID)) return;
		commandSpy.add(UUID);
		List<String> list = logConfig.getStringList("commandspy");
		list.add(UUID);
		logConfig.set("commandspy", list);
		save();
	}
	
	public void removeFromCommandSpy(String UUID) {
		if (!commandSpy.contains(UUID)) return;
		commandSpy.remove(UUID);
		List<String> list = logConfig.getStringList("commandspy");
		list.remove(UUID);
		logConfig.set("commandspy", list);
		save();		
	}
	
	public boolean isCommandSpy(String UUID) {
		return commandSpy.contains(UUID);
	}
	
	public void sendToCommandSpies(String player, String command, boolean social) {
        for (Player p : Util.getOnlinePlayers()) {
            if (commandSpy.contains(p.getUniqueId().toString())) {
                if (!p.getName().equals(player)) {
                    if (social) {
                        p.sendMessage(ChatColor.GRAY + "[Social] " + player + ": " + command);
                    } else {
                        p.sendMessage(ChatColor.GRAY + "[CS] " + player + ": " + command);
                    }
                }
            }
        }
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
