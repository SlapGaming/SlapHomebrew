package me.naithantu.SlapHomebrew;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import me.naithantu.SlapHomebrew.Storage.YamlStorage;

import org.bukkit.Bukkit;
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

	public static void dateIntoTimeConfig(String date, String message, YamlStorage timeStorage) {
		FileConfiguration timeConfig = timeStorage.getConfig();
		int i = 1;
		while (timeConfig.contains(date)) {
			date += " (" + i + ")";
			i++;
		}
		timeConfig.set(date, message);
		timeStorage.saveConfig();
	}

	public static boolean hasFlag(SlapHomebrew plugin, Location location, Flag flag) {
		RegionManager regionManager = plugin.getWorldGuard().getRegionManager(location.getWorld());
		ApplicableRegionSet regions = regionManager.getApplicableRegions(location);
		for (ProtectedRegion region : regions) {
			for (String string : region.getMembers().getPlayers()) {
				if (string.startsWith("flag:" + flag.toString().toLowerCase()))
					return true;
			}
		}
		return false;
	}

	public static String getFlag(SlapHomebrew plugin, Location location, Flag flag) {
		RegionManager regionManager = plugin.getWorldGuard().getRegionManager(location.getWorld());
		ApplicableRegionSet regions = regionManager.getApplicableRegions(location);
		for (ProtectedRegion region : regions) {
			for (String string : region.getMembers().getPlayers()) {
				if (string.startsWith("flag:" + flag.toString().toLowerCase()))
					return string;
			}
		}
		return null;
	}

	public static boolean hasEmptyInventory(Player player) {
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

	public static void broadcastToWorld(String worldName, String message) {
		for (Player player : Bukkit.getServer().getOnlinePlayers()) {
			if (player.getWorld().getName().equalsIgnoreCase(worldName)) {
				player.sendMessage(message);
			}
		}
	}

	public static String changeTimeFormat(long time) {
		final Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(time);
		final String timeString = new SimpleDateFormat("mm:ss:SS").format(cal.getTime());
		return timeString;
	}
}
