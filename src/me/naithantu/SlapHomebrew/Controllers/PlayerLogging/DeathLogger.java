package me.naithantu.SlapHomebrew.Controllers.PlayerLogging;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
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
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.PlayerDeathEvent;

public class DeathLogger extends AbstractLogger implements Listener {

	private static DeathLogger instance;
	
	private HashSet<String> suiciders;
	
	private HashSet<Batchable> deaths;
	private String deathsSQL = "INSERT INTO `mcecon`.`logger_deaths` (`player`, `death_time`, `deathcause`) VALUES (?, ?, ?);";
	
	private HashSet<Batchable> kills;
	private String killsSQL = "INSERT INTO `mcecon`.`logger_kills` (`killed_player`, `death_time`, `killed_by`) VALUES (?, ?, ?);";
	
	public DeathLogger() {
		super();
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
			
			if (deaths.size() > 25 || kills.size() > 25) {
				batch();
			}
		}
	}
	
	@Override
	public void batch() {
		batch(deathsSQL, deaths);
		Util.runLater(plugin, new Runnable() {
			
			@Override
			public void run() {
				batch(killsSQL, kills);
			}
		}, 600);
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
	
	/**
	 * Send the deaths of a player
	 * @param p The player
	 * @throws CommandException if DeathLogging disabled.
	 */
	public static void sendPlayerDeaths(final Player p) throws CommandException {
		if (instance == null) { //Check if initialized
			AbstractCommand.removeDoingCommand(p);
			throw new CommandException("DeathLogging is currently disabled.");
		}
		Util.runASync(instance.plugin, new Runnable() {
			
			@Override
			public void run() {
				HashMap<String, Integer> deathMap = new HashMap<>();
				String playername = p.getName();
				
				int totalDeaths = 0;
				
				for (Batchable batchable : instance.deaths) { //Loop thru deaths still waiting to be batched
					PlayerDeath death = (PlayerDeath) batchable;
					if (death.player.equalsIgnoreCase(playername)) { 
						addToDeathMap(deathMap, death.deathCause, 1);
						totalDeaths++;
					}
				}
				Connection con = SQLPool.getConnection(); //Get connection
				try {
					PreparedStatement prep = con.prepareStatement( //Prepare Statement to get Deaths from SQL
						"SELECT COUNT(*) as `Deaths`, `deathcause` FROM `logger_deaths` WHERE `player` = ? GROUP BY `deathcause`;"
					);
					prep.setString(1, playername);
					ResultSet deathRS = prep.executeQuery(); //Execute
					
					while (deathRS.next()) { //Loop thru results
						int deathNrs = deathRS.getInt(1);
						String cause = deathRS.getString(2);
						addToDeathMap(deathMap, cause, deathNrs); //Add to map
						totalDeaths += deathNrs;
					}
					
					if (deathMap.isEmpty()) { //No deaths
						Util.badMsg(p, "You have not died since 5 Jan. '14.");
					} else {
						Util.msg(p, "You have died " + totalDeaths + " " + (totalDeaths == 1 ? "time" : "times") + " since 5 Jan. '14"); //Send start message
						for (Entry<String, Integer> entry : deathMap.entrySet()) { //Loop thru deaths
							String reason = getReason(entry.getKey()); //Parse -> Reason
							if (reason != null) { //Send reason if not null
								p.sendMessage(ChatColor.GOLD + "  \u2517\u25B6 " + ChatColor.GRAY + reason + ": " + ChatColor.WHITE + entry.getValue());
							}
						}
					}					
				} catch (SQLException e) {
					Util.badMsg(p, "Something went wrong!");
					e.printStackTrace();
				} finally {
					SQLPool.returnConnection(con); //Return connection
					AbstractCommand.removeDoingCommand(p);
				}
			}
		});
	}
	
	/**
	 * Add deaths to the death map
	 * @param map The map
	 * @param cause The deathcause
	 * @param amountOfDeaths The number of deaths
	 */
	private static void addToDeathMap(HashMap<String, Integer> map, String cause, int amountOfDeaths) {
		if (map.get(cause) != null) {
			amountOfDeaths += map.get(cause);
		}
		map.put(cause, amountOfDeaths);
	}

	/**
	 * Get the reason based on the DeathCause
	 * @param cause The cause
	 * @return The reason
	 */
	private static String getReason(String cause) {
		String reason = null;
		if (cause.equalsIgnoreCase("player")) { //If cause = by player
			reason = "Killed by players";
		} else { //Standard death reason
			try {
				switch(DamageCause.valueOf(cause.toUpperCase())) { //Switch DamageCauses
				case BLOCK_EXPLOSION:	reason = "By Exploding blocks";					break;
				case CONTACT:			reason = "By block damage (Ex. Cactus)";		break;
				case CUSTOM:			reason = "Other";								break;
				case DROWNING:			reason = "Drowned";								break;
				case ENTITY_ATTACK:		reason = "Killed by mobs";						break;
				case ENTITY_EXPLOSION:	reason = "Killed by exploding mobs";			break;
				case FALL:				reason = "Fell to your death";					break;
				case FALLING_BLOCK:		reason = "Blocks fell on your head";			break;
				case FIRE: 
				case FIRE_TICK:			reason = "Burned to your death";				break;
				case LAVA:				reason = "Drowned in lava";						break;
				case LIGHTNING:			reason = "Struck by lightning";					break;
				case MAGIC:				reason = "Killed by magic";						break;
				case MELTING:			reason = "You melted, wat";						break;
				case POISON:			reason = "Poisoned";							break;
				case PROJECTILE:		reason = "Hit by projectiles";					break;
				case STARVATION:		reason = "Starved to death";					break;
				case SUFFOCATION:		reason = "Suffocated in a wall";				break;
				case SUICIDE:			reason = "Suicided";							break;
				case THORNS:			reason = "Killed by thorns";					break;
				case VOID:				reason = "Fell out the world";					break;
				case WITHER:			reason = "Killed by wither damage";				break;
				}
			} catch (IllegalArgumentException e) {
				
			}
		}
		return reason;
	}
	
	/**
	 * Send the number of kills to the player
	 * @param p The player
	 * @throws CommandException if DeathLogging is disabled
	 */
	public static void sendPlayerKills(final Player p) throws CommandException {
		if (instance == null) { //Check if initialized
			AbstractCommand.removeDoingCommand(p);
			throw new CommandException("DeathLogging is currently disabled.");
		}
		Util.runASync(instance.plugin, new Runnable() {
			@Override
			public void run() {
				ArrayList<PlayerKilled> kills = new ArrayList<>();
				String playername = p.getName();
				
				for (Batchable batchable : instance.kills) { //Loop thru unbatched kills
					PlayerKilled kill = (PlayerKilled) batchable;
					if (kill.killedBy.equalsIgnoreCase(playername) || kill.killedPlayer.equalsIgnoreCase(playername)) { //Add to arraylist if player = sender
						kills.add(kill);
					}
				}
				Connection con = SQLPool.getConnection(); //Get connection
				try {
					PreparedStatement prep = con.prepareStatement( //Query for getting kills out of SQL
						"SELECT `killed_player`, `killed_by` FROM `logger_kills` WHERE `killed_player` = ? OR `killed_by` = ?;"
					);
					prep.setString(1, playername);
					prep.setString(2, playername);
					ResultSet killRS = prep.executeQuery();
					while (killRS.next()) { //Loop thru results
						kills.add(instance.new PlayerKilled(killRS.getString(1), 0, killRS.getString(2))); //Add to kills
					}
					
					//Maps
					HashMap<String, Integer> playerKilled = new HashMap<>();
					HashMap<String, Integer> playerGotKilled = new HashMap<>();
					
					for (PlayerKilled kill : kills) { //Loop thru kills
						if (kill.killedBy.equalsIgnoreCase(playername)) { //Player killed another player, add to that map
							addToKillMap(playerKilled, kill.killedPlayer);
						} else { //Player got killed
							addToKillMap(playerGotKilled, kill.killedBy); 
						}
					}
					
					//Loop thru stuff for Killed By
					int numberOfTimesKilled = 0; int mostKilledByKills = 0; String mostKilledBy = null;
					for (Entry<String, Integer> entry : playerGotKilled.entrySet()) {
						numberOfTimesKilled += entry.getValue();
						if (mostKilledByKills < entry.getValue()) {
							mostKilledByKills = entry.getValue();
							mostKilledBy = entry.getKey();
						}
					}
					
					//Same thing for Kills
					int numberOfKills = 0; int mostKills = 0; String mostKillsOn = null;
					for (Entry<String, Integer> entry : playerKilled.entrySet()) {
						numberOfKills += entry.getValue();
						if (mostKills < entry.getValue()) {
							mostKills = entry.getValue();
							mostKillsOn = entry.getKey();
						}
					}
					
					
					if (numberOfKills == 0 && numberOfTimesKilled == 0) { //No kills, nor has been killed
						Util.msg(p, "You haven't killed anyone, nor have you been killed since this was implemented.");
					} else {
						//Send kills
						Util.msg(p, "You have killed " + numberOfKills + (numberOfKills == 1 ? " person." : " people and have been killed " + numberOfTimesKilled + (numberOfTimesKilled == 1 ? " time." : " times.")));
						if (mostKills > 1) {
							p.sendMessage(ChatColor.GOLD + "  \u2517\u25B6 " + ChatColor.GRAY + "Most killed: " + mostKillsOn + " (" + mostKills + " times)");
						}
						if (numberOfTimesKilled > 1) {
							p.sendMessage(ChatColor.GOLD + "  \u2517\u25B6 " + ChatColor.GRAY + "Most killed by: " + mostKilledBy + " (" + mostKilledByKills + " times)");
						}
					}
				} catch (SQLException e) {
					Util.badMsg(p, "Something went wrong!");
					e.printStackTrace();
				} finally {
					SQLPool.returnConnection(con); //Return connection
					AbstractCommand.removeDoingCommand(p);
				}
			}
		});		
	}
	
	/**
	 * Add a kill to map
	 * @param map The map
	 * @param player The player
	 */
	private static void addToKillMap(HashMap<String, Integer> map, String player) {
		int kills = 1;
		if (map.containsKey(player)) {
			kills += map.get(player);
		}
		map.put(player, kills);
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
		batch();
		instance = null;
	}

}
