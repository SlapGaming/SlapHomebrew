package me.naithantu.SlapHomebrew;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Set;
import java.util.TimeZone;

import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import me.naithantu.SlapHomebrew.Storage.YamlStorage;

public class PlayerLogger {

	private SlapHomebrew plugin;
	
	private YamlStorage logYML;
	private FileConfiguration logConfig;
	
	private SimpleDateFormat format;
	private SimpleDateFormat onlineFormat;
	
	public PlayerLogger(SlapHomebrew plugin) {
		this.plugin = plugin;
		logYML = new YamlStorage(plugin, "playerlog");
		logConfig = logYML.getConfig();
		onlineFormat = new SimpleDateFormat("dd:HH:mm:ss");
		onlineFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
		format = new SimpleDateFormat("dd-MM-yyyy");
		format.setTimeZone(TimeZone.getTimeZone("GMT"));
		onEnable();
	}
	
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
	
	public String getOnlineTime(String playername, boolean isOnline) {
		ConfigurationSection playerConfig = logConfig.getConfigurationSection("time." + playername);
		long timePlayed = 0;
		if (playerConfig != null) {
			Set<String> keys = playerConfig.getKeys(false);
			for (String key : keys) {
				Long timeToday = playerConfig.getLong(key + ".timetoday");
				timePlayed = timePlayed + timeToday;
			}
			if (isOnline) {
				long currentTime = System.currentTimeMillis();
				String todayString = format.format(new Date(currentTime)) + ".login";
				if (playerConfig.contains(todayString)) {
					timePlayed = timePlayed + (currentTime - playerConfig.getLong(todayString));
				} else {
					String yesterdayString = format.format(new Date(currentTime - 1000 * 60 * 60 * 24 + 1000)) + ".login";
					if (playerConfig.contains(yesterdayString)) {
						timePlayed = timePlayed + (currentTime - playerConfig.getLong(yesterdayString));
					}
				}
			}
		}
		if (timePlayed < 1) {
			return ChatColor.RED + "This player hasn't been online since 9th of august 2013";
		} else {
			return Util.getHeader() + playername + " has played: " + parseTimeLeft(new Date(timePlayed));
		}
	}
	
	public String getOnlineTime(String playername, boolean isOnline, Date from) {
		ConfigurationSection playerConfig = logConfig.getConfigurationSection("time." + playername);
		long timePlayed = 0;
		if (playerConfig != null) {
			for (String dateString : playerConfig.getKeys(false)) {
				Date keyDate = parseDate(dateString);
				if (keyDate != null) {
					if (from.compareTo(keyDate) >= 0) {
						timePlayed = timePlayed + playerConfig.getLong(dateString + ".timetoday");
					}
				}
			}
			if (isOnline) {
				long currentTime = System.currentTimeMillis(); String todayDate = format.format(new Date(currentTime));
				if (playerConfig.contains(todayDate + ".timetoday")) {
					timePlayed = timePlayed + playerConfig.getLong(todayDate + ".timetoday");				
				} else {
					timePlayed = timePlayed + (playerConfig.getLong(todayDate + ".timetoday") - 1000 * 60 * 60 * 24 + 1000);
				}
			}
		}
		if (timePlayed == 0) {
			return ChatColor.RED + "This player hasn't been online since 9th of august 2013";
		} else {
			return Util.getHeader() + playername + " has played: " + parseTimeLeft(new Date(timePlayed));
		}
	}
	
	public String getOnlineTime(String playername, boolean isOnline, Date from, Date till) {
		return ChatColor.RED + "Not supported yet.";
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
	
	private String parseTimeLeft(Date d) {
		System.out.println(d.toString());
		String formatDate = onlineFormat.format(d);
		String daysSub = formatDate.substring(0, 2);
		int days = Integer.parseInt(daysSub);
		String daysS = (days - 1) + "";
		if (daysS.length() == 1) daysS = "0" + daysS;
		formatDate = daysS + formatDate.substring(2);
		String message = "Unkown";
		if (formatDate.matches("00:00:00:[0-9][0-9]")) {
			message = parseSubS(formatDate, 9, 11) + " seconds.";
		} else if (formatDate.matches("00:00:[0-9][0-9]:[0-9][0-9]")) {
			message = parseSubS(formatDate, 6, 8) + " minutes and " + parseSubS(formatDate, 9, 11) + " seconds.";
		} else if (formatDate.matches("00:[0-9][0-9]:[0-9][0-9]:[0-9][0-9]")) {
			message = parseSubS(formatDate, 3, 5) + " hours, " + parseSubS(formatDate, 6, 8) + " minutes and " + parseSubS(formatDate, 9, 11) + " seconds.";
		} else if (formatDate.matches("[0-9][0-9]:[0-9][0-9]:[0-9][0-9]:[0-9][0-9]")) {
			message = parseSubS(formatDate, 0, 2) + " days, " + parseSubS(formatDate, 3, 5) + " hours, " + parseSubS(formatDate, 6, 8) + " minutes and " + parseSubS(formatDate, 9, 11) + " seconds.";
		}
		return message;
	}
	
	public Date parseDate(String dateString){
		Date date = null;
		try {
			date = format.parse(dateString);
		} catch (ParseException e) {}
		return date;
	}
	
	private int parseSubS(String s, int start, int end) {
		int returnInt = -1;
		try {
			returnInt = Integer.parseInt(s.substring(start, end));
		} catch (NumberFormatException e) {}
		return returnInt;
	}
}
