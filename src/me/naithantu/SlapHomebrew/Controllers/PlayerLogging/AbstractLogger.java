package me.naithantu.SlapHomebrew.Controllers.PlayerLogging;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashSet;

import nl.stoux.SlapPlayers.Model.Profile;
import nl.stoux.SlapPlayers.SlapPlayers;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginManager;

import me.naithantu.SlapHomebrew.Controllers.AbstractController;
import me.naithantu.SlapHomebrew.Util.Log;
import me.naithantu.SlapHomebrew.Util.Util;

public abstract class AbstractLogger extends AbstractController {

	protected boolean enabled;
	
	public AbstractLogger() {
		enable();
	}
	
	private void enable() {
		try {
			createTables();
			enabled = true;
		} catch (SQLException e) {
			Log.severe("Failed to enable logger (" + this.getClass().getName() + "). Exception: " + e.getMessage());
			enabled = false;
		}
	}
	
	public void registerEvents(PluginManager pm) {
		if (this instanceof Listener) {
			pm.registerEvents((Listener) this, plugin);
		}
	}
	
	/**
	 * Batch a set of Batchables into the MySQL DB
	 * @param sqlStatement The SQL Statement for the PreparedStatement
	 * @param set The set of batchables
	 * @param sync Specify if this should be run in sync. Standard is aSync.
	 */
	protected void batch(final String sqlStatement, HashSet<Batchable> set, boolean... sync) {
		if (set.size() == 0) return;
		final HashSet<Batchable> batch = new HashSet<>(set);
		set.clear();

        //Check which ones can be batched
        for (Batchable b : batch) {
            if (!b.isBatchable()) {
                set.add(b);
            }
        }
        //=> Remove any batchables that cannot be batched
        for (Batchable b : set) {
            batch.remove(b);
        }

        //Check batch size
        if (batch.isEmpty()) {
            return;
        }
		
		boolean inSync = false;
		if (sync.length > 0) {
			inSync = sync[0];
		}
		
		if (inSync) {
			executeBatch(sqlStatement, batch);
		} else {
			Util.runASync(new Runnable() {
				@Override
				public void run() {
					executeBatch(sqlStatement, batch);
				}
			});
		}
	}
	
	private void executeBatch(String sqlStatement, HashSet<Batchable> batch) {
		Connection con = plugin.getSQLPool().getConnection(); //Get connection
		try {
			PreparedStatement prep = con.prepareStatement(sqlStatement);
			for (Batchable batchable : batch) { //Prepare batch
				batchable.addBatch(prep);
				prep.addBatch();
			}
			prep.executeBatch(); //Execute
		} catch (SQLException e) {
			Log.severe("Failed to insert batch. Batchable class: " + batch.iterator().next().getClass().getName() + " | Exception: " + e.getMessage());
		} finally {
            plugin.getSQLPool().returnConnection(con); //Return connection
		}
	}
	
	/**
	 * Insert the batch into the SQL DB
	 */
	public abstract void batch();
	
	
	protected abstract void createTables() throws SQLException;

    /**
     * Get the UserID for a UUID.
     * @param UUID The UUID of the player
     * @return the user id or -1 if there's no profile found
     */
    protected static int getUserID(String UUID) {
        Profile profile = SlapPlayers.getUUIDController().getProfile(UUID);
        return (profile == null ? -1 : profile.getID());
    }

    /**
     * Get a Player's name based on their UserID
     * @param userID The ID
     * @return The playername or null
     */
    protected static String getPlayernameOnID(int userID) {
        Profile profile = SlapPlayers.getUUIDController().getProfile(userID);
        if (profile == null) {
            return null;
        }
        return profile.getCurrentName();
    }

	/**
	 * Execute an update on a normal statement
	 * @param query The query
	 * @return row count
	 * @throws SQLException if failed
	 */
	protected int executeUpdate(String query) throws SQLException {
		Connection con = plugin.getSQLPool().getConnection(); //Get Connection
		int result = con.createStatement().executeUpdate(query); //Execute update
        plugin.getSQLPool().returnConnection(con); //Return connection
		return result; //Return result
	}
	
	/**
	 * See if this logger is enabled
	 * @return
	 */
	public boolean isEnabled() {
		return enabled;
	}


}
