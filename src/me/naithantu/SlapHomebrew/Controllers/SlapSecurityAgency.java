package me.naithantu.SlapHomebrew.Controllers;

import java.util.ArrayList;

import me.naithantu.SlapHomebrew.Controllers.PlayerLogging.*;
import me.naithantu.SlapHomebrew.Storage.YamlStorage;
import me.naithantu.SlapHomebrew.Util.Log;
import me.naithantu.SlapHomebrew.Util.Util;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.PluginManager;
import org.bukkit.scheduler.BukkitTask;

public class SlapSecurityAgency extends AbstractController {

	private BukkitTask batchingTask;
	
	private LoggerSQL sql;
	
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
		
		sql = new LoggerSQL();
		if (!sql.connect()) { //Connect to SQL
			return;
		}
		
		//Loggers
		if (loggerEnabled("AFK")) 		add(new AFKLogger(sql)); 		//Log AFK Sessions
	//	if (loggerEnabled("Bans")) 		add(new BansLogger(sql)); 		//Log Bans
		if (loggerEnabled("Death"))		add(new DeathLogger(sql));		//Log Deaths & Kills
	//	if (loggerEnabled("Donation")) 	add(new DonationLogger(sql)); 	//Log Donations
		if (loggerEnabled("Kick")) 		add(new KickLogger(sql)); 		//Log Kicks
		if (loggerEnabled("Modreq")) 	add(new ModreqLogger(sql));		//Log Modreqs & their progress
	//	if (loggerEnabled("Notes")) 	add(new NotesLogger(sql)); 		//Log Notes (added by staff)
		if (loggerEnabled("Region")) 	add(new RegionLogger(sql)); 	//Log Region Changes
		if (loggerEnabled("Session")) 	add(new SessionLogger(sql)); 	//Log Login Sessions
		
		//Controllers
	//	if (controlEnabled("VIPDays")) 
	//	if (controlEnabled("VIPForum")) 
	//	if (controlEnabled("PlotMark"))
		
		if (loggers.isEmpty()) { //Check if any loggers are enabled
			Log.warn("All loggers are disabled in the config.");
			sql.disconnect();
			sql = null;
			return;
		}
		
		PluginManager pm = plugin.getServer().getPluginManager();
		for (AbstractLogger logger : loggers) { //Register the Loggers that also listen to events.
			logger.registerEvents(pm);
		}				
		
		sql.startPinging(); //Start pinging the SQL Server
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
		return config.getBoolean("control" + name + "Control");
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
		if (sql != null) sql.disconnect();
	}

}
