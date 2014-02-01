package me.naithantu.SlapHomebrew.Controllers.PlayerLogging;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashSet;

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
	 */
	protected void batch(final String sqlStatement, HashSet<Batchable> set) {
		if (set.size() == 0) return;
		final HashSet<Batchable> batch = new HashSet<>(set);
		set.clear();
		Util.runASync(plugin, new Runnable() {
			@Override
			public void run() {
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
		});
	}
	
	/**
	 * Insert the batch into the SQL DB
	 */
	public abstract void batch();
	
	
	protected abstract void createTables() throws SQLException;
	
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
