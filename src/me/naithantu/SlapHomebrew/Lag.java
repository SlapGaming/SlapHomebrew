package me.naithantu.SlapHomebrew;

import me.naithantu.SlapHomebrew.Runnables.TPSTask;

public class Lag {
	TPSTask tpsTask;
	
	public Lag(SlapHomebrew plugin){
		tpsTask = new TPSTask();
		tpsTask.runTaskTimer(plugin, 0, 20);
	}
	
	public double getTPS(){
		return tpsTask.getTPS();
	}
}
