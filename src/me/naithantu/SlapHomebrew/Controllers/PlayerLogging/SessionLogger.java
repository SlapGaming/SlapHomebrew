package me.naithantu.SlapHomebrew.Controllers.PlayerLogging;

import java.net.InetSocketAddress;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;

import me.naithantu.SlapHomebrew.Commands.AbstractCommand;
import me.naithantu.SlapHomebrew.Commands.Exception.CommandException;
import me.naithantu.SlapHomebrew.Util.SQLPool;
import me.naithantu.SlapHomebrew.Util.Util;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class SessionLogger extends AbstractLogger implements Listener {

	private static SessionLogger instance;
	
	private String sqlQuery = 
			"INSERT INTO `mcecon`.`logger_times` (`player`, `join_time`, `quit_time`, `ip`, `port`, `first_time`) " +
			"VALUES (?, ?, ?, ?, ?, ?);";
	
	private HashMap<String, Session> activeSessions; // Playername -> Session
	private HashSet<Batchable> finishedSessions;
	
	private SimpleDateFormat format;
	
	public SessionLogger() {
		super();
		if (!enabled) return;
		activeSessions = new HashMap<>();
		finishedSessions = new HashSet<>();
		format = new SimpleDateFormat("dd/MM/yyyy"); //Create format;
		instance = this;
	}
	
	/*
	 *************
	 * Log stuff *
	 *************
	 */
	
	@Override
	public void createTables() throws SQLException {
		executeUpdate(
			"CREATE TABLE IF NOT EXISTS `logger_times` ( " +
			"`player` varchar(20) NOT NULL, " +
			"`join_time` bigint(20) NOT NULL, " +
			"`quit_time` bigint(20) NOT NULL, " +
			"`ip` varchar(50) NOT NULL, `port` int(11) NOT NULL, " +
			"`first_time` tinyint(1) NOT NULL DEFAULT '0', " +
			"PRIMARY KEY (`player`,`join_time`,`quit_time`), " +
			"KEY `ip` (`ip`) " +
			") ENGINE=InnoDB DEFAULT CHARSET=latin1;"
		);
	}
	
	@EventHandler
	public void login(PlayerJoinEvent event) {
		Player joinedPlayer = event.getPlayer(); //Get player
		String playername = joinedPlayer.getName(); //Get name
		InetSocketAddress adress = joinedPlayer.getAddress(); //Get adress
		activeSessions.put(playername, new Session(playername, adress.getHostString(), adress.getPort(), !joinedPlayer.hasPlayedBefore())); //Insert into active map
	}
	
	@EventHandler
	public void logout(PlayerQuitEvent event) {
		String playername = event.getPlayer().getName(); //Get player
		Session s = activeSessions.get(playername); //Get session
		if (s == null) return; //Shouldn't be called
		s.setQuitTime(); //Set quit time
		activeSessions.remove(playername); //Remove from map
		finishedSessions.add(s); //Add to set
		if (finishedSessions.size() >= 10 && plugin.isEnabled()) {
			batch();
		}
	}
	
	
	@Override
	public void batch() {
		batch(sqlQuery, finishedSessions);
	}
		
	@Override
	public void shutdown() {
		batch();
		instance = null;
	}
	
	private class Session implements Batchable {
		
		String player;
		long join;
		long quit;
		String ip;
		int port;
		boolean firstTime;
		
		public Session(String player, String ip, int port, boolean firstTime) {
			this.player = player;
			this.join = System.currentTimeMillis();
			this.ip = ip;
			this.port = port;
			this.firstTime = firstTime;
			quit = -1;
		}
		
		public void setQuitTime() {
			this.quit = System.currentTimeMillis();
		}
		
		@Override
		public void addBatch(PreparedStatement preparedStatement) throws SQLException {
			preparedStatement.setString(1, player);
			preparedStatement.setLong(2, join);
			preparedStatement.setLong(3, quit);
			preparedStatement.setString(4, ip);
			preparedStatement.setInt(5, port);
			preparedStatement.setBoolean(6, firstTime);
		}
		
	}
	
	/*
	 ************************
	 * Calculations/Getters *
	 ************************
	 */
	
	/**
	 * Get Format dd/MM/yyyy
	 * @return get format
	 */
	public SimpleDateFormat getFormat() {
		return format;
	}
	
	/**
	 * Get instance
	 * @param p The player getting the instance
	 * @return the instance
	 * @throws CommandException if no instance available
	 */
	public static SessionLogger getInstance(Player p) throws CommandException {
		if (instance == null) {
			AbstractCommand.removeDoingCommand(p);
			throw new CommandException("The SessionLogger is currently disabled.");
		}
		return instance;
	}
	
	/**
	 * Get instance
	 * @return The instance
	 * @throws CommandException if no instance available
	 */
	public static SessionLogger getInstance() throws CommandException {
		if (instance == null) {
			throw new CommandException("The SessionLogger is currently disabled.");
		}
		return instance;
	}
	
	/**
	 * Send the playtime to a player
	 * @param p The player
	 * @throws CommandException
	 */
	public static void sendPlayerTime(final Player p) throws CommandException {
		getInstance(p);
		
		String playername = p.getName();
		long playtime = 0L;
		
		for (Batchable batchable : instance.finishedSessions) { //Loop thru unbatched sessions
			Session finishedSession = (Session) batchable;
			if (finishedSession.player.equalsIgnoreCase(playername)) { //If current player add time
				playtime += finishedSession.quit - finishedSession.join;
			}
		}
		
		Connection con = SQLPool.getConnection();
		try {
			PreparedStatement prep = con.prepareStatement( //Get Total time from DB
				"SELECT SUM( `quit_time` ) - SUM( `join_time` ) AS `playtime` FROM `logger_times` WHERE `player` = ?;"
			);
			prep.setString(1, playername);
			ResultSet timeRS = prep.executeQuery();
			if (timeRS.next()) { //If time given
				playtime += timeRS.getLong(1);
			}
			
			//Get AFK time
			long afk = AFKLogger.getAFKTime(playername);
			
			if (instance.activeSessions.containsKey(playername)) { //Add current online time
				playtime += (System.currentTimeMillis() - instance.activeSessions.get(playername).join);
			}
			
			//Send messages
			Util.msg(p, "Onlinetime: " + Util.getTimePlayedString(playtime) + ".");
			if (afk > 0) {
				p.sendMessage(ChatColor.GOLD + "  \u2517\u25B6 " + ChatColor.GRAY + "of which AFK: " + Util.getTimePlayedString(afk));
			}			
		} catch (SQLException e) {
			e.printStackTrace();
			Util.badMsg(p, "Woops! Something went wrong.");
		} finally {
			SQLPool.returnConnection(con);
			AbstractCommand.removeDoingCommand(p);
		}
	}
	
	/**
	 * Get HashMap with the number of entries specified
	 * @param fromDate From date, can be null
	 * @param toDate To date, can be null
	 * @param entries Number of players that should be gathered
	 * @return Sorted arraylist with leaderboard entries
	 */
	public ArrayList<LeaderboardEntry> getLeaderboard(Date fromDate, Date toDate, int entries) {
		int addEntries = entries + 5; //Add 5 entries, just in case
		long[] times = parseDates(fromDate, toDate); //Parse the dates
		HashMap<String, Long> map = getPlayedTimes(times[0], times[1], addEntries); //Get playedtimes
		ArrayList<LeaderboardEntry> lb = createSortLeaderboardEntries(map); //Create & Sort the leaderboard
		int lbSize = lb.size(); //Get size
		while (entries < lbSize) { //Remove any entries over given Entries
			lb.remove(entries);
			lbSize--;
		}
		return lb;
	}	
	
	/**
	 * Get the played times for multiple players
	 * @param players The players to be checked
	 * @param fromDate From date, can be null
	 * @param toDate To date, can be null
	 * @return Map with all players and their play times, possible that a player isn't in the map.
	 */
	public HashMap<String, Long> getPlayedTimes(String[] players, Date fromDate, Date toDate) {
		long[] times = parseDates(fromDate, toDate);
		return getPlayedTimes(times[0], times[1], 0, players);
	}
	
	/**
	 * Get played times (between two dates)
	 * @param player The player 
	 * @param fromDate From date, if null = since start of this system.
	 * @param toDate Till date, if null = till now.
	 * @return time of that player, or 0 if not played
	 */
	public long getPlayedTime(String player, Date fromDate, Date toDate) {
		long[] times = parseDates(fromDate, toDate); //Parse times
		HashMap<String, Long> map = getPlayedTimes(times[0], times[1], 0, player); //Get map
		if (map.isEmpty()) {
			return 0;
		} else {
			return map.get(player.toLowerCase());
		}
	}
	
	/**
	 * Parse the dates
	 * @param fromDate From date, can be null
	 * @param toDate To Date, can be null
	 * @return array with [0] = FromTime & [1] = ToTime
	 */
	private long[] parseDates(Date fromDate, Date toDate) {
		long fromTime = 0L;
		long toTime = Long.MAX_VALUE;
		if (fromDate != null) { //Parse from date if given
			fromTime = fromDate.getTime();
		}
		if (toDate != null) { //Parse to date if given
			toTime = toDate.getTime() + (24 * 60 * 60 * 1000) - 1;
		}
		return new long[]{fromTime, toTime}; //Return times array
	}
	
	
	
	/**
	 * Get Played Time between two dates
	 * @param fromTime Time in milliseconds
	 * @param toTime Time in milliseconds
	 * @param limit Limit the number of results (0 or lower = Infinite)
	 * @param players All the players that should be searched
	 * @return HashMap with all played times Key:[Player] => Value:[TimePlayed]
	 */
	private HashMap<String, Long> getPlayedTimes(long fromTime, long toTime, int limit, String... players) {
		batch(sqlQuery, finishedSessions, true); //Batch in sync with this thread
		
		Connection con = SQLPool.getConnection();
		
		HashMap<String, Long> timesMap = new HashMap<>();
		if (limit < 1) { //No limit
			limit = Integer.MAX_VALUE;
		}
		
		String playersSQL = "";
		boolean checkingPlayers;
		if (checkingPlayers = (players.length > 0)) { //If players specified
			playersSQL = " AND (";
			boolean first = true;
			for (int x = 0; x < players.length; x++) { //Loop thru number of players
				if (!first) {
					playersSQL += " OR "; //Add or statement
				}
				playersSQL += "`player` = ?"; //Add player
				first = false;
			}
			playersSQL += ") ";
		}
		
		
		try {
			PreparedStatement prep = con.prepareStatement( //Create Prep'd Statement
				"SELECT " +
					"`player`, " +
					"SUM( IF(`quit_time` > ?, ?, `quit_time`) - IF(`join_time` < ?, ?, `join_time`) ) as `online_time` " +
				"FROM `logger_times` " +
				"WHERE (" +
						"(`join_time` > ? AND `quit_time` < ?)" +
							" OR " +
						"(`join_time` < ? AND `quit_time` > ? AND `quit_time` < ?)" +
							" OR " +
						"(`quit_time` > ? AND `join_time` > ? AND `join_time` < ?) " +
					") " + 
					playersSQL + 
				"GROUP BY `player` " +
				"ORDER BY `online_time` DESC " +
				"LIMIT 0, ?;"
			);
						
			//SUM => IF
			prep.setLong(1, toTime);
			prep.setLong(2, toTime);
			prep.setLong(3, fromTime);
			prep.setLong(4, fromTime);
			
			//WHERE
			prep.setLong(5, fromTime); //Both between specified dates 
			prep.setLong(6, toTime);
			prep.setLong(7, fromTime); //Join time before the FromDate, Quit time between From & To
			prep.setLong(8, fromTime);
			prep.setLong(9, toTime);
			prep.setLong(10, toTime); //Quit after ToDate, Join between From & To
			prep.setLong(11, fromTime);
			prep.setLong(12, toTime);
			
			//Players
			int x = 13;
			for (String player : players) { //Set players if there are any
				prep.setString(x++, player);
			}
			
			//Limit
			prep.setLong(x, limit);
			
			ResultSet rs = prep.executeQuery(); //Execute			
			while (rs.next()) { //Loop thru results
				timesMap.put(
					rs.getString(1).toLowerCase(), //Get naam
					rs.getLong(2) //Get playedtime
				);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			SQLPool.returnConnection(con);
		}
		
		long quit = System.currentTimeMillis();
		for (Session session : new HashMap<String, Session>(activeSessions).values()) { //Look thru active sessions
			if (checkingPlayers) { //If checking for certain players
				boolean found = false;
				for (String player : players) { //Loop thru given players
					if (player.equalsIgnoreCase(session.player)) { 
						found = true; //If player found, break this loop
						break;
					}
				}
				if (!found) continue; //If not found, skip this session
			}
			long join = session.join;
			if (join > toTime) continue; //Joined after toTime, skip
			
			if ((join > fromTime && join < toTime) || (quit > fromTime && quit < toTime) ) { //If Join or Quit time in the TimeFrame
				long played = (quit > toTime ? toTime : quit) - (join < fromTime ? fromTime : join); //Calculate played time
				String plc = session.player.toLowerCase(); //to LowerCase
				if (timesMap.containsKey(plc)) { //If already in map
					played += timesMap.get(plc); //Add to time
				}
				timesMap.put(session.player.toLowerCase(), played); //Put combined time in map
			}
		}
		return timesMap;
	}
	
	public class LeaderboardEntry implements Comparable<LeaderboardEntry> {
		
		private String playernameLC;
		private long playtime;
		
		public LeaderboardEntry(String playernameLC, long playtime) {
			this.playernameLC = playernameLC;
			this.playtime = playtime;
		}
		
		/**
		 * Get the player's name in LowerCase
		 * @return the name
		 */
		public String getPlayernameLC() {
			return playernameLC;
		}
		
		/**
		 * Get the player's playedtime
		 * @return The playedtime
		 */
		public long getPlaytime() {
			return playtime;
		}
				
		@Override
		public int compareTo(LeaderboardEntry o) {
			return (int) (playtime - o.playtime);
		}
		
	}
	
	/**
	 * Parse a HashMap into leaderboardEntries, and sort the leaderboard in the Desc order.
	 * @param playedTimes The played times
	 * @return Sorted Array
	 */
	public ArrayList<LeaderboardEntry> createSortLeaderboardEntries(HashMap<String, Long> playedTimes) {
		ArrayList<LeaderboardEntry> entries = new ArrayList<>();
		for (Entry<String, Long> entry : playedTimes.entrySet()) {
			entries.add(
				new LeaderboardEntry(
					entry.getKey(),
					entry.getValue()
				)
			);
		}
		Collections.sort(entries);
		return entries;
	}
	
	
	
	

}
