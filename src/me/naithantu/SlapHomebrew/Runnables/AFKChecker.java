package me.naithantu.SlapHomebrew.Runnables;

import me.naithantu.SlapHomebrew.Controllers.AwayFromKeyboard;
import me.naithantu.SlapHomebrew.PlayerExtension.PlayerControl;
import me.naithantu.SlapHomebrew.SlapHomebrew;
import me.naithantu.SlapHomebrew.Util.Util;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class AFKChecker extends BukkitRunnable {
	
	private SlapHomebrew plugin;
	private AwayFromKeyboard afk;
	private int allowedMinutes;
	private long allowedInactive;
	private long inactiveWarning;
	private long kickedTime;
	
	public AFKChecker(SlapHomebrew plugin, AwayFromKeyboard afk, int allowedInactiveMinutes) {
		this.plugin = plugin;
		this.afk = afk;
		this.allowedMinutes = allowedInactiveMinutes;
		this.allowedInactive = (long) allowedMinutes * 60 * 1000;
		this.inactiveWarning = (long) (allowedMinutes - 1) * 60 * 1000;
		this.kickedTime = 45 * 60 * 1000; //45 Minutes before kick
	}
	
	@Override
	public void run() {
		Long systemTime = System.currentTimeMillis();
		for (Player p : plugin.getServer().getOnlinePlayers()) {
			if (!afk.hasPreventAFK(p)) {
				long lastActive = PlayerControl.getPlayer(p).getLastActivity();
				if (!afk.isAfk(p)) {
					if (lastActive != 0) {
						long lastActiveSeconds = systemTime - lastActive;
						if (lastActiveSeconds > allowedInactive) {
							//Go AFK
							afk.goAfk(p, "Inactive for more than " + allowedMinutes + " minutes.");
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
