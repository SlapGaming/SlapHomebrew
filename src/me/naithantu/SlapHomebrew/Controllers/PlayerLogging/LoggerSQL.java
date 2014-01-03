package me.naithantu.SlapHomebrew.Controllers.PlayerLogging;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import me.naithantu.SlapHomebrew.Util.Log;

public class LoggerSQL {

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
	 * Get the SQL Connection
	 * @return the con
	 */
	public Connection getConnection() {
		return con;
	}

}
