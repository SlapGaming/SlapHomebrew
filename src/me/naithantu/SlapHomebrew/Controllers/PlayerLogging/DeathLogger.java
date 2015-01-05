package me.naithantu.SlapHomebrew.Controllers.PlayerLogging;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;

import me.naithantu.SlapHomebrew.Commands.AbstractCommand;
import me.naithantu.SlapHomebrew.Commands.Exception.CommandException;
import me.naithantu.SlapHomebrew.Util.DateUtil;
import me.naithantu.SlapHomebrew.Util.Util;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
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
	private String deathsSQL = "INSERT INTO `sh_logger_deaths`(`user_id`, `death_time`, `deathcause`) VALUES (?, ?, ?)";
	
	private HashSet<Batchable> kills;
	private String killsSQL = "INSERT INTO `sh_logger_kills`(`killed_player`, `death_time`, `killed_by`) VALUES (?, ?, ?)";
	
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
            String killedUUID = killedPlayer.getUniqueId().toString(); //Get
			int killedUserID = getUserID(killedUUID);

			DamageCause damageCause = damageEvent.getCause(); //Get cause
			Player killer = killedPlayer.getKiller(); //Get the killer, if there is one
			long matchedTime = System.currentTimeMillis(); //Get time for matched time (Death & Kill)

            //The death cause as String
            String cause;

			if (suiciders.contains(killedUUID)) { //If player /suicided
				suiciders.remove(killedUUID); //Remove from set
                cause = "SUICIDE";
			} else if (killer != null) { //If player got killed by another player
                cause = "PLAYER";
				kills.add(new PlayerKilled(killedUserID, matchedTime, getUserID(killer.getUniqueId().toString()))); //Add the kill
			} else { //Other reason
                cause = damageCause.toString();
			}
			deaths.add(new PlayerDeath(killedUserID, matchedTime, cause)); //Add the death
			
			if (deaths.size() > 25 || kills.size() > 25) {
				batch();
			}
		}
	}
	
	@Override
	public void batch() {
		batch(deathsSQL, deaths);
		Util.runLater(new Runnable() {
			
			@Override
			public void run() {
				batch(killsSQL, kills);
			}
		}, 600);
	}
	
	/**
	 * A player commits suicide
	 * @param UUID The player's UUID
	 */
	public static void playerCommitsSuicide(String UUID) {
		if (instance != null) {
			instance.suiciders.add(UUID); //Add to map
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
		Util.runASync(new Runnable() {
			
			@Override
			public void run() {
                //Map containing DeathCause -> NrOfDeaths
				HashMap<String, Integer> deathMap = new HashMap<>();
				String playername = p.getName();
                int playerID = getUserID(p.getUniqueId().toString());

                //Keep track of the number of total deaths
				int totalDeaths = 0;

                //Loop thru deaths still waiting to be batched
				for (Batchable batchable : instance.deaths) {
					PlayerDeath death = (PlayerDeath) batchable;
					if (death.player == playerID) {
						addToDeathMap(deathMap, death.deathCause, 1);
						totalDeaths++;
					}
				}

				Connection con = instance.plugin.getSQLPool().getConnection(); //Get connection
				try {
                    //Prepare Statement to get Deaths from SQL
					PreparedStatement prep = con.prepareStatement(
                        "SELECT COUNT(*) as `deaths`, `deathcause` FROM `sh_logger_deaths` WHERE `user_id` = ? GROUP BY `deathcause`;"
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
                    instance.plugin.getSQLPool().returnConnection(con); //Return connection
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
		if (cause.equalsIgnoreCase("PLAYER")) { //If cause = by player
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

        //Get the UserID
        final int userID = getUserID(p.getUniqueId().toString());

		Util.runASync(new Runnable() {
			@Override
			public void run() {
				ArrayList<PlayerKilled> kills = new ArrayList<>();

                //Loop thru unbatched kills
				for (Batchable batchable : instance.kills) {
					PlayerKilled kill = (PlayerKilled) batchable;
					if (kill.killedBy ==  userID || kill.killedPlayer == userID) { //Add to arraylist if player = sender
						kills.add(kill);
					}
				}

				Connection con = instance.plugin.getSQLPool().getConnection(); //Get connection
				try {
					PreparedStatement prep = con.prepareStatement( //Query for getting kills out of SQL
						"SELECT `killed_player`, `killed_by` FROM `sh_logger_kills` WHERE `killed_player` = ? OR `killed_by` = ?;"
					);
					prep.setInt(1, userID);
					prep.setInt(2, userID);
					ResultSet killRS = prep.executeQuery();
					while (killRS.next()) { //Loop thru results
						kills.add(instance.new PlayerKilled(killRS.getInt(1), 0, killRS.getInt(2))); //Add to kills
					}
					
					//Maps
					HashMap<Integer, Integer> playerKilled = new HashMap<>();
					HashMap<Integer, Integer> playerGotKilled = new HashMap<>();
					
					for (PlayerKilled kill : kills) { //Loop thru kills
						if (kill.killedBy == userID) { //Player killed another player, add to that map
							addToKillMap(playerKilled, kill.killedPlayer);
						} else { //Player got killed
							addToKillMap(playerGotKilled, kill.killedBy); 
						}
					}
					
					//Loop thru stuff for Killed By
					int numberOfTimesKilled = 0,
                        mostKilledByKills = 0,
                        mostKilledBy = 0;
					for (Entry<Integer, Integer> entry : playerGotKilled.entrySet()) {
						numberOfTimesKilled += entry.getValue();
						if (mostKilledByKills < entry.getValue()) {
							mostKilledByKills = entry.getValue();
							mostKilledBy = entry.getKey();
						}
					}
					
					//Same thing for Kills
					int numberOfKills = 0,
                        mostKills = 0,
                        mostKillsOn = 0;
					for (Entry<Integer, Integer> entry : playerKilled.entrySet()) {
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
							p.sendMessage(ChatColor.GOLD + "  \u2517\u25B6 " + ChatColor.GRAY + "Most killed: " + getPlayernameOnID(mostKillsOn) + " (" + mostKills + " times)");
						}
						if (numberOfTimesKilled > 1) {
							p.sendMessage(ChatColor.GOLD + "  \u2517\u25B6 " + ChatColor.GRAY + "Most killed by: " + getPlayernameOnID(mostKilledBy) + " (" + mostKilledByKills + " times)");
						}
					}
				} catch (SQLException e) {
					Util.badMsg(p, "Something went wrong!");
					e.printStackTrace();
				} finally {
                    instance.plugin.getSQLPool().returnConnection(con); //Return connection
					AbstractCommand.removeDoingCommand(p);
				}
			}
		});		
	}

    /**
     * Send the CommandSender the current kills leaderboard
     * @param p The CommandSender
     * @param thisMonth only get the leaderboard for this month
     * @throws CommandException if DeathLogging is currently disabled
     */
    public static void sendKillsLeaderboard(final CommandSender p, final boolean thisMonth) throws CommandException {
        if (instance == null) { //Check if initialized
            AbstractCommand.removeDoingCommand(p);
            throw new CommandException("DeathLogging is currently disabled.");
        }

        Util.runASync(new Runnable() {
            @Override
            public void run() {
                //Get a connection
                Connection con = instance.plugin.getSQLPool().getConnection();
                try {
                    //Monthly strings
                    String monthly = "";
                    long since = 0;
                    if (thisMonth) {
                        //Get a format of the current month + year based on Date now.
                        String formatted = DateUtil.format("MM-yyyy");
                        //=> Reverse the process only not giving a day, thus forcing it to go to the first of the month.
                        Date reversed = DateUtil.parse("MM-yyyy", formatted);
                        //=> Get unix timestamp
                        since = reversed.getTime();

                        //Set montly string
                        monthly = "WHERE `death_time` > ? ";
                    }

                    //Prepare statement
                    PreparedStatement lbPrep = con.prepareStatement(
                        "SELECT COUNT(*) as `kills`, `killed_by` as `killer` FROM `sh_logger_kills` " + monthly + "GROUP BY `killed_by` ORDER BY `kills` DESC LIMIT 0,10;"
                    );

                    //Check for Monthly
                    if (thisMonth) {
                        lbPrep.setLong(1, since);
                    }

                    //Excecute query
                    ResultSet lbRS = lbPrep.executeQuery();
                    String[] results = new String[10];
                    int resultPosition = 0;

                    //=> Loop thru results
                    while (lbRS.next()) {
                        //Get data
                        int kills = lbRS.getInt(1);
                        int playerID = lbRS.getInt(2);

                        //Create sentence
                        String sentence = ChatColor.GREEN + String.valueOf(resultPosition + 1) + ". " + ChatColor.GOLD + getPlayernameOnID(playerID) + ChatColor.WHITE + " - " + kills + (kills == 1 ? " kill" : " kills");
                        results[resultPosition++] = sentence;
                    }

                    //=> Check if there were any results
                    if (resultPosition == 0) {
                        results[0] = "There is nothing here =(";
                    }

                    //Send messages
                    Util.msg(p, ChatColor.YELLOW + "--- " + ChatColor.GOLD + "Kills Leaderboard" + ChatColor.YELLOW + " ---" + (thisMonth ? ChatColor.WHITE + " (This Month)" : ""));
                    for (String result : results) {
                        if (result == null) break; //Break for loop if nothing to show anymore.
                        p.sendMessage(result);
                    }
                } catch (SQLException | ParseException e) {
                    Util.badMsg(p, "An error occurred!");
                    e.printStackTrace(); //Debug
                } finally {
                    instance.plugin.getSQLPool().returnConnection(con); //Return connection
                    AbstractCommand.removeDoingCommand(p);
                }
            }
        });

    }
	
	/**
	 * Add a kill to map
	 * @param map The map
	 * @param playerID The player's ID
	 */
	private static void addToKillMap(HashMap<Integer, Integer> map, int playerID) {
		int kills = 1;
		if (map.containsKey(playerID)) {
			kills += map.get(playerID);
		}
		map.put(playerID, kills);
	}
	
	
	@Override
	protected void createTables() throws SQLException {
		//TODO
	}
	
	private class PlayerKilled implements Batchable {
		
		int killedPlayer;
		long time;
		int killedBy;
		
		public PlayerKilled(int killedPlayer, long time, int killedBy) {
			this.killedPlayer = killedPlayer;
			this.time = time;
			this.killedBy = killedBy;
		}	
		
		@Override
		public void addBatch(PreparedStatement preparedStatement) throws SQLException {
			preparedStatement.setInt(1, killedPlayer);
			preparedStatement.setLong(2, time);
			preparedStatement.setInt(3, killedBy);
		}

        @Override
        public boolean isBatchable() {
            //Always true, going to assume there's no possibility that the player can die before the MySQL Queries can finish.
            //EDGY AS FUCK
            return true;
        }
    }
	
	private class PlayerDeath implements Batchable {

        int player;
		long time;
		String deathCause;

        /**
         * A new PlayerDeath (event) to be registered in the DB
         * @param player The UserID of the player
         * @param time The time of death
         * @param deathCause The cause of death
         */
		public PlayerDeath(int player, long time, String deathCause) {
			this.player = player;
			this.time = time;
			this.deathCause = deathCause;
		}	
		
		@Override
		public void addBatch(PreparedStatement preparedStatement) throws SQLException {
			preparedStatement.setInt(1, player);
			preparedStatement.setLong(2, time);
			preparedStatement.setString(3, deathCause);
		}

        @Override
        public boolean isBatchable() {
            //See PlayerKilled -> isBatchable
            return true;
        }
    }

	@Override
	public void shutdown() {
		batch();
		instance = null;
	}

}
