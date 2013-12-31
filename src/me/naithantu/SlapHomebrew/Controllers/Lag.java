package me.naithantu.SlapHomebrew.Controllers;

import me.naithantu.SlapHomebrew.Runnables.TPSTask;

public class Lag extends AbstractController {
	
	private TPSTask tpsTask;
	
	public Lag(){
		tpsTask = new TPSTask();
		tpsTask.runTaskTimer(plugin, 0, 20);
	}
	
	public double getTPS(){
		return tpsTask.getTPS();
	}
	
    @Override
    public void shutdown() {
    	//Not needed
    }
}
