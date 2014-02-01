package me.naithantu.SlapHomebrew.Controllers.PlayerLogging;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;

import com.mcbans.firestar.mcbans.MCBans;

public class KickLogger extends AbstractLogger implements Listener {

	private static KickLogger instance;
	
	private HashSet<Batchable> kicks;
	private HashMap<String, String> kickedByMap;
	
	private String sqlQuery = 
			"INSERT INTO `mcecon`.`logger_kicks` (`player`, `kicked_time`, `kicked_by`, `reason`) " +
			"VALUES (?, ?, ?, ?);";
	
	public KickLogger() {
		super();
		if (!enabled) return;
		kicks = new HashSet<>();
		kickedByMap = new HashMap<>();
		instance = this;
	}
	
	@Override
	public void registerEvents(PluginManager pm) {
		super.registerEvents(pm);
		initialzeMCBansLogger();
	}
	
	
	/**
	 * Check if this server has the plugin MCBans
	 * if the server has MCBans, register MCBans events listener
	 */
	private void initialzeMCBansLogger() {
		PluginManager pm = plugin.getServer().getPluginManager();
		Plugin mcBans = pm.getPlugin("MCBans");
		if (mcBans != null && mcBans.isEnabled() && mcBans instanceof MCBans) {
			pm.registerEvents(new MCBansListener(), plugin);
		}		
	}
	
	@Override
	public void createTables() throws SQLException {
		executeUpdate(
			"CREATE TABLE IF NOT EXISTS `logger_kicks` ( " +
			"`player` varchar(20) NOT NULL, " +
			"`kicked_time` bigint(20) NOT NULL, " +
			"`kicked_by` varchar(20) DEFAULT NULL, " +
			"`reason` varchar(1000) DEFAULT NULL, " +
			"KEY `player` (`player`,`kicked_time`,`kicked_by`) " +
			") ENGINE=InnoDB DEFAULT CHARSET=latin1;"
		);
	}
		
	@EventHandler(priority=EventPriority.MONITOR)
	public void onKick(PlayerKickEvent event) {
		if (!event.isCancelled()) {
			String player = event.getPlayer().getName(); //Get the kicked player
			String by = null;
			if (kickedByMap.containsKey(player)) { //If kicked by a person thru a different command (like /skick)
				by = kickedByMap.get(player); //Get from map
				kickedByMap.remove(player); //And remove from map
			}
			addKick(new PlayerKicked(event.getPlayer().getName(), System.currentTimeMillis(), by, event.getReason())); //Add the kick
		}
	}
		
	/**
	 * Add a player kick to the list of kicks
	 * @param kick The kick
	 */
	private void addKick(PlayerKicked kick) {
		kicks.add(kick);
		if (kicks.size() >= 5 && plugin.isEnabled()) {
			batch();
		}
	}
	
	/**
	 * Add a kicked by player to the map (will be caught/used in the KickEvent
	 * @param player The player that is kicked
	 * @param kickedByPlayer The player that kicked the other
	 */
	public void addPlayerKickedBy(String player, String kickedByPlayer) {
		kickedByMap.put(player, kickedByPlayer);
	}
	
	/**
	 * Log a kicked by player.
	 * In example: Log who kicked a player using /skick
	 * @param player The player that is kicked
	 * @param kickedByPlayer The player that kicked the other
	 */
	public static void logPlayerKickedBy(String player, String kickedByPlayer) {
		if (instance != null) instance.addPlayerKickedBy(player, kickedByPlayer);
	}
	
	@Override
	public void batch() {
		batch(sqlQuery, kicks);
	}
	
	@Override
	public void shutdown() {
		batch();
		instance = null;
	}
	
	private class PlayerKicked implements Batchable {
		
		String player;
		long kickedTime;
		String kickedBy;
		String kickReason;
		
		public PlayerKicked(String player, long kickedTime, String kickedBy, String kickReason) {
			super();
			this.player = player;
			this.kickedTime = kickedTime;
			this.kickedBy = kickedBy;
			this.kickReason = kickReason;
		}

		@Override
		public void addBatch(PreparedStatement preparedStatement) throws SQLException {
			preparedStatement.setString(1, player);
			preparedStatement.setLong(2, kickedTime);
			preparedStatement.setString(3, kickedBy);
			preparedStatement.setString(4, kickReason);
		}
	}
	
	private class MCBansListener implements Listener {
		
		@EventHandler(priority=EventPriority.MONITOR)
		public void onKick(com.mcbans.firestar.mcbans.events.PlayerKickEvent event) {
			if (!event.isCancelled()) {
				addPlayerKickedBy(event.getPlayer(), event.getSender());
			}
		}
		
	}

}
