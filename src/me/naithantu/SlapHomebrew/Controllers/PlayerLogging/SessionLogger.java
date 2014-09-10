package me.naithantu.SlapHomebrew.Controllers.PlayerLogging;

import java.net.InetSocketAddress;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;

import me.naithantu.SlapHomebrew.Commands.AbstractCommand;
import me.naithantu.SlapHomebrew.Commands.Exception.CommandException;
import me.naithantu.SlapHomebrew.PlayerExtension.UUIDControl;
import me.naithantu.SlapHomebrew.Util.DateUtil;
import me.naithantu.SlapHomebrew.Util.SQLPool;
import me.naithantu.SlapHomebrew.Util.Util;

import mkremins.fanciful.FancyMessage;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class SessionLogger extends AbstractLogger implements Listener {

	private static SessionLogger instance;
	
	private String sqlQuery = 
			"INSERT INTO `sh_logger_times` (`user_id`, `join_time`, `quit_time`, `ip`, `port`, `first_time`) " +
			"VALUES (?, ?, ?, ?, ?, ?);";
	
	private HashMap<String, Session> activeSessions; // Playername -> Session
	private HashSet<Batchable> finishedSessions;
	
	public SessionLogger() {
		super();
		if (!enabled) return;
		activeSessions = new HashMap<>();
		finishedSessions = new HashSet<>();
		instance = this;
	}
	
	/*
	 *************
	 * Log stuff *
	 *************
	 */
	
	@Override
	public void createTables() throws SQLException {

	}
	
	@EventHandler
	public void login(PlayerJoinEvent event) {
		Player joinedPlayer = event.getPlayer(); //Get player
		String UUID = joinedPlayer.getUniqueId().toString(); //Get name
		InetSocketAddress address = joinedPlayer.getAddress(); //Get adress
		activeSessions.put(UUID, new Session(UUID, address.getHostString(), address.getPort(), !joinedPlayer.hasPlayedBefore())); //Insert into active map
	}
	
	@EventHandler
	public void logout(PlayerQuitEvent event) {
		String UUID = event.getPlayer().getUniqueId().toString(); //Get player
		Session s = activeSessions.get(UUID); //Get session
		if (s == null) return; //Shouldn't be called
		s.setQuitTime(); //Set quit time
		activeSessions.remove(UUID); //Remove from map
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

    /**
     * Get a list of sessions from a player
     * @param userID The ID of the player
     * @param logins Output the login times
     * @param ips Output the IP
     * @return List of sessions
     * @throws CommandException if an error occurred
     */
    public static ArrayList<Profilable> getSessions(int userID, boolean logins, boolean ips) throws CommandException {
        //Create the new list
        ArrayList<Profilable> sessions = new ArrayList<>();

        //Get the sessions
        Connection con = SQLPool.getConnection();
        try {
            //Prepare a statement
            PreparedStatement prep = con.prepareStatement("SELECT `join_time`, `quit_time`, `ip` FROM `sh_logger_times` WHERE `user_id` = ?;");
            prep.setInt(1, userID);

            //Get results
            ResultSet rs = prep.executeQuery();
            while (rs.next()) {
                //Get data
                long join = rs.getLong(1);
                long quit = rs.getLong(2);
                String ip = rs.getString(3);

                //Add Session
                sessions.add(instance.new Session(userID, join, quit, ip, logins, ips));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new CommandException("An error occurred! Notify Stoux!");
        } finally {
            SQLPool.returnConnection(con);
        }


        //Return the list
        return sessions;
    }
	
	private class Session extends Profilable implements Batchable {

        int userID;
		String UUID;
		long join;
		long quit;
		String ip;
		int port;
		boolean firstTime;

        //Profilable data
        boolean logins;
        boolean ips;
		
		public Session(String UUID, String ip, int port, boolean firstTime) {
			this.UUID = UUID;
			this.join = System.currentTimeMillis();
			this.ip = ip;
			this.port = port;
			this.firstTime = firstTime;
			quit = -1;
		}

        private Session(int userID, long join, long quit, String ip, boolean logins, boolean ips) {
            this.userID = userID;
            this.join = join;
            this.quit = quit;
            this.ip = ip;
            this.logins = logins;
            this.ips = ips;
        }

        public void setQuitTime() {
			this.quit = System.currentTimeMillis();
		}
		
		@Override
		public void addBatch(PreparedStatement preparedStatement) throws SQLException {
			preparedStatement.setInt(1, userID);
			preparedStatement.setLong(2, join);
			preparedStatement.setLong(3, quit);
			preparedStatement.setString(4, ip);
			preparedStatement.setInt(5, port);
			preparedStatement.setBoolean(6, firstTime);
		}

        @Override
        public boolean isBatchable() {
            return ((userID = getUserID(UUID)) != -1);
        }

        @Override
        public long getTimestamp() {
            return join;
        }

        @Override
        public FancyMessage asFancyMessage() {
            FancyMessage timestamp = super.asFancyMessage();
            if (ips && !logins) {
                return timestamp.then("IP: " + ip);
            }
            //Add the login times
            FancyMessage login = timestamp.then("Logged in from ")
                    .then("[" + DateUtil.format("HH:mm", join) + "]").color(ChatColor.GOLD).tooltip(DateUtil.format("dd MMMM yyyy | HH:mm:ss zzz", join))
                    .then(" till ")
                    .then("[" + DateUtil.format("HH:mm", quit) + "]").color(ChatColor.GOLD).tooltip(DateUtil.format("dd MMMM yyyy | HH:mm:ss zzz", quit));

            //Check if IP needs to be added
            if (!ips) return login;

            //=> Add the IP
            return login.then(" [IP]").color(ChatColor.DARK_AQUA).tooltip("IP: " + ip);
        }
    }
	
	/*
	 ************************
	 * Calculations/Getters *
	 ************************
	 */
	
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

        //Get info
		String UUID = p.getUniqueId().toString();
        UUIDControl.UUIDProfile profile = UUIDControl.getInstance().getUUIDProfile(UUID);
        if (profile == null) {
            throw new CommandException("Your UserID is not known yet.");
        }
        int userID = profile.getUserID();
		long playtime = 0L;
		
		for (Batchable batchable : instance.finishedSessions) { //Loop thru unbatched sessions
			Session finishedSession = (Session) batchable;
			if (UUID.equalsIgnoreCase(finishedSession.UUID)) { //If current player add time
				playtime += finishedSession.quit - finishedSession.join;
			}
		}
		
		Connection con = SQLPool.getConnection();
		try {
			PreparedStatement prep = con.prepareStatement( //Get Total time from DB
				"SELECT SUM( `quit_time` ) - SUM( `join_time` ) AS `playtime` FROM `sh_logger_times` WHERE `user_id` = ?;"
			);
			prep.setInt(1, userID);
			ResultSet timeRS = prep.executeQuery();
			if (timeRS.next()) { //If time given
				playtime += timeRS.getLong(1);
			}
			
			//Get AFK time
			long afk = AFKLogger.getAFKTime(profile);
			
			if (instance.activeSessions.containsKey(UUID)) { //Add current online time
				playtime += (System.currentTimeMillis() - instance.activeSessions.get(UUID).join);
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
		HashMap<Integer, Long> map = getPlayedTimes(times[0], times[1], addEntries); //Get playedtimes
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
	 * @param players The UUIDProfile's of the players that need to be checked
	 * @param fromDate From date, can be null
	 * @param toDate To date, can be null
	 * @return Map with all players and their play times, possible that a player isn't in the map.
	 */
	public HashMap<Integer, Long> getPlayedTimes(UUIDControl.UUIDProfile[] players, Date fromDate, Date toDate) {
		long[] times = parseDates(fromDate, toDate);
		return getPlayedTimes(times[0], times[1], 0, players);
	}
	
	/**
	 * Get played times (between two dates)
	 * @param playerProfile The player's UUIDProfile
	 * @param fromDate From date, if null = since start of this system.
	 * @param toDate Till date, if null = till now.
	 * @return time of that player, or 0 if not played
	 */
	public long getPlayedTime(UUIDControl.UUIDProfile playerProfile, Date fromDate, Date toDate) {
		long[] times = parseDates(fromDate, toDate); //Parse times
		HashMap<Integer, Long> map = getPlayedTimes(times[0], times[1], 0, playerProfile); //Get map
		if (map.isEmpty()) {
			return 0;
		} else {
			return map.get(playerProfile.getUserID());
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
	 * @return HashMap with all played times Key:[UserID] => Value:[TimePlayed]
	 */
	private HashMap<Integer, Long> getPlayedTimes(long fromTime, long toTime, int limit, UUIDControl.UUIDProfile... players) {
		batch(sqlQuery, finishedSessions, true); //Batch in sync with this thread
		
		Connection con = SQLPool.getConnection();

        //Time map
        //K:[UserID] => V:[Time Played]
		HashMap<Integer, Long> timesMap = new HashMap<>();
		if (limit < 1 || limit > 20) { //Limit is set to 20
			limit = 20;
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
				playersSQL += "`user_id` = ?"; //Add player
				first = false;
			}
			playersSQL += ") ";
		}
		
		
		try {
			PreparedStatement prep = con.prepareStatement( //Create Prep'd Statement
				"SELECT " +
					"`user_id`, " +
					"SUM( IF(`quit_time` > ?, ?, `quit_time`) - IF(`join_time` < ?, ?, `join_time`) ) as `online_time` " +
				"FROM `sh_logger_times` " +
				"WHERE (" +
						"(`join_time` > ? AND `quit_time` < ?)" +
							" OR " +
						"(`join_time` < ? AND `quit_time` > ? AND `quit_time` < ?)" +
							" OR " +
						"(`quit_time` > ? AND `join_time` > ? AND `join_time` < ?) " +
					") " + 
					playersSQL + 
				"GROUP BY `user_id` " +
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
			for (UUIDControl.UUIDProfile player : players) { //Set players if there are any
				prep.setInt(x++, player.getUserID());
			}
			
			//Limit
			prep.setLong(x, limit);
			
			ResultSet rs = prep.executeQuery(); //Execute			
			while (rs.next()) { //Loop thru results
				timesMap.put(
					rs.getInt(1), //Get UserID
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
				for (UUIDControl.UUIDProfile player : players) { //Loop thru given players
					if (player.getUUID().equalsIgnoreCase(session.UUID)) {
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
                int userID = UUIDControl.getInstance().getUUIDProfile(session.UUID).getUserID();
				if (timesMap.containsKey(userID)) { //If already in map
					played += timesMap.get(userID); //Add to time
				}
				timesMap.put(userID, played); //Put combined time in map
			}
		}
		return timesMap;
	}
	
	public class LeaderboardEntry implements Comparable<LeaderboardEntry> {
		
		private Integer userID;
		private long playtime;
		
		public LeaderboardEntry(Integer userID, long playtime) {
			this.userID = userID;
			this.playtime = playtime;
		}

        /**
         * Get the UserID of this entry
         * @return the User ID
         */
        public Integer getUserID() {
            return userID;
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
			return new Long(o.playtime).compareTo(new Long(playtime));
		}
		
	}
	
	/**
	 * Parse a HashMap into leaderboardEntries, and sort the leaderboard in the Desc order.
	 * @param playedTimes The played times
	 * @return Sorted Array
	 */
	public ArrayList<LeaderboardEntry> createSortLeaderboardEntries(HashMap<Integer, Long> playedTimes) {
		ArrayList<LeaderboardEntry> entries = new ArrayList<>();
		for (Entry<Integer, Long> entry : playedTimes.entrySet()) {
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
