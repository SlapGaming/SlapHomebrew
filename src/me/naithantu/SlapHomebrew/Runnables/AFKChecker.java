package me.naithantu.SlapHomebrew.Runnables;

import me.naithantu.SlapHomebrew.SlapHomebrew;
import me.naithantu.SlapHomebrew.Controllers.AwayFromKeyboard;
import me.naithantu.SlapHomebrew.Controllers.PlayerLogger;
import me.naithantu.SlapHomebrew.Util.Util;

import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class AFKChecker extends BukkitRunnable {
	
	private SlapHomebrew plugin;
	private AwayFromKeyboard afk;
	private PlayerLogger playerLogger;
	private int allowedMinutes;
	private long allowedInactive;
	private long inactiveWarning;
	private long kickedTime;
	
	public AFKChecker(SlapHomebrew plugin, AwayFromKeyboard afk, PlayerLogger playerLogger, int allowedInactiveMinutes) {
		this.plugin = plugin;
		this.afk = afk;
		this.playerLogger = playerLogger;
		this.allowedMinutes = allowedInactiveMinutes;
		this.allowedInactive = (long) allowedMinutes * 60 * 1000;
		this.inactiveWarning = (long) (allowedMinutes - 1) * 60 * 1000;
		this.kickedTime = 45 * 60 * 1000; //45 Minutes before kick
	}
	
	@Override
	public void run() {
		Long systemTime = System.currentTimeMillis();
		for (Player p :plugin.getServer().getOnlinePlayers()) {
			String name = p.getName();
			if (!afk.hasPreventAFK(name)) {
				long lastActive = playerLogger.getLastActivity(name);
				if (!afk.isAfk(name)) {
					if (lastActive != 0) {
						long lastActiveSeconds = systemTime - lastActive;
						if (lastActiveSeconds > allowedInactive) {
							//Go AFK
							afk.goAfk(name, "Inactive for more than " + allowedMinutes + " minutes.");
						} else if (lastActiveSeconds > inactiveWarning && lastActiveSeconds < (inactiveWarning + (15 * 1000))) {
							//Warn for AFK
							Util.badMsg(p, "You will Auto-AFK in 1 minute.");
						}
					}
				} else {
					if ((systemTime - lastActive) > kickedTime) {
						if (Util.testPermission(p, "afk.kick")) {
							p.kickPlayer("AFK for more than 45 minutes.");
						}
					}
				}
			}
		}
	}

}
