package me.naithantu.SlapHomebrew.Controllers.PlayerLogging;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;

public class AFKLogger extends AbstractLogger {
	
	private static AFKLogger instance;
	
	private HashMap<String, AFKSession> activeSessions;
	private HashSet<Batchable> finishedSessions;
	
	private String query = 
			"INSERT INTO `mcecon`.`logger_afk` (`player`, `went_afk`, `left_afk`, `reason`) " +
			"VALUES (?, ?, ?, ?);";
	
	public AFKLogger(LoggerSQL sql) {
		super(sql);
		if (!enabled) return;
		activeSessions = new HashMap<>();
		finishedSessions = new HashSet<>();
		instance = this;
	}
	
	@Override
	public void createTables() throws SQLException {
		executeUpdate(
			"CREATE TABLE IF NOT EXISTS `logger_afk` ( " +
			"`player` varchar(50) NOT NULL, " +
			"`went_afk` bigint(20) NOT NULL, " +
			"`left_afk` bigint(20) NOT NULL, " +
			"`reason` varchar(255) DEFAULT NULL, " +
			"KEY `player` (`player`) " +
			") ENGINE=InnoDB DEFAULT CHARSET=latin1;"
		);
	}
	
	/**
	 * A player enters an AFK Session
	 * @param player The player
	 * @param reason The reason (can be null)
	 */
	public static void logPlayerGoesAFK(String player, String reason) {
		if (instance != null) instance.playerGoesAFK(player, reason);		
	}
	
	/**
	 * A player enters an AFK Session
	 * @param player The player
	 * @param reason The reason (can be null)
	 */
	private void playerGoesAFK(String player, String reason) {
		AFKSession session = new AFKSession(player, reason);
		activeSessions.put(player, session);		
	}
	
	/**
	 * A player leaves their AFK Session
	 * @param player The player
	 */
	public static void logPlayerLeftAFK(String player) {
		if (instance != null) instance.playerLeftAFK(player);
	}
	
	/**
	 * A player leaves their AFK Session
	 * @param player The player
	 */
	private void playerLeftAFK(String player) {
		AFKSession session = activeSessions.get(player); //Get the session
		if (session == null) return; //Shouldn't be called
		session.leftAFK(); //Time Leave AFK
		activeSessions.remove(player); //Remove from active sessions
		finishedSessions.add(session); //Add to finished sessions
		
		if (finishedSessions.size() >= 20 && plugin.isEnabled()) {
			batch(query, finishedSessions);
		}
	}
			
	@Override
	public void shutdown() {
		batch(query, finishedSessions);
		instance = null;
	}
	
	private class AFKSession implements Batchable {
		
		String player;
		long wentAFK;
		long leftAFK;
		String reason;
		
		public AFKSession(String player, String reason) {
			this.player = player;
			this.reason = reason;
			wentAFK = System.currentTimeMillis();
		}
		
		public void leftAFK() {
			leftAFK = System.currentTimeMillis();
		}	
		
		@Override
		public void addBatch(PreparedStatement preparedStatement) throws SQLException {
			preparedStatement.setString(1, player);
			preparedStatement.setLong(2, wentAFK);
			preparedStatement.setLong(3, leftAFK);
			preparedStatement.setString(4, reason);
		}
	}
	
}
