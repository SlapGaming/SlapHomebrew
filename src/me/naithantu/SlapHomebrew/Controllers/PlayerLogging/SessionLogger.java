package me.naithantu.SlapHomebrew.Controllers.PlayerLogging;

import java.net.InetSocketAddress;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;

import me.naithantu.SlapHomebrew.Commands.AbstractCommand;
import me.naithantu.SlapHomebrew.Commands.Exception.CommandException;
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
	
	public SessionLogger(LoggerSQL sql) {
		super(sql);
		if (!enabled) return;
		activeSessions = new HashMap<>();
		finishedSessions = new HashSet<>();
		instance = this;
	}
	
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
	
	/**
	 * Send the playtime to a player
	 * @param p The player
	 * @throws CommandException
	 */
	public static void sendPlayerTime(final Player p) throws CommandException {
		if (instance == null) {
			AbstractCommand.removeDoingCommand(p);
			throw new CommandException("The SessionLogger is currently disabled.");
		}
		
		String playername = p.getName();
		long playtime = 0L;
		
		for (Batchable batchable : instance.finishedSessions) { //Loop thru unbatched sessions
			Session finishedSession = (Session) batchable;
			if (finishedSession.player.equalsIgnoreCase(playername)) { //If current player add time
				playtime += finishedSession.quit - finishedSession.join;
			}
		}
		
		try {
			PreparedStatement prep = instance.sql.getConnection().prepareStatement( //Get Total time from DB
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
			AbstractCommand.removeDoingCommand(p);
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

}
