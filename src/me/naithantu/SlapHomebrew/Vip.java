package me.naithantu.SlapHomebrew;

import me.naithantu.SlapHomebrew.Storage.YamlStorage;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;

import ru.tehkode.permissions.PermissionUser;
import ru.tehkode.permissions.bukkit.PermissionsEx;

public class Vip {
	YamlStorage vipStorage;
	FileConfiguration vipConfig;

	public Vip(YamlStorage vipStorage) {
		this.vipStorage = vipStorage;
		vipConfig = vipStorage.getConfig();
	}

	public void promoteVip(String playerName) {
		String[] vipGroup = { "VIP" };
		String[] vipGuideGroup = { "VIPGuide" };
		PermissionUser user = PermissionsEx.getUser(playerName);
		//Remove old homes permission (if required)
		if (vipConfig.getConfigurationSection("homes").contains(playerName)) {
			String permission = "essentials.sethome.multiple." + Integer.toString(getHomes(playerName));
			user.removePermission(permission);
		}
		String[] groupNames = user.getGroupsNames();
		if (groupNames[0].contains("Guide")) {
			user.setGroups(vipGuideGroup);
		} else {
			user.setGroups(vipGroup);
		}
		//Add new homes.
		if (vipConfig.getConfigurationSection("homes").contains(playerName)) {
			String permission = "essentials.sethome.multiple." + Integer.toString(getHomes(playerName));
			user.addPermission(permission);
		}
		
		//Add money, mark to promote on forum and send a mail.
		SlapHomebrew.econ.depositPlayer(playerName, 2500);
		Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "vip mark " + playerName + " promote");
		Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "mail send " + playerName + " " + ChatColor.DARK_AQUA + "[VIP] " + ChatColor.WHITE
				+ "You have been promoted to VIP! For a full list of your new permissions, go to slapgaming.com/vip!");
	}

	public void demoteVip(String playerName) {
		String[] memberGroup = { "member" };
		String[] guideGroup = { "Guide" };
		PermissionUser user = PermissionsEx.getUser(playerName);
		String[] groupNames = user.getGroupsNames();

		//Remove old homes permission (if required)
		if (vipConfig.getConfigurationSection("homes").contains(playerName)) {
			String permission = "essentials.sethome.multiple." + Integer.toString(getHomes(playerName));
			user.removePermission(permission);
		}

		if (groupNames[0].contains("VIPGuide")) {
			//If they were a VIPGuide, demote to guide. Nothing else needs to be changed so no need to vip mark.
			user.setGroups(guideGroup);
			Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "mail send " + playerName + " " + ChatColor.DARK_AQUA + "[VIP] " + ChatColor.WHITE
					+ "You have been demoted to guide! Please visit slapgaming.com/vip to renew your VIP!");
		} else if (groupNames[0].contains("VIP")) {
			//If they were a VIP, demote to member and mark to remove forum & ventrilo VIP.
			user.setGroups(memberGroup);
			Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "mail send " + playerName + " " + ChatColor.DARK_AQUA + "[VIP] " + ChatColor.WHITE
					+ "You have been demoted to member! Please visit slapgaming.com/vip to renew your VIP!");
			Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "vip mark " + playerName + " demote");
		}

		//Add new homes.
		if (vipConfig.getConfigurationSection("homes").contains(playerName)) {
			String permission = "essentials.sethome.multiple." + Integer.toString(getHomes(playerName));
			user.addPermission(permission);
		}
	}

	public int getHomes(String playerName) {
		int homes = 0;
		PermissionUser user = PermissionsEx.getUser(playerName);
		String[] group = user.getGroupsNames();
		//Get group homes.
		if (group[0].equalsIgnoreCase("builder")) {
			homes = 1;
		} else if (group[0].equalsIgnoreCase("member") || group[0].equalsIgnoreCase("guide") || group[0].equalsIgnoreCase("vipguide")) {
			homes = 4;
		} else if (group[0].equalsIgnoreCase("slap")) {
			homes = 6;
		} else if (group[0].equalsIgnoreCase("vip")) {
			homes = 8;
		} else if (group[0].equalsIgnoreCase("mod")) {
			homes = 15;
		} else if (group[0].equalsIgnoreCase("admin")) {
			homes = 25;
		}
		//Add bought homes.
		if (vipConfig.getConfigurationSection("homes").contains(playerName)) {
			homes += 20 * vipConfig.getConfigurationSection("homes").getInt(playerName);
		}
		return homes;
	}

	public void addHomes(String playerName) {
		if (vipConfig.getConfigurationSection("homes") == null) {
			vipConfig.createSection("homes");
		}
		PermissionUser user = PermissionsEx.getUser(playerName);
		//Remove old homes permission (if required)
		if (vipConfig.getConfigurationSection("homes").contains(playerName)) {
			String permission = "essentials.sethome.multiple." + Integer.toString(getHomes(playerName));
			user.removePermission(permission);
			//If player has already bought homes in the past, add them.
			vipConfig.getConfigurationSection("homes").set(playerName, vipConfig.getConfigurationSection("homes").getInt(playerName) + 1);
		} else {
			//Otherwise, just set it to 1.
			vipConfig.getConfigurationSection("homes").set(playerName, 1);
		}
		vipStorage.saveConfig();
		String permission = "essentials.sethome.multiple." + Integer.toString(getHomes(playerName));
		user.addPermission(permission);
	}
}
