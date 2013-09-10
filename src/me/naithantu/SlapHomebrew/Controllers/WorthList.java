package me.naithantu.SlapHomebrew.Controllers;

import java.util.ArrayList;
import java.util.Collections;

import me.naithantu.SlapHomebrew.SlapHomebrew;
import me.naithantu.SlapHomebrew.Storage.YamlStorage;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;

public class WorthList {

	private boolean configFound;
	private ConfigurationSection worthConfig;
	
	private ArrayList<String> worthList;
	private int pages;
	
	public WorthList(SlapHomebrew plugin) {
		YamlStorage worthStorage = new YamlStorage(plugin, "../Essentials/worth");
		worthConfig = worthStorage.getConfig().getConfigurationSection("worth");
		configFound = worthConfig != null;
		worthList = new ArrayList<>();
		parseList();
	}
	
	private void parseList() {
		if (!configFound) return;
		ArrayList<String> keys = new ArrayList<>(worthConfig.getKeys(false));
		Collections.sort(keys);
		
		for (String key : keys) {
			Material material = Material.matchMaterial(key);
			if (material != null) {
				worthList.add(ChatColor.GOLD + capitalizeFirst(key) + ChatColor.WHITE + ": $" + worthConfig.getString(key));
			} else {
				material = parseKey(key);
				if (material != null) {
					worthList.add(ChatColor.GOLD + capitalizeFirst(material.toString().replace("_", " ")) + ChatColor.WHITE + ": " + worthConfig.getString(key));
				}
			}
		}
		pages = (int)Math.ceil((double)worthList.size() / (double) 9);
	}
	
	private Material parseKey(String key) {
		for (Material m : Material.values()) {
			 String mN = m.toString().replace("_", "").toLowerCase();
			 if (key.equals(mN)) {
				 return m;
			 }
		}
		return null;
	}
	
	private String capitalizeFirst(String s) {
		return s.substring(0, 1).toUpperCase() + s.substring(1).toLowerCase();
	}
	
	public void sendPage(CommandSender sender, int page) {
		sender.sendMessage(ChatColor.YELLOW + "===" + ChatColor.GOLD + " Worth List " + ChatColor.YELLOW + "===" + ChatColor.GOLD + " Page " + page + " of " + pages + " " + ChatColor.YELLOW + "===");
		int x = (page - 1) * 9;
		int count = 0;
		while (x < worthList.size() && count < 9) {
			sender.sendMessage(worthList.get(x));
			count++; x++;
		}
	}

	public int getPages() {
		return pages;
	}
	
	public boolean isConfigFound() {
		return configFound;
	}
	

}
