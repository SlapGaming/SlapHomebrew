package me.naithantu.SlapHomebrew;

import me.naithantu.SlapHomebrew.Storage.YamlStorage;

import org.bukkit.configuration.file.FileConfiguration;

public class Util {

	public static void dateIntoTimeConfig(String date, String message, YamlStorage timeStorage){
		FileConfiguration timeConfig = timeStorage.getConfig();
		int i = 1;
		while(timeConfig.contains(date)){
			date += " (" + i + ")";
			i++;
		}
		timeConfig.set(date, message);
		timeStorage.saveConfig();
	}
}
