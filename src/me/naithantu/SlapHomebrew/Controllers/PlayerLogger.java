package me.naithantu.SlapHomebrew.Controllers;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Set;
import java.util.TimeZone;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import com.earth2me.essentials.User;
import com.earth2me.essentials.UserMap;

import ru.tehkode.permissions.PermissionManager;
import ru.tehkode.permissions.PermissionUser;
import ru.tehkode.permissions.bukkit.PermissionsEx;

import me.naithantu.SlapHomebrew.SlapHomebrew;
import me.naithantu.SlapHomebrew.Storage.YamlStorage;
import me.naithantu.SlapHomebrew.Util.Util;

public class PlayerLogger {

	private SlapHomebrew plugin;
	
	private YamlStorage logYML;
	private FileConfiguration logConfig;
	
	private SimpleDateFormat format;
	private SimpleDateFormat onlineFormat;
	
	private Comparator<TimePlayer> comp;
	
	public PlayerLogger(SlapHomebrew plugin) {
		this.plugin = plugin;
		logYML = new YamlStorage(plugin, "playerlog");
		logConfig = logYML.getConfig();
		onlineFormat = new SimpleDateFormat("dd:HH:mm:ss");
		onlineFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
		format = new SimpleDateFormat("dd-MM-yyyy");
		format.setTimeZone(TimeZone.getTimeZone("GMT"));
		createComp();
		onEnable();
	}
	
	/*
	 * SET IN CONFIG
	 */
	public void setLoginTime(String playername) {
		logConfig.set("time." + playername + "." + format.format(new Date()) + ".login", System.currentTimeMillis()); 
		save();
	}
	
	public void setLogoutTime(String playername) {
		long loginTime = 0; long timeToday = 0; long currentTime = System.currentTimeMillis();
		String todayString = "time." + playername + "." + format.format(new Date(currentTime)) + ".";
		if (logConfig.contains(todayString + "login")) {
			loginTime = logConfig.getLong(todayString + "login");
			logConfig.set(todayString + "login", null);
		} else {
			String yesterdayString = "time." + playername + "." + format.format(new Date(currentTime - 1000*60*60*24 + 1000)) + ".login";
			loginTime = logConfig.getLong(yesterdayString);
			logConfig.set(yesterdayString, null);
		}
		
		if (logConfig.contains(todayString + "timetoday")) {
			timeToday = logConfig.getLong(todayString + "timetoday");
		}
		
		if (loginTime != 0) {
			Long timePlayed = currentTime - loginTime;
			timePlayed = timePlayed + timeToday;
			logConfig.set(todayString + "timetoday", timePlayed);
		}
		save();
	}
	
	
	/*
	 * TIME CALCULATORS
	 */
	private long getPlayTime(String playername, boolean isOnline) {
		ConfigurationSection playerConfig = logConfig.getConfigurationSection("time." + playername);
		long timePlayed = -1;
		if (playerConfig != null) {
			timePlayed = 0;
			Set<String> keys = playerConfig.getKeys(false);
			for (String key : keys) {
				Long timeToday = playerConfig.getLong(key + ".timetoday");
				timePlayed = timePlayed + timeToday;
			}
			if (isOnline) {
				long currentTime = System.currentTimeMillis();
				timePlayed = timePlayed + getOnlinePlayTime(playername, playerConfig, currentTime);
			}
		}
		return timePlayed;
	}
	
	private long getPlayTime(String playername, boolean isOnline, Date fromDate) {
		ConfigurationSection playerConfig = logConfig.getConfigurationSection("time." + playername);
		long timePlayed = -1;
		if (playerConfig != null) {
			timePlayed = 0;
			long currentTime = System.currentTimeMillis();
			Set<String> keys = playerConfig.getKeys(false);
			for (String key : keys) {
				try {
					if (format.parse(key).after(fromDate)) {
						timePlayed = timePlayed + playerConfig.getLong(key + ".timetoday");
					}
				} catch (Exception e) {}
			}
			if (isOnline) {
				timePlayed = timePlayed + getOnlinePlayTime(playername, playerConfig, currentTime);
			}
		}
		return timePlayed;
	}
	
