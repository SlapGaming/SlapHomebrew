package me.naithantu.SlapHomebrew.Runnables;

import me.naithantu.SlapHomebrew.SlapHomebrew;
import me.naithantu.SlapHomebrew.Controllers.AwayFromKeyboard;
import me.naithantu.SlapHomebrew.Controllers.PlayerLogger;

import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class AFKChecker extends BukkitRunnable {
	
	private SlapHomebrew plugin;
	private AwayFromKeyboard afk;
	private PlayerLogger playerLogger;
	private int allowedMinutes;
	private long allowedInactive;
	
	public AFKChecker(SlapHomebrew plugin, AwayFromKeyboard afk, PlayerLogger playerLogger, int allowedInactiveMinutes) {
		this.plugin = plugin;
		this.afk = afk;
		this.playerLogger = playerLogger;
		this.allowedMinutes = allowedInactiveMinutes;
		this.allowedInactive = (long) allowedInactiveMinutes * 60 * 1000;
	}
	
	@Override
	public void run() {
		Long systemTime = System.currentTimeMillis();
		for (Player p :plugin.getServer().getOnlinePlayers()) {
			String name = p.getName();
			if (!afk.hasPreventAFK(name)) {
				if (!afk.isAfk(name)) {
					long lastActive = playerLogger.getLastActivity(name);
					if (lastActive != 0) {
						if ((systemTime - lastActive) > allowedInactive) {
							//Go AFK
							afk.goAfk(name, "Inactive for more than " + allowedMinutes + " minutes.");
						}
					}
				}
			}
		}
	}

}
