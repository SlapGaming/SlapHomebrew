package me.naithantu.SlapHomebrew.Controllers.PlayerLogging;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashSet;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.PlayerDeathEvent;

public class DeathLogger extends AbstractLogger implements Listener {

	private static DeathLogger instance;
	
	private HashSet<String> suiciders;
	
	private HashSet<Batchable> deaths;
	private String deathsSQL = "INSERT INTO `mcecon`.`logger_deaths` (`player`, `time`, `deathcause`) VALUES (?, ?, ?);";
	
	private HashSet<Batchable> kills;
	private String killsSQL = "INSERT INTO `mcecon`.`logger_kills` (`killed_player`, `time`, `killed_by`) VALUES (?, ?, ?);";
	
	public DeathLogger(LoggerSQL sql) {
		super(sql);
		if (!enabled) return;
		suiciders = new HashSet<>();
		
		deaths = new HashSet<>();
		kills = new HashSet<>();
		instance = this;
	}
	
	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent event) {
		Player killedPlayer = event.getEntity();
		EntityDamageEvent damageEvent = killedPlayer.getLastDamageCause();
		if (damageEvent != null) { //Check if hurt
			String playername = killedPlayer.getName();
			String causeReason;
			
			DamageCause cause = damageEvent.getCause(); //Get cause
			Player killer = killedPlayer.getKiller(); //Get the killer, if there is one
			long matchedTime = System.currentTimeMillis(); //Get time for matched time (Death & Kill)
			
			if (suiciders.contains(playername)) { //If player /suicided
				suiciders.remove(playername); //Remove from set
				causeReason = "SUICIDE";
			} else if (killer != null) { //If player got killed by another player
				causeReason = "PLAYER";
				kills.add(new PlayerKilled(killedPlayer.getName(), matchedTime, killer.getName())); //Add the kill
			} else { //Other reason
				causeReason = cause.toString();
			}
			deaths.add(new PlayerDeath(killedPlayer.getName(), matchedTime, causeReason)); //Add the death
			
			if (deaths.size() > 25) { //Check if there are more than 25 deaths recorded
				batch(deathsSQL, deaths);
			} else if (kills.size() > 25) { //else check for kills (else is for preventing batching both at the same time).
				batch(killsSQL, kills);
			}
		}
	}
	
	/**
	 * A player commits suicide
	 * @param playername The player
	 */
	public static void playerCommitsSuicide(String playername) {
		if (instance != null) {
			instance.suiciders.add(playername); //Add to map
		}
	}

	@Override
	protected void createTables() throws SQLException {
		executeUpdate( //Create deaths table
			"CREATE TABLE IF NOT EXISTS `logger_deaths` ( " +
			"`player` varchar(255) NOT NULL, " +
			"`death_time` bigint(20) NOT NULL, " +
			"`deathcause` varchar(255) NOT NULL, " +
			"KEY `player` (`player`,`death_time`) " +
			") ENGINE=InnoDB DEFAULT CHARSET=latin1;"
		);
		executeUpdate( //Create kills table
			"CREATE TABLE IF NOT EXISTS `logger_kills` ( " +
			"`killed_player` varchar(255) NOT NULL, " +
			"`death_time` bigint(20) NOT NULL, " +
			"`killed_by` varchar(255) NOT NULL, " +
			"KEY `killed_player` (`killed_player`,`death_time`,`killed_by`) " +
			") ENGINE=InnoDB DEFAULT CHARSET=latin1;"
		);
	}
	
	private class PlayerKilled implements Batchable {
		
		String killedPlayer;
		long time;
		String killedBy;
		
		public PlayerKilled(String killedPlayer, long time, String killedBy) {
			this.killedPlayer = killedPlayer;
			this.time = time;
			this.killedBy = killedBy;
		}	
		
		@Override
		public void addBatch(PreparedStatement preparedStatement) throws SQLException {
			preparedStatement.setString(1, killedPlayer);
			preparedStatement.setLong(2, time);
			preparedStatement.setString(3, killedBy);
		}
		
	}
	
	private class PlayerDeath implements Batchable {
		
		String player;
		long time;
		String deathCause;
		
		public PlayerDeath(String player, long time, String deathCause) {
			this.player = player;
			this.time = time;
			this.deathCause = deathCause;
		}	
		
		@Override
		public void addBatch(PreparedStatement preparedStatement) throws SQLException {
			preparedStatement.setString(1, player);
			preparedStatement.setLong(2, time);
			preparedStatement.setString(3, deathCause);
		}
		
	}

	@Override
	public void shutdown() {
		if (!deaths.isEmpty()) {
			batch(deathsSQL, deaths);
		}
		if (!kills.isEmpty()) {
			batch(killsSQL, kills);
		}
		instance = null;
	}

}
