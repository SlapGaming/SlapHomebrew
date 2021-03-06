package me.naithantu.SlapHomebrew.Controllers.PlayerLogging;

import com.nyancraft.reportrts.ReportRTS;
import com.nyancraft.reportrts.data.HelpRequest;
import com.nyancraft.reportrts.event.ReportCompleteEvent;
import me.naithantu.SlapHomebrew.Util.Log;
import me.naithantu.SlapHomebrew.Util.Util;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;

public class ModreqLogger extends AbstractLogger implements Listener {

	private String sqlQuery = 
			"INSERT INTO `mcecon`.`logger_modreqs` (`iteration`, `modreq_id`, `issued_time`, `issued_by_player`, `request`, `handled_time`, `handled_by_staff`) " +
			"VALUES (?, ?, ?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE `handled_time` = ?, `handled_by_staff` = ?;";
	
	private HashMap<Integer, CompletedModreq> modreqBatch;
	private int iteration;
	
	public ModreqLogger() {
		super();
		findReportRTS();
		if (!enabled) return;
		modreqBatch = new HashMap<Integer, ModreqLogger.CompletedModreq>();
		findIteration();
	}
	
	/**
	 * Find the ReportRTS plugin
	 */
	private void findReportRTS() {
		Plugin reportRTS = plugin.getServer().getPluginManager().getPlugin("ReportRTS"); //Get Plugin
		if (reportRTS == null || !reportRTS.isEnabled() || !(reportRTS instanceof ReportRTS)) {
			enabled = false;
		}
	}
	
	@Override
	public void createTables() throws SQLException {

	}
	
	/**
	 * Find the current iteration in the database
	 */
	private void findIteration() {
		Connection con = plugin.getSQLPool().getConnection();
		try {
			ResultSet rs = con.createStatement().executeQuery("SELECT MAX(`iteration`) FROM `logger_modreqs`;"); //Get max current iteration
			rs.next();
			iteration = rs.getInt(1);
			if (rs.wasNull()) { //No entries yet
				iteration = 0;
			}
		} catch (SQLException e) {
			enabled = false;
			Log.severe("Failed to find iteration (ModreqLogger). Exception: " + e.getMessage());
		} finally {
            plugin.getSQLPool().returnConnection(con);
		}
	}
		
	/**
	 * Called when a Mod completes a modreq.
	 * @param event
	 */
	@EventHandler
	public void onCompleteModreq(ReportCompleteEvent event) {
		final HelpRequest req = event.getRequest();
		final int id = req.getId();
		if (iteration == 0 || id == 1) { //Check Iteration
			iteration++;
		}
		
		Util.runLater(new Runnable() { //Run later, to prevent null?
			@Override
			public void run() {
				CompletedModreq cm = new CompletedModreq(iteration, id, req.getTimestamp(), req.getName(), req.getMessage(), req.getModTimestamp(), req.getModName());
				modreqBatch.put(id, cm);
				
				if (modreqBatch.size() > 20) {
					batchModreqs();
				}
			}
		}, 1);
	}
	
	/**
	 * Batch the modreqs
	 */
	private void batchModreqs() {
		if (modreqBatch.isEmpty()) return;
		batch(sqlQuery, new HashSet<Batchable>(modreqBatch.values()));
		modreqBatch = new HashMap<>();
	}
	
	@Override
	public void batch() {
		batchModreqs();
	}

	@Override
	public void shutdown() {
        batch(sqlQuery, new HashSet<Batchable>(modreqBatch.values()), true);
	}
	
	private class CompletedModreq implements Batchable {
		
		int iteration;
		int modreqID;
		long issuedTime;
		String issuedByPlayer;
		String request;
		long handledTime;
		String handledByStaff;
		
		public CompletedModreq(int iteration, int modreqID, long issuedTime, String issuedByPlayer, String request, long handledTime, String handledByStaff) {
			this.iteration = iteration;
			this.modreqID = modreqID;
			this.issuedTime = issuedTime;
			this.issuedByPlayer = issuedByPlayer;
			this.request = request;
			this.handledTime = handledTime;
			this.handledByStaff = handledByStaff;
		}
		
		@Override
		public void addBatch(PreparedStatement preparedStatement) throws SQLException {
			preparedStatement.setInt(1, iteration);
			preparedStatement.setInt(2, modreqID);
			preparedStatement.setLong(3, issuedTime);
			preparedStatement.setString(4, issuedByPlayer);
			preparedStatement.setString(5, request);
			preparedStatement.setLong(6, handledTime);
			preparedStatement.setString(7, handledByStaff);
			preparedStatement.setLong(8, handledTime);
			preparedStatement.setString(9, handledByStaff);
		}

        @Override
        public boolean isBatchable() {
            return false;
        }
    }

}
