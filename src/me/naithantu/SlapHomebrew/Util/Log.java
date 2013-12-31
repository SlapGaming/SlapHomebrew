package me.naithantu.SlapHomebrew.Util;

import java.util.logging.Logger;

/**
 * Static Util Logging Class
 *
 */
public class Log {

	private static Logger log;
	
	/**
	 * Initialize the logger
	 * @param logger
	 */
	public static void intialize(Logger logger) {
		log = logger;
	}
	
	public static void info(String msg) {
		log.info(msg);
	}
	
	public static void warn(String msg) {
		log.warning(msg);
	}
	
	public static void severe(String msg) {
		log.severe(msg);
	}
	
	public static void fine(String msg) {
		log.fine(msg);
	}
	
	public static void finer(String msg) {
		log.finer(msg);
	}
	
	public static void finest(String msg) {
		log.finest(msg);
	}
	
	/**
	 * Remove any static references
	 */
	public static void shutdown() {
		log = null;
	}
	
	
	
	

}
