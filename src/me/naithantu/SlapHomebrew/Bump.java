package me.naithantu.SlapHomebrew;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class Bump {
	SlapHomebrew plugin;
	int amsgId;
	int shortBumpTimer;
	boolean bumpIsDone = true;

	
	public Bump(SlapHomebrew plugin){
		this.plugin = plugin;
	}
	
	public void bumpTimer() {
		amsgId = Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
			public void run() {
				//Aggresive bumping thing here. Start 5 minute timer.
				bumpIsDone = false;
				Bukkit.dispatchCommand(Bukkit.getServer().getConsoleSender(), "mod-broadcast Post On Yogscast/Minecraftforums, Use /Bumpdone When You Are Going Bump!");
				if (getOnlineStaff() > 0) {
					shortBumpTimer();
				} else {
					bumpTimer();
				}
			}
		}, 144000);
	}

	public void shortBumpTimer() {
		shortBumpTimer = Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
			public void run() {
				if (getOnlineStaff() > 0 && !bumpIsDone) {
					Bukkit.dispatchCommand(Bukkit.getServer().getConsoleSender(), "mod-broadcast Post On Yogscast/Minecraftforums, Use /Bumpdone When You Are Going To Bump!");
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
	
	public void cancelTimer(){
		Bukkit.getScheduler().cancelTask(amsgId);
		Bukkit.getScheduler().cancelTask(shortBumpTimer);
	}
	
	public boolean getBumpIsDone(){
		return bumpIsDone;
	}
	
	public void bump(){
		Bukkit.getScheduler().cancelTask(shortBumpTimer);
		bumpIsDone = true;
		bumpTimer();
	}
}
