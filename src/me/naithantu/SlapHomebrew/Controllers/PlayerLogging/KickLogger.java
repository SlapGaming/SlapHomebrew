package me.naithantu.SlapHomebrew.Controllers.PlayerLogging;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.HashMap;
import java.util.HashSet;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
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
	
	private class PlayerKicked implements Batchable {

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
