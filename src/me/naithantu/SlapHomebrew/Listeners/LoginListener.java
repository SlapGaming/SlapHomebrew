package me.naithantu.SlapHomebrew.Listeners;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import me.naithantu.SlapHomebrew.SlapHomebrew;
import me.naithantu.SlapHomebrew.Storage.YamlStorage;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;

import ru.tehkode.permissions.PermissionUser;
import ru.tehkode.permissions.bukkit.PermissionsEx;

public class LoginListener implements Listener {
	SlapHomebrew plugin;
	YamlStorage timeConfig;
	YamlStorage dataConfig;
	YamlStorage vipConfig;
	
	public LoginListener(SlapHomebrew plugin){
		this.plugin = plugin;
		timeConfig = plugin.getTimeConfig();
		dataConfig = plugin.getDataConfig();
		vipConfig = plugin.getVipConfig();
	}
	

	@EventHandler
	public void onPlayerLogin(PlayerLoginEvent event) {
		final Player player = event.getPlayer();
		if(player.hasPermission("slaphomebrew.staff")){
			String date = new SimpleDateFormat("MMM-d HH:mm:ss z").format(new Date());
			date = date.substring(0, 1).toUpperCase() + date.substring(1);
			addToConfig(date, player.getName() + " logged in.");
			timeConfig.saveConfig();
		}
		
		//Plot message
		if (player.hasPermission("slaphomebrew.plot.admin") && plugin.getUnfinishedPlots().size() > 0) {
			plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
				public void run() {
					player.sendMessage(ChatColor.GREEN + "There are " + plugin.getUnfinishedPlots().size() + " unfinished plot requests! Type /plot check to see them!");
				}
			}, 10);
		}
		if (player.hasPermission("slaphomebrew.vip.check") && plugin.getUnfinishedForumVip().size() > 0) {
			plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
				public void run() {
					player.sendMessage(ChatColor.GREEN + "There are " + plugin.getUnfinishedForumVip().size() + " waiting forum promotions/demotions! Type /vip check to see them!");
				}
			}, 10);
		}

		//Vip grant reset
		DateFormat checkDay = new SimpleDateFormat("dd");
		DateFormat checkMonth = new SimpleDateFormat("MM");
		DateFormat checkYear = new SimpleDateFormat("yyyy");
		Date date = new Date();
		int vipDay = Integer.valueOf(checkDay.format(date));
		int vipMonth = Integer.valueOf(checkMonth.format(date));
		int vipYear = Integer.valueOf(checkYear.format(date));
		if (vipDay > dataConfig.getInt("vipdate.day") || vipMonth > dataConfig.getInt("vipdate.month") || vipYear > dataConfig.getInt("vipdate.year")) {
			SlapHomebrew.usedGrant.clear();
			updateVipDays();
		}
		dataConfig.set("vipdate.day", vipDay);
		dataConfig.set("vipdate.month", vipMonth);
		dataConfig.set("vipdate.year", vipYear);
		dataConfig.saveConfig();

		//Check homes.
		if (vipConfig.getConfigurationSection("homes") != null) {
			if (vipConfig.getConfigurationSection("homes").contains(player.getName())) {
				if (!player.hasPermission("essentials.sethome.multiple." + Integer.toString(plugin.getHomes(player.getName())))) {
					PermissionUser user = PermissionsEx.getUser(player.getName());
					String permission = "essentials.sethome.multiple." + Integer.toString(plugin.getHomes(player.getName()));
					user.addPermission(permission);
				}
			}
		}
	}
	
	private void updateVipDays() {
		HashMap<String, Integer> tempMap = new HashMap<String, Integer>();
		if (vipConfig.getConfigurationSection("vipdays") == null)
			return;
		for (String key : vipConfig.getConfigurationSection("vipdays").getKeys(false)) {
			tempMap.put(key, vipConfig.getInt("vipdays." + key));
		}
		vipConfig.set("vipdays", null);
		//Create configurationsection if it isn't there yet:
		if (vipConfig.getConfigurationSection("vipdays") == null) {
			vipConfig.createSection("vipdays");
		}
		for (Map.Entry<String, Integer> entry : tempMap.entrySet()) {
			PermissionUser user = PermissionsEx.getUser(entry.getKey());
			if (entry.getValue() != -1 && !user.has("slaphomebrew.vip.freeze")) {
				entry.setValue(entry.getValue() - 1);
			}
			if (entry.getValue() == 0) {
				plugin.demoteVip(entry.getKey());
			} else {
				vipConfig.getConfigurationSection("vipdays").set(entry.getKey(), entry.getValue());
			}
		}
		vipConfig.saveConfig();
	}
	
	void addToConfig(String date, String message){
		int i = 1;
		while(timeConfig.contains(date)){
			date += "(" + i + ")";
			i++;
		}
		timeConfig.set(date, message);
	}
}
