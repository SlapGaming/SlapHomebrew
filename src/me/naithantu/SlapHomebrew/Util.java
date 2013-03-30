package me.naithantu.SlapHomebrew;

import me.naithantu.SlapHomebrew.Storage.YamlStorage;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

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
	
	public static boolean hasEmptyInventory(Player player){
		Boolean emptyInv = true;
		PlayerInventory inv = player.getInventory();
		for (ItemStack stack : inv.getContents()) {
			//TODO Is try - catch really required here?
			try {
				if (stack.getType() != (Material.AIR)) {
					emptyInv = false;
				}
			} catch (NullPointerException e) {
			}
		}
		for (ItemStack stack : inv.getArmorContents()) {
			try {
				if (stack.getType() != (Material.AIR)) {
					emptyInv = false;
				}
			} catch (NullPointerException e) {
			}
		}
		return emptyInv;
	}
	
	public static boolean hasFullInventory(Player player){
		Boolean fullInv = true;
		PlayerInventory inv = player.getInventory();
		for (ItemStack stack : inv.getContents()) {
			try {
				if (stack.getType() == (Material.AIR)) {
					fullInv = false;
				}
			} catch (NullPointerException e) {
			}
		}
		return fullInv;
	}
}
