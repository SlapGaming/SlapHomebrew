package me.naithantu.SlapHomebrew;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import me.naithantu.SlapHomebrew.Storage.YamlStorage;

public class Jails {

	private SlapHomebrew plugin;
	
	private YamlStorage jailYML;
	private FileConfiguration jailConfig;
	
	private SimpleDateFormat timeLeftFormat;
	
	public Jails(SlapHomebrew plugin) {
		this.plugin = plugin;
		jailYML = new YamlStorage(plugin, "jails");
		jailConfig = jailYML.getConfig();
		timeLeftFormat = new SimpleDateFormat("HH:mm:ss");
	}
	
	public void createJail(String jailName, Location loc, boolean chatAllowed, boolean msgCommandsAllowed) {
		String jailInConfig = "jails." + jailName + ".";
		jailConfig.set(jailInConfig + "location.world", loc.getWorld());
		jailConfig.set(jailInConfig + "location.X", loc.getBlockX());
		jailConfig.set(jailInConfig + "location.Y", loc.getBlockY());
		jailConfig.set(jailInConfig + "location.Z", loc.getBlockZ());
		jailConfig.set(jailInConfig + "location.pitch", loc.getPitch());
		jailConfig.set(jailInConfig + "location.yaw", loc.getYaw());
		jailConfig.set(jailInConfig + "chatallowed", chatAllowed);
		jailConfig.set(jailInConfig + "msgallowed", msgCommandsAllowed);
		save();
	}
	
	public void deleteJail(String jailName) {
		jailConfig.set("jails." + jailName, null);
		save();
	}
	
	public boolean jailExists(String jailName) {
		return jailConfig.contains("jails." + jailName);
	}
	
	public boolean isInJail(String playerName) {
		return jailConfig.contains("jailed." + playerName);
	}
	
	public boolean putOnlinePlayerInJail(Player jailedPlayer, String reason, String jail, long timeLeft) {
		String jailedPlayerName = jailedPlayer.getName();
		String personInConfig = "jailed." + jailedPlayerName + ".";
		jailConfig.set(personInConfig + "reason", reason);
		jailConfig.set(personInConfig + "jail", jail);
		jailConfig.set(personInConfig + "releaseon", System.currentTimeMillis() + timeLeft);
		jailConfig.set(personInConfig + "injail", true);
		Location playerLoc = jailedPlayer.getLocation();
		setOldLocInConfig(playerLoc, personInConfig);
		save();
		if (putInJail(jailedPlayer, timeLeft, jail, reason)) {
			return true;
		} else {
			jailConfig.set("jailed." + jailedPlayerName, null);
			save();
			return false;
		}
	}
	
	public void putOfflinePlayerInJail(String playerName, String reason, String jail, long timeLeft) {
		String personInConfig = "jailed." + playerName + ".";
		jailConfig.set(personInConfig + "reason", reason);
		jailConfig.set(personInConfig + "jail", jail);
		jailConfig.set(personInConfig + "timeleft", timeLeft);
		jailConfig.set(personInConfig + "injail", false);
		save();
	}
	
	public void switchToOnlineJail(Player jailedPlayer) {
		String jailedPlayerName = jailedPlayer.getName();
		String personInConfig = "jailed." + jailedPlayerName + ".";
		long timeLeft = jailConfig.getLong(personInConfig + "timeleft");
		if (timeLeft <= 0) {
			releasePlayerFromJail(jailedPlayer);
		} else {
			if (!jailConfig.getBoolean(personInConfig + "injail")) {
				setOldLocInConfig(jailedPlayer.getLocation(), personInConfig);
				if (putInJail(jailedPlayer, timeLeft, jailConfig.getString(personInConfig + "jail"), jailConfig.getString(personInConfig + "reason"))) {
					jailConfig.set(personInConfig + "injail", true);
				} else {
					jailConfig.set("jailed." + jailedPlayerName, null);
					save();
					return;
				}
			}
			jailConfig.set(personInConfig + "releaseon", System.currentTimeMillis() + timeLeft);
			jailConfig.set(personInConfig + "timeleft", null);
			save();
		}
	}
	
	public void switchToOfflineJail(Player jailedPlayer) {
		String personInConfig = "jailed." + jailedPlayer.getName() + ".";
		Long releaseOn = jailConfig.getLong(personInConfig + "releaseon");
		Long timeLeft = releaseOn - System.currentTimeMillis();
		jailConfig.set(personInConfig + "releaseon", null);
		jailConfig.set(personInConfig + "timeleft", timeLeft);
		save();
	}
	
	private void setOldLocInConfig(Location playerLoc, String personInConfig) {
		jailConfig.set(personInConfig + "oldloc.world", playerLoc.getWorld());
		jailConfig.set(personInConfig + "oldloc.X", playerLoc.getBlockX());
		jailConfig.set(personInConfig + "oldloc.Y", playerLoc.getBlockY());
		jailConfig.set(personInConfig + "oldloc.Z", playerLoc.getBlockZ());
		jailConfig.set(personInConfig + "oldloc.pitch", playerLoc.getPitch());
		jailConfig.set(personInConfig + "oldloc.yaw", playerLoc.getYaw());
	}
	
