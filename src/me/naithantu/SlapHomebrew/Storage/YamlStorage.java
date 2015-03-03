package me.naithantu.SlapHomebrew.Storage;

import me.naithantu.SlapHomebrew.SlapHomebrew;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;

public class YamlStorage {
	SlapHomebrew plugin;
	String fileName;
	File file;
	FileConfiguration config;

	public YamlStorage(SlapHomebrew plugin, String fileName) {
		this.plugin = plugin;
		this.fileName = fileName + ".yml";
		getConfig();
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
	
	/**
	 * Put a location in a config file
	 * @param config The config file
	 * @param savePath The path where to save it
	 * @param location The location
	 */
	public static void putLocationInConfig(FileConfiguration config, String savePath, Location location) {
		//Coords
		config.set(savePath + ".x", location.getX());
		config.set(savePath + ".y", location.getY());
		config.set(savePath + ".z", location.getZ());
		
		//Extra info
		config.set(savePath + ".yaw", (double) location.getYaw());
		config.set(savePath + ".pitch", (double) location.getPitch());
		
		//Worldname
		config.set(savePath + ".world", location.getWorld().getName());
	}
	
	/**
	 * Load the location from a config
	 * @param config The config
	 * @param loadPath The path to the location
	 * @return the Location or null
	 */
	public static Location loadLocationFromConfig(FileConfiguration config, String loadPath) {
		//Check if path exists
		if (!config.contains(loadPath)) return null;
		
		try {
			//Get world
			String worldname = config.getString(loadPath + ".world");
			World w = Bukkit.getWorld(worldname);
			if (w == null) return null;
			
			//Get coords
			double x = config.getDouble(loadPath + ".x");
			double y = config.getDouble(loadPath + ".y");
			double z = config.getDouble(loadPath + ".z");
			
			//Extra info
			float yaw = (float) config.getDouble(loadPath + ".yaw");
			float pitch = (float) config.getDouble(loadPath + ".pitch");
			
			//Create location
			return new Location(w, x, y, z, yaw, pitch);
		} catch (Exception e) {
			return null;
		}
	}
}
