package me.naithantu.SlapHomebrew.Controllers;

import me.naithantu.SlapHomebrew.SlapHomebrew;

public abstract class AbstractController {

	protected SlapHomebrew plugin;
	
	/**
	 * Get the SlapHomebrew plugin
	 */
	public AbstractController() {
		plugin = SlapHomebrew.getInstance();
	}
	
	public abstract void shutdown();

}
