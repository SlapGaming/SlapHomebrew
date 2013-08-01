package me.naithantu.SlapHomebrew.Runnables;

import me.naithantu.SlapHomebrew.Jails;

import org.bukkit.scheduler.BukkitRunnable;

public class JailChecker extends BukkitRunnable {

	private Jails jails;
	
	
	public JailChecker(Jails jails) {
		this.jails = jails;
	}
	
	@Override
	public void run() {
		if (jails.onlinePlayersJailed()) {
			long currentTime = System.currentTimeMillis();
			jails.checkForRelease(currentTime);
		}
	}
	
}
