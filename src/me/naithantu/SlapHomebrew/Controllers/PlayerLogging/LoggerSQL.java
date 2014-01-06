package me.naithantu.SlapHomebrew.Controllers.PlayerLogging;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.bukkit.scheduler.BukkitTask;

import me.naithantu.SlapHomebrew.SlapHomebrew;
import me.naithantu.SlapHomebrew.Util.Log;
import me.naithantu.SlapHomebrew.Util.Util;

public class LoggerSQL {

	private BukkitTask pingingTask;
	
	private Connection con;
	
	public LoggerSQL() {
		
	}
	
	/**
	 * Connect to SQL
	 * @return
	 */
	public boolean connect() {
		try {
			con = DriverManager.getConnection("jdbc:mysql://localhost:3306/mcecon","mecon", "B9eCusTa");
			return true;
		} catch (SQLException e) {
			Log.severe("PlayerLogger failed to connect with SQL. Exception: " + e.getMessage());
			return false;
		}
	}
	
	/**
	 * Disconnect the LoggerSQL
	 */
	public void disconnect() {
		try {
			con.close();
			if (pingingTask instanceof BukkitTask && pingingTask != null) {
				pingingTask.cancel();
			}
		} catch (SQLException e) {
			//Failed to close.. Ignore
		}
	}
	
	/**
	 * Start pinging the SQL server
	 */
	public void startPinging() {
		pingingTask = Util.runASyncTimer(SlapHomebrew.getInstance(), new Runnable() {
			
			@Override
			public void run() {
				try {
					con.isValid(5);
				} catch (SQLException e) {
					Log.severe("Something went wrong with pinging the SQL. Exception: " + e.getMessage());
				}
			}
		}, 18000, 18000);
	}
	
	
	/**
	 * Get the SQL Connection
	 * @return the con
	 */
	public Connection getConnection() {
		return con;
	}

}
