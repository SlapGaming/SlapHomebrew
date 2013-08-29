package me.naithantu.SlapHomebrew.Controllers;

import java.util.ArrayList;
import java.util.List;

import me.naithantu.SlapHomebrew.SlapHomebrew;
import me.naithantu.SlapHomebrew.Storage.YamlStorage;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import ru.tehkode.permissions.PermissionUser;
import ru.tehkode.permissions.bukkit.PermissionsEx;

public class Vip {
	
	private SlapHomebrew plugin;
	private YamlStorage vipStorage;
	private FileConfiguration vipConfig;
	private TabController tabController;

	public Vip(SlapHomebrew plugin, YamlStorage vipStorage, TabController tabController) {
		this.plugin = plugin;
		this.vipStorage = vipStorage;
		vipConfig = vipStorage.getConfig();
		this.tabController = tabController;
	}

	public void promoteVip(String playerName) {
		String playername = playerName.toLowerCase();
		String[] vipGroup = { "VIP" };
		String[] vipGuideGroup = { "VIPGuide" };
		PermissionUser user = PermissionsEx.getUser(playername);
		//Remove old homes permission (if required)
		if (vipConfig.getConfigurationSection("homes").contains(playername)) {
			String permission = "essentials.sethome.multiple." + Integer.toString(getHomes(playername));
			user.removePermission(permission);
		}
		String[] groupNames = user.getGroupsNames();
		if (groupNames[0].contains("Guide")) {
			user.setGroups(vipGuideGroup);
		} else {
			user.setGroups(vipGroup);
		}
		//Update the tab if online
		Player p = plugin.getServer().getPlayerExact(playername);
		if (p != null) {
			tabController.playerSwitchGroup(p);
		}
		
		//Add new homes.
		if (vipConfig.getConfigurationSection("homes").contains(playername)) {
			String permission = "essentials.sethome.multiple." + Integer.toString(getHomes(playername));
			user.addPermission(permission);
		}

		//Add money, mark to promote on forum and send a mail.
		plugin.getEconomy().depositPlayer(playername, 2500);
		Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "vip mark " + playername + " promote");
		Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "mail send " + playername + " " + ChatColor.DARK_AQUA + "[VIP] " + ChatColor.WHITE
				+ "You have been promoted to VIP! For a full list of your new permissions, go to slapgaming.com/vip!");

		//Add to list so they get a book when they log on.
		List<String> playerList = new ArrayList<String>();
		if (vipConfig.contains("book")) 
			playerList = vipConfig.getStringList("book");

		playerList.add(playername);
		vipConfig.set("book", playerList);
		save();
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
		//Update the tab if online
		Player p = plugin.getServer().getPlayerExact(playerName);
		if (p != null) {
			tabController.playerSwitchGroup(p);
		}

		//Add new homes.
		if (vipConfig.getConfigurationSection("homes").contains(playerName)) {
			String permission = "essentials.sethome.multiple." + Integer.toString(getHomes(playerName));
			user.addPermission(permission);
		}
	}

	public int getHomes(String playerName) {
		String playername = playerName.toLowerCase();
		int homes = 0;
		PermissionUser user = PermissionsEx.getUser(playername);
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
		if (vipConfig.getConfigurationSection("homes").contains(playername)) {
			homes += 20 * vipConfig.getConfigurationSection("homes").getInt(playername);
		}
		return homes;
	}

	public void addHomes(String playerName) {
		String playername = playerName.toLowerCase();
		if (vipConfig.getConfigurationSection("homes") == null) {
			vipConfig.createSection("homes");
		}
		PermissionUser user = PermissionsEx.getUser(playername);
		//Remove old homes permission (if required)
		if (vipConfig.getConfigurationSection("homes").contains(playername)) {
			String permission = "essentials.sethome.multiple." + Integer.toString(getHomes(playername));
			user.removePermission(permission);
			//If player has already bought homes in the past, add them.
			vipConfig.getConfigurationSection("homes").set(playername, vipConfig.getConfigurationSection("homes").getInt(playername) + 1);
		} else {
			//Otherwise, just set it to 1.
			vipConfig.getConfigurationSection("homes").set(playername, 1);
		}
		save();
		String permission = "essentials.sethome.multiple." + Integer.toString(getHomes(playername));
		user.addPermission(permission);
	}

	public void save(){
		vipStorage.saveConfig();
	}
	
	public int getVipDays(String player) {
		return vipConfig.getInt("vipdays." + player.toLowerCase());
	}

	public void setVipDays(String player, int days) {
		vipConfig.set("vipdays." + player.toLowerCase(), days);
	}
	
	public void resetVipDays(String player) {
		vipConfig.set("vipdays." + player.toLowerCase(), null);
	}
	
}
