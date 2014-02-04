package me.naithantu.SlapHomebrew.Controllers;

import java.util.ArrayList;

import me.naithantu.SlapHomebrew.Controllers.PlayerLogging.*;
import me.naithantu.SlapHomebrew.Storage.YamlStorage;
import me.naithantu.SlapHomebrew.Util.Log;
import me.naithantu.SlapHomebrew.Util.SQLPool;
import me.naithantu.SlapHomebrew.Util.Util;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.PluginManager;
import org.bukkit.scheduler.BukkitTask;

public class SlapSecurityAgency extends AbstractController {

	private BukkitTask batchingTask;
	
	private FileConfiguration config;
	private ArrayList<AbstractLogger> loggers;
	
	public SlapSecurityAgency() {		
		loggers = new ArrayList<>();
		YamlStorage yaml = new YamlStorage(plugin, "playerlogging"); //Get Config
		config = yaml.getConfig();
		if (!config.contains("enabled") || config.getBoolean("enabled") == false) { //Check if enabled
			Log.info("Slap Security Agency - Player Logging is disabled.");
			return;
		}
		
		if (!SQLPool.isSetup()) { //SQL Disabled
			Log.warn("[SSA] No SQL Connection available. SSA is disabled.");
			return;
		}
		
		//Loggers
		if (loggerEnabled("AFK")) 		add(new AFKLogger()); 		//Log AFK Sessions
	//	if (loggerEnabled("Bans")) 		add(new BansLogger()); 		//Log Bans
		if (loggerEnabled("Death"))		add(new DeathLogger());		//Log Deaths & Kills
	//	if (loggerEnabled("Donation")) 	add(new DonationLogger()); 	//Log Donations
		if (loggerEnabled("Kick")) 		add(new KickLogger()); 		//Log Kicks
		if (loggerEnabled("Modreq")) 	add(new ModreqLogger());	//Log Modreqs & their progress
	//	if (loggerEnabled("Notes")) 	add(new NotesLogger()); 	//Log Notes (added by staff)
		if (loggerEnabled("Promotion")) add(new PromotionLogger());	//Log promotions
		if (loggerEnabled("Region")) 	add(new RegionLogger()); 	//Log Region Changes
		if (loggerEnabled("Session")) 	add(new SessionLogger()); 	//Log Login Sessions
		
		//Controllers
		if (controlEnabled("VIPForum")) add(new VipForumControl()); //Control VIP Forum promotions 
		if (controlEnabled("Plot"))		add(new PlotControl());		//Control plot marks
		
		if (loggers.isEmpty()) { //Check if any loggers are enabled
			Log.warn("All loggers are disabled in the config.");
			return;
		}
		
		PluginManager pm = plugin.getServer().getPluginManager();
		for (AbstractLogger logger : loggers) { //Register the Loggers that also listen to events.
			logger.registerEvents(pm);
		}				
		
		startBatchingTask(); //Start batching the loggers
	}
	
	private void add(AbstractLogger logger) {
		if (logger.isEnabled()) {
			loggers.add(logger);
		}
	}
	
	/**
	 * Start batching the Loggers each 2 minutes.
	 */
	private void startBatchingTask() {
		batchingTask = Util.runASyncTimer(plugin, new Runnable() {
			
			private int size = -1;
			private int current = 0;
			
			@Override
			public void run() {
				if (size == -1) size = loggers.size(); //Check if size is given
				loggers.get(current++).batch();
				if (current >= size) {
					current = 0;
				}
			}
		}, 12000, 2400);
	}
	
	/**
	 * Get if a logger is enabled
	 * @param name The name of the logger which will be appended with 'Logger'
	 * @return enabled
	 */
	private boolean loggerEnabled(String name) {
		return config.getBoolean("loggers." + name + "Logger");
	}
	
	/**
	 * Get if a controller is enabled.
	 * @param name The name of the controller which will be appended with 'Control'
	 * @return enabled
	 */
	private boolean controlEnabled(String name) {
		return config.getBoolean("control." + name + "Control");
	}
	
	/**
	 * Shutdown all loggers
	 */
	public void shutdown() {
		if (batchingTask instanceof BukkitTask && batchingTask != null) {
			batchingTask.cancel();
		}
		for (AbstractLogger logger : loggers) {
			logger.shutdown();
		}
	}

}
