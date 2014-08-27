package me.naithantu.SlapHomebrew.Controllers.PlayerLogging;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashSet;

import me.naithantu.SlapHomebrew.PlayerExtension.UUIDControl;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginManager;

import me.naithantu.SlapHomebrew.Controllers.AbstractController;
import me.naithantu.SlapHomebrew.Util.Log;
import me.naithantu.SlapHomebrew.Util.SQLPool;
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
		Connection con = SQLPool.getConnection(); //Get connection
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
			SQLPool.returnConnection(con); //Return connection
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
    public static int getUserID(String UUID) {
        UUIDControl.UUIDProfile profile = UUIDControl.getInstance().getUUIDProfile(UUID);
        return (profile == null ? -1 : profile.getUserID());
    }

    /**
     * Get a Player's name based on their UserID
     * @param userID The ID
     * @return The playername or null
     */
    public static String getPlayernameOnID(int userID) {
        UUIDControl.UUIDProfile profile = UUIDControl.getInstance().getUUIDProfile(userID);
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
		Connection con = SQLPool.getConnection(); //Get Connection
		int result = con.createStatement().executeUpdate(query); //Execute update
		SQLPool.returnConnection(con); //Return connection
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