	private boolean putInJail(Player jailedPlayer, long timeLeft, String jail, String reason) {
		Location loc = parseLocationConfig(jailConfig, "jails." + jail + ".location");
		if (loc != null) {
			jailedPlayer.teleport(loc);
			String timeLeftMessage = parseTimeLeft(new Date(timeLeft));
			jailedPlayer.sendMessage(new String[] {Util.getHeader() + "You have been jailed. Reason: " + ChatColor.GREEN + reason, Util.getHeader() + "Time left in jail: " + timeLeftMessage});
			for (Player onlinePlayer : plugin.getServer().getOnlinePlayers()) {
				if (!onlinePlayer.getName().equals(jailedPlayer.getName())) onlinePlayer.sendMessage(Util.getHeader() + jailedPlayer.getName() + " has been jailed.");
			}
			return true;
		} else return false;
	}
	
	public void releasePlayerFromJail(String playerName) {
		Player freePlayer = plugin.getServer().getPlayer(playerName);
		if (freePlayer != null) {
			releasePlayerFromJail(freePlayer);
		} else {
			releaseOfflinePlayerFromJail(playerName);
		}
	}
	
	private void releasePlayerFromJail(Player freePlayer) {
		String personInConfig = "jailed." + freePlayer.getName() + ".";
		if (jailConfig.getBoolean(personInConfig + "injail")) {
			Location oldLoc = parseLocationConfig(jailConfig, personInConfig + "oldloc");
			if (oldLoc != null) freePlayer.teleport(oldLoc);
			else freePlayer.sendMessage(Util.getHeader() + "You are free. Use /spawn to get out of the jail.");
		}
		jailConfig.set("jailed." + freePlayer.getName(), null);
		save();
	}
	
	private void releaseOfflinePlayerFromJail(String playername) {
		jailConfig.set("jailed." + playername + "timeleft", 0);
		save();
	}
	
	public List<String> getJailList(){
		ArrayList<String> jailList = new ArrayList<>();
		for (String key : jailConfig.getKeys(true)) {
			if (key.contains("jails.")) {
				String[] splitKey = key.replace(".", "-").split("-");
				if (splitKey.length == 2) {
					jailList.add(splitKey[1]);
				}
			}
		}
		return jailList;
	}
	
	
	private void save() {
		jailYML.saveConfig();
	}
	
	public void getJailInfo(CommandSender sender, String jailedPlayer) {
		String personInConfig = "jailed." + jailedPlayer + ".";
		long timeLeft;
		if (jailConfig.contains(personInConfig + "timeleft")) timeLeft = jailConfig.getLong(personInConfig + "timeleft");
		else timeLeft = jailConfig.getLong(personInConfig + "releaseon") - System.currentTimeMillis();
		sender.sendMessage(new String[] {Util.getHeader() + jailedPlayer + " is in jail: " + jailConfig.getString(personInConfig + "jail") + ". Time left: " + parseTimeLeft(new Date(timeLeft)), Util.getHeader() + "Reason: " +
				ChatColor.GREEN + jailConfig.getString(personInConfig + "reason")});
	}
	
	public void getJailInfo(Player jailedPlayer) {
		String personInConfig = "jailed." + jailedPlayer.getName() + ".";
		jailedPlayer.sendMessage(new String[] {Util.getHeader() + "You are jailed. Time left: " + parseTimeLeft(new Date(jailConfig.getLong(personInConfig + "releaseon") - System.currentTimeMillis())), Util.getHeader() + "Reason: " +
				ChatColor.GREEN + jailConfig.getString(personInConfig + "reason")});
	}
	
	public static Location parseLocationConfig(FileConfiguration config, String pathToLoc) {
		Location loc = null;
		if (config.contains(pathToLoc)) {
			try {
				String worldName = config.getString(pathToLoc + ".world");
				World world = null;
				if (worldName != null) world = Bukkit.getWorld(worldName);
				double locX = config.getDouble(pathToLoc + ".X");
				double locY = config.getDouble(pathToLoc + ".Y");
				double locZ = config.getDouble(pathToLoc + ".Z");
				float locPitch = Float.parseFloat(config.getString(pathToLoc + ".pitch"));
				float locYaw = Float.parseFloat(config.getString(pathToLoc + ".yaw"));
				if (world != null && locX != 0 && locY != 0 && locZ != 0 && locPitch != 0 && locYaw != 0) {
					loc = new Location(world, locX, locY, locZ, locYaw, locPitch);
				}
			} catch (NumberFormatException | NullPointerException e) {}
		}
		return loc;
	}
	
	public String parseTimeLeft(Date d) {
		String timeLeftString = timeLeftFormat.format(d);
		String timeLeftMessage = "Unkown.";
		if (timeLeftString.matches("00:00:[0-9][0-9]")) {
			timeLeftMessage = Integer.parseInt(timeLeftString.substring(6, 8)) + " seconds.";
		} else if (timeLeftString.matches("00:[0-9][0-9]:[0-9][0-9]")) {
			timeLeftMessage = Integer.parseInt(timeLeftString.substring(3, 5)) + " minutes and " + Integer.parseInt(timeLeftString.substring(6, 8)) + " seconds.";
		} else if (timeLeftString.matches("[0-9][0-9]:[0-9][0-9]:[0-9][0-9]")) {
			timeLeftMessage = Integer.parseInt(timeLeftMessage.substring(0, 2)) + " hours, " + Integer.parseInt(timeLeftString.substring(3, 5)) + " minutes and " + Integer.parseInt(timeLeftString.substring(6, 8)) + " seconds.";
		}
		return timeLeftMessage;
	}

}
