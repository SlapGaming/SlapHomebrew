package me.naithantu.SlapHomebrew.Controllers.PlayerLogging;

import nl.stoux.SlapPlayers.Model.Profile;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;

public class AFKLogger extends AbstractLogger {
	
	private static AFKLogger instance;
	
	private HashMap<String, AFKSession> activeSessions;
	private HashSet<Batchable> finishedSessions;
	
	private String query =
			"INSERT INTO `sh_logger_afk` (`user_id`, `went_afk`, `left_afk`, `reason`) " +
			"VALUES (?, ?, ?, ?);";
	
	public AFKLogger() {
		super();
		if (!enabled) return;
		activeSessions = new HashMap<>();
		finishedSessions = new HashSet<>();
		instance = this;
	}
	
	@Override
	public void createTables() throws SQLException {
		//TODO
	}
	
	/**
	 * A player enters an AFK Session
	 * @param UUID The player's UUID
	 * @param reason The reason (can be null)
	 */
	public static void logPlayerGoesAFK(String UUID, String reason) {
		if (instance != null) instance.playerGoesAFK(UUID, reason);
	}
	
	/**
	 * A player enters an AFK Session
	 * @param UUID The player's UUID
	 * @param reason The reason or null
	 */
	private void playerGoesAFK(String UUID, String reason) {
		AFKSession session = new AFKSession(UUID, reason);
		activeSessions.put(UUID, session);
	}
	
	/**
	 * A player leaves their AFK Session
	 * @param UUID The player's UUID
	 */
	public static void logPlayerLeftAFK(String UUID) {
		if (instance != null) instance.playerLeftAFK(UUID);
	}

	/**
	 * A player leaves their AFK Session
	 * @param UUID The player's UUID
	 */
	private void playerLeftAFK(String UUID) {
		AFKSession session = activeSessions.get(UUID); //Get the session
		if (session == null) return; //Shouldn't be called
		session.leftAFK(); //Time Leave AFK
		activeSessions.remove(UUID); //Remove from active sessions
		finishedSessions.add(session); //Add to finished sessions
		
		if (finishedSessions.size() >= 20 && plugin.isEnabled()) {
			batch();
		}
	}
	
	/**
	 * Get the AFK time for a player
	 * Should be called in A-Sync
	 * @param profile The player's UUIDProfile
	 * @return time afk, or -1 if failed
	 */
	public static long getAFKTime(final Profile profile) {
		if (instance == null) { //Check if initialzed
			return -1L;
		}
		long afkTime = 0;
        int userID = profile.getID();
        String UUID = profile.getUUIDString();
		
		for (Batchable batchable : instance.finishedSessions) { //Get from unbatched
			AFKSession session = (AFKSession) batchable;
            if (session.userID == userID) { //Check if session is about player
				afkTime += (session.leftAFK - session.wentAFK); //Add to time
			}
		}
		Connection con = instance.plugin.getSQLPool().getConnection(); //Get a connection
		try {
			PreparedStatement prep = con.prepareStatement( //Get from DB
				"SELECT SUM(`left_afk`) - SUM(`went_afk`) FROM `sh_logger_afk` WHERE `user_id` = ?;"
			); 
			prep.setInt(1, userID);
			ResultSet afkRS = prep.executeQuery(); //Execute
			if (afkRS.next()) { //If given
				afkTime += afkRS.getLong(1); //Add to time
			}
			if (instance.activeSessions.containsKey(UUID)) { //Check if currently AFK
				afkTime += (System.currentTimeMillis() - instance.activeSessions.get(UUID).wentAFK);
			}
		} catch (SQLException e) {
			e.printStackTrace();
			afkTime = -1;
		} finally {
            instance.plugin.getSQLPool().returnConnection(con); //Return con
		}
		return afkTime;
	}
			
	@Override
	public void shutdown() {
		batch();
		instance = null;
	}
	
	@Override
	public void batch() {
		batch(query, finishedSessions);
	}
	
	private class AFKSession implements Batchable {

        int userID;
		String UUID;
		long wentAFK;
		long leftAFK;
		String reason;
		
		public AFKSession(String UUID, String reason) {
            this.userID = -1;
			this.UUID = UUID;
			this.reason = reason;
			wentAFK = System.currentTimeMillis();
		}
		
		public void leftAFK() {
			leftAFK = System.currentTimeMillis();
		}	
		
		@Override
		public void addBatch(PreparedStatement preparedStatement) throws SQLException {
			preparedStatement.setInt(1, userID);
			preparedStatement.setLong(2, wentAFK);
			preparedStatement.setLong(3, leftAFK);
			preparedStatement.setString(4, reason);
		}

        @Override
        public boolean isBatchable() {
            userID = getUserID(UUID);
            return (userID != -1);
        }
    }
	
}
