package me.naithantu.SlapHomebrew.Runnables;

import org.bukkit.scheduler.BukkitRunnable;

public class TPSTask extends BukkitRunnable {
	private double tps = 0;
	long prevTime;
	long elapsedTime;
	double elapsedTimeSec;
	
	public TPSTask(){
		prevTime = System.currentTimeMillis();
	}
	
	public double getTPS(){
		return tps;
	}

	@Override
	public void run() {
		long time = System.currentTimeMillis();
		elapsedTime = time - prevTime;
		elapsedTimeSec = (double) elapsedTime / 1000;
		tps = 20 / elapsedTimeSec;
		prevTime = time;
	}
}
