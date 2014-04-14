package me.naithantu.SlapHomebrew.Listeners;

import me.naithantu.SlapHomebrew.SlapHomebrew;

import org.bukkit.event.Listener;

public abstract class AbstractListener implements Listener {

	protected SlapHomebrew plugin; 
	
	public AbstractListener() {
		plugin = SlapHomebrew.getInstance();
	}

	/**
	 * Disable any functionalties in this listener
	 * This does NOT unregister it
	 * 
	 * Standard implementation is nothing
	 */
	public void disable() {}
	
}
