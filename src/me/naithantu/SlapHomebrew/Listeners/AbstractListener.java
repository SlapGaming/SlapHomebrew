package me.naithantu.SlapHomebrew.Listeners;

import me.naithantu.SlapHomebrew.SlapHomebrew;

import org.bukkit.event.Listener;

public abstract class AbstractListener implements Listener {

	protected SlapHomebrew plugin; 
	
	public AbstractListener() {
		plugin = SlapHomebrew.getInstance();
	}

}
