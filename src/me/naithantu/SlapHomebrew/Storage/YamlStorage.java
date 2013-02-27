package me.naithantu.SlapHomebrew.Storage;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;

import me.naithantu.SlapHomebrew.SlapHomebrew;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public class YamlStorage extends YamlConfiguration{
	SlapHomebrew plugin;
	String fileName;
	File file;
	FileConfiguration config;

	public YamlStorage(SlapHomebrew plugin, String fileName) {
		this.plugin = plugin;
		this.fileName = fileName + ".yml";
	}

	public void reloadConfig() {
		if (file == null) {
			file = new File(plugin.getDataFolder(), fileName);
		}
		config = YamlConfiguration.loadConfiguration(file);

		// Look for defaults in the jar
		InputStream defConfigStream = plugin.getResource(fileName);
		if (defConfigStream != null) {
			YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defConfigStream);
			config.setDefaults(defConfig);
		}
	}
	
	public FileConfiguration getConfig() {
		if (config == null) {
			this.reloadConfig();
		}
		return config;
	}

	public void saveConfig() {
		if (config == null || file == null) {
			return;
		}
		try {
			getConfig().save(file);
		} catch (IOException ex) {
			plugin.getLogger().log(Level.SEVERE, "Could not save config to " + config, ex);
		}
	}

	public void saveDefaultConfig() {
		if (file == null) {
			file = new File(plugin.getDataFolder(), "customConfig.yml");
		}
		if (!file.exists()) {
			this.plugin.saveResource(fileName, false);
		}
	}
}
