package me.naithantu.SlapHomebrew.Controllers;

import java.text.SimpleDateFormat;
import java.util.Date;

import me.naithantu.SlapHomebrew.Storage.YamlStorage;
import me.naithantu.SlapHomebrew.Util.Util;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

public class Bump extends AbstractController {
	BukkitTask amsgId;
	BukkitTask shortBumpTimer;
	boolean bumpIsDone = true;
	YamlStorage dataStorage;
	FileConfiguration dataConfig;

	public Bump(YamlStorage dataStorage, FileConfiguration dataConfig) {
		this.dataStorage = dataStorage;
		this.dataConfig = dataConfig;
		boolean devServer = false;
		FileConfiguration pluginConfig = plugin.getConfig();
		if (pluginConfig.contains("devserver")) {
			devServer = pluginConfig.getBoolean("devserver");
		} else {
			pluginConfig.set("devserver", false);
			plugin.saveConfig();
		}
		if (!devServer) {
			bumpTimer();
		} else {
			plugin.getLogger().info("[Bump] Running a dev server, Bump is disabled.");
		}
	}

	public void bumpTimer() {
		amsgId = Util.runLater(plugin, new Runnable() {
			public void run() {
				//Aggressive bumping thing here. Start 5 minute timer.
				bumpIsDone = false;
				Util.messagePermissionHolders("bump", ChatColor.RED + "[BUMP] " + ChatColor.GREEN + "Bump the forums! When you're going to, do: " + ChatColor.YELLOW + "/bumpdone");
				if (getOnlineStaff() > 0) {
					shortBumpTimer();
				} else {
					bumpTimer();
				}
			}
		}, 144000);
	}

	public void shortBumpTimer() {
		shortBumpTimer = Util.runLater(plugin, new Runnable() {
			public void run() {
				if (getOnlineStaff() > 0 && !bumpIsDone) {
					Util.messagePermissionHolders("bump", ChatColor.RED + "[BUMP] " + ChatColor.GREEN + "Bump the forums! When you're going to, do: " + ChatColor.YELLOW + "/bumpdone");
					shortBumpTimer();
				} else {
					bumpTimer();
				}
			}
		}, 1200);
	}

	int getOnlineStaff() {
		int onlineStaff = 0;
		for (Player player : Bukkit.getServer().getOnlinePlayers()) {
			if (player.hasPermission("slaphomebrew.bump")) {
				onlineStaff++;
			}
		}
		return onlineStaff;
	}

	public boolean getBumpIsDone() {
		return bumpIsDone;
	}

	public void bump(String playerName) {
		shortBumpTimer.cancel();
		bumpIsDone = true;
		bumpTimer();
		
		//Add message to config.
		String date = new SimpleDateFormat("MMM.d HH:mm z").format(new Date());
		date = date.substring(0, 1).toUpperCase() + date.substring(1);
		dataConfig.set("bumps." + date, playerName);
		dataStorage.saveConfig();

	}
	
    @Override
    public void shutdown() {
    	//Not needed
    }
}
