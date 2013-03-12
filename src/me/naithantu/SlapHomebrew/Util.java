package me.naithantu.SlapHomebrew;

import me.naithantu.SlapHomebrew.Storage.YamlStorage;

import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;

import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

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
	
	public static boolean hasFlag(SlapHomebrew plugin, Location location, String flag){
		RegionManager regionManager = plugin.getWorldGuard().getRegionManager(location.getWorld());
		ApplicableRegionSet noBackDeathRegions = regionManager.getApplicableRegions(location);
		for(ProtectedRegion region: noBackDeathRegions){
			if(region.getMembers().contains("flag:" + flag)){
				return true;
			}
		}
		return false;
	}
}
