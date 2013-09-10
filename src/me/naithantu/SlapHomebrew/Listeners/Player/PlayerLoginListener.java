package me.naithantu.SlapHomebrew.Listeners.Player;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import me.naithantu.SlapHomebrew.SlapHomebrew;
import me.naithantu.SlapHomebrew.Controllers.Book;
import me.naithantu.SlapHomebrew.Controllers.Jails;
import me.naithantu.SlapHomebrew.Controllers.Mail;
import me.naithantu.SlapHomebrew.Controllers.PlayerLogger;
import me.naithantu.SlapHomebrew.Controllers.TabController;
import me.naithantu.SlapHomebrew.Storage.YamlStorage;
import me.naithantu.SlapHomebrew.Util.Util;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;

import com.earth2me.essentials.User;

import ru.tehkode.permissions.PermissionUser;
import ru.tehkode.permissions.bukkit.PermissionsEx;

public class PlayerLoginListener implements Listener {
	SlapHomebrew plugin;
	YamlStorage timeStorage;
	YamlStorage dataStorage;
	YamlStorage vipStorage;

	FileConfiguration timeConfig;
	FileConfiguration dataConfig;
	FileConfiguration vipConfig;
	
	private Mail mail;
	private Jails jails;
	private PlayerLogger playerLogger;
	private TabController tabController;

	public PlayerLoginListener(SlapHomebrew plugin, YamlStorage timeStorage, YamlStorage dataStorage, YamlStorage vipStorage, Mail mail, Jails jails, PlayerLogger playerLogger, TabController tabController) {
		this.plugin = plugin;
		this.timeStorage = timeStorage;
		this.dataStorage = dataStorage;
		this.vipStorage = vipStorage;
		timeConfig = timeStorage.getConfig();
		dataConfig = dataStorage.getConfig();
		vipConfig = vipStorage.getConfig();
		this.mail = mail;
		this.jails = jails;
		this.playerLogger = playerLogger;
		this.tabController = tabController;
	}

	@EventHandler
	public void onPlayerLogin(PlayerLoginEvent event) {
		final Player player = event.getPlayer();

		plugin.getExtras().getGhostTeam().addPlayer(player);

		if (player.getWorld().getName().equals("world_start"))
			player.setAllowFlight(true);

		if (player.hasPermission("slaphomebrew.staff")) {
			String date = new SimpleDateFormat("MMM-d HH:mm:ss z").format(new Date());
			date = date.substring(0, 1).toUpperCase() + date.substring(1);
			Util.dateIntoTimeConfig(date, player.getName() + " logged in", timeStorage);
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
			YamlStorage dataStorage = plugin.getDataStorage();
			dataStorage.getConfig().set("usedgrant", null);
			dataStorage.saveConfig();
			updateVipDays();
		}
		dataConfig.set("vipdate.day", vipDay);
		dataConfig.set("vipdate.month", vipMonth);
		dataConfig.set("vipdate.year", vipYear);
		dataStorage.saveConfig();

		//Check homes.
		if (vipConfig.getConfigurationSection("homes") != null) {
			if (vipConfig.getConfigurationSection("homes").contains(player.getName())) {
				if (!player.hasPermission("essentials.sethome.multiple." + Integer.toString(plugin.getVip().getHomes(player.getName())))) {
					PermissionUser user = PermissionsEx.getUser(player.getName());
					String permission = "essentials.sethome.multiple." + Integer.toString(plugin.getVip().getHomes(player.getName()));
					user.addPermission(permission);
				}
			}
		}

		//Check vip book
		if (vipConfig.getStringList("book") != null) {
			List<String> playerList = vipConfig.getStringList("book");
			if (playerList.contains(player.getName())) {
				System.out.println("Giving book!");
				plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
					public void run() {
						player.getInventory().addItem(Book.getBook(new YamlStorage(plugin, "bookStorage")));
					}
				}, 1);
				playerList.remove(player.getName());
				vipConfig.set("book", playerList);
				vipStorage.saveConfig();
			}
		}	
		
		//Log login time
		playerLogger.setLoginTime(player.getName());
		
		//Add to Tab
		tabController.playerJoin(player);
		
		//First time join?
		User u = plugin.getEssentials().getUserMap().getUser(player.getName());
		boolean firstTime = false;
		if (u == null) {
			firstTime = true;
		}
		final boolean fFirstTime = firstTime;
		
		Util.runLater(plugin, new Runnable() {
			
			@Override
			public void run() {
				//Minechat prevention
				playerLogger.joinedMinechatChecker(player);
				
				//Throw in jail
				if (jails.isInJail(player.getName())) {
					jails.switchToOnlineJail(player);
				}
				
				//Check mails
				mail.hasNewMail(player);
				
				//First time broadcast
				if (fFirstTime) {
					plugin.getServer().broadcastMessage(Util.getHeader() + "Welcome " + ChatColor.GREEN + player.getName() + ChatColor.WHITE + " to the SlapGaming Minecraft Server. If you need help please contact a " + ChatColor.GOLD + "Guide" + ChatColor.WHITE + ", " +
							ChatColor.AQUA + "Mod" + ChatColor.WHITE + " or " + ChatColor.RED + "Admin" + ChatColor.WHITE +
							" by typing " + ChatColor.RED + "/modreq [message]" + ChatColor.WHITE + "!");
				}
				
			}
		}, 10);
		
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
				plugin.getVip().demoteVip(entry.getKey());
			} else {
				vipConfig.getConfigurationSection("vipdays").set(entry.getKey(), entry.getValue());
			}
		}
		vipStorage.saveConfig();
	}
}