	private long getOnlinePlayTime(String playername, ConfigurationSection section, long currentTime) {
		long timePlayed = 0;
		String todayString = format.format(new Date(currentTime)) + ".login";
		if (section.contains(todayString)) {
			timePlayed = timePlayed + (currentTime - section.getLong(todayString));
		} else {
			String yesterdayString = format.format(new Date(currentTime - 1000 * 60 * 60 * 24 + 1000)) + ".login";
			if (section.contains(yesterdayString)) {
				timePlayed = timePlayed + (currentTime - section.getLong(yesterdayString));
			}
		}
		return timePlayed;
	}
	
	
	/*
	 * COMMANDS
	 */
	public void getOnlineTime(final CommandSender sender, final String playername, final boolean isOnline) {
		Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
			
			@Override
			public void run() {
				long timePlayed = getPlayTime(playername, isOnline);
				if (timePlayed < 1) {
					sender.sendMessage(ChatColor.RED + "This player hasn't been online since 11th of august 2013");
				} else {
					sender.sendMessage(Util.getHeader() + playername + " has played: " + Util.getTimePlayedString(timePlayed) + ".");
				}
			}
		});
	}
	
	public void getOnlineTime(final CommandSender sender, final String playername, final boolean isOnline, final Date from) {
		Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
			
			@Override
			public void run() {
				long timePlayed = getPlayTime(playername, isOnline, from);
				if (timePlayed < 1) {
					sender.sendMessage(ChatColor.RED + "This player hasn't been online since " + format.format(from));
				} else {
					sender.sendMessage(Util.getHeader() + playername + " has played: " + Util.getTimePlayedString(timePlayed) + ".");
				}
			}
		});
	}
	
	public String getOnlineTime(String playername, boolean isOnline, Date from, Date till) {
		return ChatColor.RED + "Not supported yet.";
	}
	
	public void getTimeList(final CommandSender sender, final boolean staff, final int nr, final Date fromDate) {
		Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
			
			@Override
			public void run() {
				ConfigurationSection pConfig = logConfig.getConfigurationSection("time");
				if (pConfig != null) {
					String msgString = Util.getHeader() + "Checking time";
					if (fromDate != null) {
						msgString = msgString + " since " + format.format(fromDate);
					}
					sender.sendMessage(msgString + "..");
					int sendPlayers = 0;
					UserMap eMap = plugin.getEssentials().getUserMap();
					ArrayList<TimePlayer> players = new ArrayList<>();
					if (staff) {
						PermissionManager pManager = PermissionsEx.getPermissionManager();
						String[] groups = new String[]{"SuperAdmin", "Admin", "VIPGuide", "Guide", "Mod"};
						for (String group : groups) {
							for (PermissionUser user : pManager.getGroup(group).getUsers()) {
								User u = eMap.getUser(user.getName());
								if (u != null) {
									String playername = u.getName();
									if (fromDate == null) {
										players.add(new TimePlayer(playername, getPlayTime(playername, u.isOnline())));
									} else {
										players.add(new TimePlayer(playername, getPlayTime(playername, u.isOnline(), fromDate)));
									}
								}
							}
						}
						sendPlayers = players.size();
					} else {
						for (String player : pConfig.getKeys(false)) {
							User u = eMap.getUser(player);
							if (u != null) {
								String playername = u.getName();
								if (fromDate == null) {
									players.add(new TimePlayer(playername, getPlayTime(playername, u.isOnline())));
								} else {
									players.add(new TimePlayer(playername, getPlayTime(playername, u.isOnline(), fromDate)));
								}
							}
						}
						sendPlayers = nr;
					}
					Collections.sort(players, comp);
					int x = 0; int arraySize = players.size();
					while (x < sendPlayers && x < arraySize) {
						TimePlayer p = players.get(x);
						sender.sendMessage(ChatColor.GREEN + String.valueOf(x + 1) + ChatColor.GRAY + "-" + p.playername + ": " + ChatColor.WHITE + Util.getTimePlayedString(p.timePlayed));
						x++;
					}
				} else {
					Util.badMsg(sender, "No times found.");
				}
			}
		});
	}
	
	private class TimePlayer {
		String playername;
		long timePlayed;
		
		public TimePlayer(String playerName, long timePlayed) {
			this.playername = playerName;
			this.timePlayed = timePlayed;
		}
	}
	
	private void createComp() {
		comp = new Comparator<TimePlayer>() {
			
			@Override
			public int compare(TimePlayer o1, TimePlayer o2) {
				if (o1.timePlayed < o2.timePlayed) return 1;
				else if (o1.timePlayed > o2.timePlayed) return -1;
				return 0;
			}
		};
	}
	
	private void save(){
		logYML.saveConfig();
	}
	
	public void onEnable(){
		for (Player onlinePlayer : plugin.getServer().getOnlinePlayers()) {
			String date = format.format(new Date()); 
			logConfig.set("time." + onlinePlayer.getName() + "." + date, System.currentTimeMillis()); 
		}
	}

	public void onDisable(){
		for (Player onlinePlayer : plugin.getServer().getOnlinePlayers()) {
			setLogoutTime(onlinePlayer.getName());
		}
	}
	
	public Date parseDate(String dateString){
		Date date = null;
		try {
			date = format.parse(dateString);
		} catch (ParseException e) {}
		return date;
	}
	
}
