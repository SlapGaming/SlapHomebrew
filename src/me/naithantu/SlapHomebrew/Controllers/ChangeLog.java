package me.naithantu.SlapHomebrew.Controllers;

import me.naithantu.SlapHomebrew.Storage.YamlStorage;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.List;

public class ChangeLog extends AbstractController {

	private int pages;
	private ArrayList<ChangeLog.LoggedChange> changeLogArray;
	
	private YamlStorage storage;
	private FileConfiguration config;
	
	public ChangeLog() {
		changeLogArray = new ArrayList<>();
		
		storage = new YamlStorage(plugin, "changelog");
		config = storage.getConfig();
		load();
	}
	
	public void showPage(CommandSender targetPlayer, int page) {
		if (pages < 1) {
			targetPlayer.sendMessage(ChatColor.RED + "There is no changelog available");
			return;
		} else if (page > pages || page < 1) {
			targetPlayer.sendMessage(ChatColor.RED + "There are only " + pages + " pages.");
			return;
		}
		targetPlayer.sendMessage(ChatColor.YELLOW + "==================== " + ChatColor.GOLD  + "Changelog" + ChatColor.YELLOW  + " ====================");
		int xCount = (page - 1) * 5;
		int last = (page * 5);
		while (xCount < last) {
			try {
				sendMessage(targetPlayer, changeLogArray.get(xCount));
			} catch (Exception ex) {
				break;
			}
			xCount++;
		}
		targetPlayer.sendMessage(ChatColor.YELLOW + "================== " + ChatColor.GOLD  + "Page " + page + " out of " + pages + ChatColor.YELLOW  + " ==================");
	}
	
	/**
	 * Return the number of pages
	 * @return the number of pages
	 */
	public int getPages() {
		return pages;
	}
	
	private void sendMessage(CommandSender targetPlayer, LoggedChange loggedChange) {
		targetPlayer.sendMessage(ChatColor.GOLD + "[" + loggedChange.date + "] " + ChatColor.WHITE + loggedChange.change);
	}
	
	public void addToChangelog(String date, String change) {
		changeLogArray.add(0, new LoggedChange(date, change));
		List<String> changes;
		if (config.contains(date)) {
			changes = config.getStringList(date);
		} else {
			changes = new ArrayList<>();
		}
		changes.add(change);
		config.set(date, changes);
		storage.saveConfig();
	}
	
	private void load() {
		for (String date : config.getKeys(false)) {
			List<String> changes = config.getStringList(date);
			for (String change : changes) {
				changeLogArray.add(0, new LoggedChange(date, change));
			}
		}
		pages = (int)Math.ceil((changeLogArray.size() / (double)5));
	}
	
	public void reload() {
		storage.reloadConfig();
		changeLogArray.clear();
		load();
	}
	
	private class LoggedChange {
		
		public String date;
		public String change;
		
		public LoggedChange(String date, String change) {
			this.date = date;
			this.change = change;
		}
		
	}

    @Override
    public void shutdown() {
    	//Not needed
    }
	
}
