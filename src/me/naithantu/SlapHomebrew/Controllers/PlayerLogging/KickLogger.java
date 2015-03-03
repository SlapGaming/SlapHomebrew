package me.naithantu.SlapHomebrew.Controllers.PlayerLogging;

import com.mcbans.firestar.mcbans.MCBans;
import me.naithantu.SlapHomebrew.Commands.Exception.CommandException;
import mkremins.fanciful.FancyMessage;
import nl.stoux.SlapPlayers.SlapPlayers;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class KickLogger extends AbstractLogger implements Listener {

	private static KickLogger instance;
	
	private HashSet<Batchable> kicks;
	private HashMap<String, String> kickedByMap;
	
	private String sqlQuery =
            "INSERT INTO `sh_logger_kicks`(`user_id`, `kicked_time`, `kicked_by`, `reason`) " +
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

	}
		
	@EventHandler(priority=EventPriority.MONITOR)
	public void onKick(PlayerKickEvent event) {
		if (!event.isCancelled()) {
			String UUID = event.getPlayer().getUniqueId().toString(); //Get the kicked player
			String by = null;
			if (kickedByMap.containsKey(UUID)) { //If kicked by a person thru a different command (like /skick)
				by = kickedByMap.get(UUID); //Get from map
				kickedByMap.remove(UUID); //And remove from map
			}
			addKick(new PlayerKicked(UUID, System.currentTimeMillis(), by, event.getReason())); //Add the kick
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
     * Add a kicked by player to the map
     * @param UUID The UUID of the kicked player
     * @param kickedByUUID The UUID of the player who kicked the other one
     */
    private void addPlayerKickedBy(String UUID, String kickedByUUID) {
        kickedByMap.put(UUID, kickedByUUID);
    }

	/**
	 * Log a kicked by player.
	 * In example: Log who kicked a player using /skick
	 * @param player The player that is kicked
	 * @param kickedBy The commandsender that kicked the player
	 */
	public static void logPlayerKickedBy(Player player, CommandSender kickedBy) {
        String kickedByUUID = (kickedBy instanceof Player ? ((Player) kickedBy).getUniqueId().toString() : "CONSOLE");
		if (instance != null) instance.addPlayerKickedBy(player.getUniqueId().toString(), kickedByUUID);
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

    /**
     * Get a list of all kicks of a certain player
     * @param userID The ID of the player
     * @return the list of kicks
     * @throws CommandException if an error occurred
     */
    public static ArrayList<Profilable> getKicks(int userID) throws CommandException {
        //Create a new List
        ArrayList<Profilable> kicks = new ArrayList<>();

        //Get a connection
        Connection con = instance.plugin.getSQLPool().getConnection();
        try {
            //Get the kicks
            PreparedStatement prep = con.prepareStatement("SELECT `kicked_time`, `kicked_by`, `reason` FROM `sh_logger_kicks` WHERE `user_id` = ?;");
            prep.setInt(1, userID);

            //Get the results
            ResultSet rs = prep.executeQuery();
            while (rs.next()) {
                //Get the data
                long kickedTime = rs.getLong(1);
                Integer kickedBy = rs.getInt(2);
                if (rs.wasNull()) {
                    kickedBy = null;
                }
                String reason = rs.getString(3);

                //Add the new Kick
                kicks.add(instance.new PlayerKicked(userID, kickedTime, kickedBy, reason));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new CommandException("An error occurred! Notify Stoux!");
        } finally {
            instance.plugin.getSQLPool().returnConnection(con);
        }

        return kicks;
    }

    public class PlayerKicked extends Profilable implements Batchable {

        int userID;
		String UUID;
		long kickedTime;
        Integer kickedByID;
		String kickedByUUID;
		String kickReason;
		
		public PlayerKicked(String UUID, long kickedTime, String kickedByUUID, String kickReason) {
			this.UUID = UUID;
			this.kickedTime = kickedTime;
			this.kickedByUUID = kickedByUUID;
			this.kickReason = kickReason;
		}

        private PlayerKicked(int userID, long kickedTime, Integer kickedByID, String kickReason) {
            this.userID = userID;
            this.kickedTime = kickedTime;
            this.kickedByID = kickedByID;
            this.kickReason = kickReason;
        }

        @Override
		public void addBatch(PreparedStatement preparedStatement) throws SQLException {
			preparedStatement.setInt(1, userID);
			preparedStatement.setLong(2, kickedTime);
            if (kickedByUUID == null) {
                preparedStatement.setNull(3, Types.INTEGER);
            } else {
                preparedStatement.setInt(3, kickedByID);
            }
			preparedStatement.setString(4, kickReason);
		}

        @Override
        public boolean isBatchable() {
            //Check the main user
            if (((userID = getUserID(UUID)) == -1)) {
                return false;
            }

            //Check the KickedBy param
            if (kickedByUUID != null) {
                return ((kickedByID = getUserID(kickedByUUID)) != -1);
            }

            //All good
            return true;
        }

        @Override
        public long getTimestamp() {
            return kickedTime;
        }

        @Override
        public FancyMessage asFancyMessage() {
            FancyMessage kick = super.asFancyMessage().then("Kicked");

            //Check if the player is kicked by a mod
            if (kickedByID != null) {
                kick = kick.then(" by ").then(SlapPlayers.getUUIDController().getProfile(kickedByID).getCurrentName()).color(ChatColor.GOLD);
            }

            //Add the rest
            return kick.then(": " + kickReason);
        }
    }
	
	private class MCBansListener implements Listener {
		
		@EventHandler(priority=EventPriority.MONITOR)
		public void onKick(com.mcbans.firestar.mcbans.events.PlayerKickEvent event) {
			if (!event.isCancelled()) {
                String senderUUID = "";
                if ("CONSOLE".equalsIgnoreCase(event.getSender())) {
                    senderUUID = "CONSOLE";
                } else {
                    senderUUID = event.getSenderUUID().toString();
                }
				addPlayerKickedBy(event.getPlayerUUID().toString(), senderUUID);
			}
		}
		
	}

}
