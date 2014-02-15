package me.naithantu.SlapHomebrew.Controllers.PlayerLogging;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import me.naithantu.SlapHomebrew.Commands.Exception.CommandException;
import me.naithantu.SlapHomebrew.Util.Log;
import me.naithantu.SlapHomebrew.Util.SQLPool;
import me.naithantu.SlapHomebrew.Util.Util;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

public class PlotControl extends AbstractLogger {

	private static PlotControl instance;	
	
	private HashSet<String> doingCommand; 
	private HashMap<Integer, HashMap<Integer, PlotMark>> unfinishedPlotmarks;
	
	private int currentIteration;
	private int currentID;
	
	private SimpleDateFormat format;
	
	public PlotControl() {
		super();
		if (!enabled) return; //Check if enable
		
		unfinishedPlotmarks = new HashMap<>();
		getIteration(); //Get iteration, current ID & unfinished plot marks
		if (!enabled) return; //Check if still enabled
		
		instance = this;
		doingCommand = new HashSet<>();
		format = new SimpleDateFormat("dd MMM. HH:mm zzz");
	}
	
	/**
	 * Get the current iteration & ID
	 */
	private void getIteration() {
		Connection con = SQLPool.getConnection(); //Get con
		try {
			//Get current iteration
			ResultSet itRS = con.createStatement().executeQuery("SELECT MAX(`iteration`) FROM `logger_plots`;");
			itRS.next();
			currentIteration = itRS.getInt(1);
			if (itRS.wasNull()) { //No entries in the DB -> Set everything to 1
				currentIteration = 1;
				currentID = 0;
				return;
			}
			
			//Get Max ID
			PreparedStatement prep = con.prepareStatement("SELECT MAX(`id`) FROM `logger_plots` WHERE `iteration` = ?;");
			prep.setInt(1, currentIteration); //Set current iteration
			ResultSet idRS = prep.executeQuery(); //Execute the Query
			idRS.next();
			currentID = idRS.getInt(1);
			
			//Get unfinished plot marks
			ResultSet plotMarksRS = con.createStatement().executeQuery(
				"SELECT `iteration`, `id`, `marked_by`, `comment`, `mark_time`, `world`, `x`, `y`, `z` " +
				"FROM `logger_plots` WHERE `handled_by` IS NULL;"
			);
			while (plotMarksRS.next()) {
				PlotMark plotMark = new PlotMark( //Create new PlotMark
					plotMarksRS.getInt(1), 
					plotMarksRS.getInt(2),
					plotMarksRS.getString(3),
					plotMarksRS.getString(4),
					plotMarksRS.getLong(5),
					plotMarksRS.getString(6),
					plotMarksRS.getDouble(7),
					plotMarksRS.getDouble(8),
					plotMarksRS.getDouble(9)
				);
				addToUnfinishedMap(plotMark); //Add to map
			}
		} catch (SQLException e) {
			enabled = false;
			Log.severe("Failed to get iteration for PlotControl. Exception: " + e.getMessage());
		} finally {
			SQLPool.returnConnection(con);
		}
	}
	

	@Override
	protected void createTables() throws SQLException {
		executeUpdate(
			"CREATE TABLE IF NOT EXISTS `logger_plots` ( " +
			"`iteration` int(11) NOT NULL, `id` int(11) NOT NULL, `marked_by` varchar(255) NOT NULL, `comment` varchar(255) DEFAULT NULL, `mark_time` bigint(20) NOT NULL, " +
			"`world` varchar(255) NOT NULL, `x` double(10,3) NOT NULL, `y` double(7,3) NOT NULL, `z` double(10,3) NOT NULL, " +
			"`handled_by` varchar(255) DEFAULT NULL, `handled_time` bigint(20) DEFAULT NULL, `handled_comment` varchar(255) DEFAULT NULL, " +
			"PRIMARY KEY (`iteration`,`id`) ) ENGINE=InnoDB DEFAULT CHARSET=latin1;"
		);
	}

	@Override
	public void shutdown() {
		instance = null;
	}
	
	@Override
	public void batch() {
		//Not a batcher
	}
	
	/**
	 * Get the instance of PlotControl
	 * @return The instance
	 * @throws CommandException if not enabled
	 */
	public static PlotControl getInstance() throws CommandException {
		if (instance == null) throw new CommandException("Plot Control is not enabled!");
		return instance;
	}
	
	/**
	 * Send the number of unfinished plot marks to the player
	 * @param p The player
	 */
	public static void sendUnfinishedPlotMarks(Player p) {
		if (instance == null) return; //Check if instance available
		int pendingPlots = 0;
		for (HashMap<Integer, PlotMark> map : instance.unfinishedPlotmarks.values()) { //Add all pending plots
			pendingPlots += map.size();
		}
		if (pendingPlots > 0) { //If pending plots -> Send message
			p.sendMessage(ChatColor.GREEN + "There " + ((pendingPlots == 1) ? "is " : "are ") + pendingPlots + " pending plot " + ((pendingPlots == 1) ? "mark." : "marks."));
		}
	}
	
	/**
	 * Get all the unfinished plot marks
	 * @param p The player
	 * @throws CommandException if already doing command or no pending plot marks
	 */
	public void getUnfinishedPlotMarks(Player p) throws CommandException {
		checkDoingCommand(p, false);
		Set<Integer> iterationsSet = unfinishedPlotmarks.keySet();
		if (iterationsSet.isEmpty()) throw new CommandException("There are no pending plot marks."); //No plot marks
		
		ArrayList<PlotMark> plotMarks = new ArrayList<>();
		for (HashMap<Integer, PlotMark> idMap : unfinishedPlotmarks.values()) { //Loop thru iterations
			for (PlotMark foundPlotMark : idMap.values()) { //Loop thru plotmarks
				plotMarks.add(foundPlotMark); //Add plotmarks
			}
		}
		
		Collections.sort(plotMarks);
		int nrOfMarks = plotMarks.size();
		p.sendMessage(ChatColor.AQUA + "---------- " + nrOfMarks + " Plot " + ((nrOfMarks == 1) ? "Mark" : "Marks") + " Waiting ----------");
		for (PlotMark foundPlotMark : plotMarks) { //Send info about plot marks
			foundPlotMark.sendInfo(p);
		}
	}
	
	
	/**
	 * Get info about a plot mark
	 * This will take the current iteration as iteration
	 * @param p The player
	 * @param id The ID of the plot mark
	 * @throws CommandException if already doing a plot command
	 */
	public void getPlotMarkInfo(final Player p, final int id) throws CommandException {
		getPlotMarkInfo(p, currentIteration, id);
	}
	
	/**
	 * Get info about a plot mark
	 * @param p The player
	 * @param iteration The iteration
	 * @param id The ID of the plot mark
	 * @throws CommandException if already doing a plot command
	 */
	public void getPlotMarkInfo(final Player p, final int iteration, final int id) throws CommandException {
		try {
			PlotMark pm = unfinishedPlotmarks.get(iteration).get(id);
			pm.sendInfo(p);			
		} catch (NullPointerException e) {
			checkDoingCommand(p, true);
			p.sendMessage(ChatColor.GRAY + "Getting plot mark...");
			
			Util.runASync(new Runnable() {
				@Override
				public void run() {
					Connection con = SQLPool.getConnection();
					try {
						PlotMark pm = getPlotMarkSQL(con, iteration, id);
						pm.sendInfo(p);
					} catch (SQLException e) { //SQL Error
						Util.badMsg(p, "Failed to get plot mark.");
						Log.severe("Failed to get PlotMark. Exception: " + e.getMessage());
					} catch (CommandException e) { //Plot not found
						Util.badMsg(p, e.getMessage());
					} finally {
						SQLPool.returnConnection(con);
						doingCommand.remove(p.getName());
					}
				}
			});
		}
	}
	
	/**
	 * Mark a plot location
	 * Is marked at the player's current location
	 * @param p The player
	 * @param comment Any comment if specified, can be null.
	 * @throws CommandException if already doing a command
	 */
	public void markPlotLocation(final Player p, String comment) throws CommandException {
		checkDoingCommand(p, true);
		final PlotMark nPM = new PlotMark(p, comment);
		
		Util.runASync(new Runnable() {
			@Override
			public void run() {
				Connection con = SQLPool.getConnection();
				try {
					PreparedStatement prep = con.prepareStatement(
						"INSERT INTO `mcecon`.`logger_plots` (" +
						"`iteration`, `id`, `marked_by`, `comment`, `mark_time`, `world`, `x`, `y`, `z`) " +
						"VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?);"
					);
					
					//Set values
					prep.setInt(1, nPM.iteration);
					prep.setInt(2, nPM.id);
					prep.setString(3, nPM.byPlayer);
					prep.setString(4, nPM.comment);
					prep.setLong(5, nPM.time);
					prep.setString(6, nPM.world);
					prep.setDouble(7, nPM.x);
					prep.setDouble(8, nPM.y);
					prep.setDouble(9, nPM.z);
					
					//Insert
					prep.executeUpdate();
					
					//Add to map
					addToUnfinishedMap(nPM);
					
					//Msg
					Util.msg(p, "Marked plot with ID: " + ChatColor.GREEN + "#" + nPM.id);
				} catch (SQLException e) {
					Log.severe("Failed to mark plot location. Exception: " + e.getMessage());
					Util.badMsg(p, "Failed to mark plot. Warn Stoux if he's around.");
				} finally {
					SQLPool.returnConnection(con);
					doingCommand.remove(p.getName());
				}
			}
		});
	}
	
	/**
	 * Finish a plot mark.
	 * This will take the current iteration
	 * @param p The player that finished the plot mark
	 * @param id The ID
	 * @param comment An optional comment, can be null
	 * @throws CommandException if already doing a command
	 */
	public void finishPlotMark(final Player p, int id, String comment) throws CommandException {
		finishPlotMark(p, currentIteration, id, comment);
	}
	
	/**
	 * Finish a plot mark.
	 * @param p The player that finished the plot mark
	 * @param iteration The iteration
	 * @param id The ID
	 * @param comment An optional comment, can be null
	 * @throws CommandException if already doing a command
	 */
	public void finishPlotMark(final Player p, int iteration, int id, String comment) throws CommandException {
		try {
			HashMap<Integer, PlotMark> idMap = unfinishedPlotmarks.get(iteration);
			final PlotMark uPM = idMap.get(id);
			if (uPM == null) throw new NullPointerException(); //Throw new NullPointer if null
			
			checkDoingCommand(p, true);
			
			uPM.handledBy = p.getName();
			uPM.handledTime = System.currentTimeMillis();
			uPM.handledComment = comment;
			
			idMap.remove(id); //Remove PM From ID Map
			if (idMap.size() == 0) {  //If no marks left
				unfinishedPlotmarks.remove(iteration); //Remove whole map
			}
			
			Util.runASync(new Runnable() {
				
				@Override
				public void run() {
					Connection con = SQLPool.getConnection(); //Get connection
					try {
						PreparedStatement prep = con.prepareStatement(
							"UPDATE `logger_plots` SET `handled_by` = ?, `handled_time` = ?, `handled_comment` = ? " +
							"WHERE `iteration` = ? && `id` = ?;"
						);
						//New Data
						prep.setString(1, uPM.handledBy);
						prep.setLong(2, uPM.handledTime);
						prep.setString(3, uPM.handledComment);
						//Where clause
						prep.setInt(4, uPM.iteration);
						prep.setInt(5, uPM.id);
						//Update
						prep.executeUpdate();
						//Msg
						Util.msg(p, "Finished plot mark " + ChatColor.GREEN + "#" + ((uPM.iteration == currentIteration) ? uPM.id : uPM.iteration + "." + uPM.id) + "!");
					} catch (SQLException e) {
						Log.severe("Failed to finish plot mark. Exception: " + e.getMessage());
						Util.badMsg(p, "Failed to finish plot mark. Bug Stoux if he's around.");
						uPM.handledBy = null;
						addToUnfinishedMap(uPM);
					} finally {
						SQLPool.returnConnection(con); //Return connection
						doingCommand.remove(p.getName());
					}
				}
			});
		} catch (NullPointerException e) {
			throw new CommandException("No Unfinished PlotMark found with ID: " + id + ChatColor.GRAY + " (Iteration: " + iteration + ")");
		}
	}
	
	/**
	 * Get a plot mark from SQL.
	 * This needs to be called in A-Sync.
	 * @param con The SQL Connection
	 * @param iteration The iteration 
	 * @param id The id
	 * @return The plotmark or null (Will send a message)
	 * @throws SQLException if error with SQL
	 * @throws CommandException if no plot mark found
	 */
	private PlotMark getPlotMarkSQL(Connection con, int iteration, int id) throws SQLException, CommandException {
		PreparedStatement prep = con.prepareStatement(
			"SELECT `marked_by`, `comment`, `mark_time`, `world`, `x`, `y`, `z`, `handled_by`, `handled_time`, `handled_comment` " +
			"FROM `logger_plots` WHERE `iteration` = ? && `id` = ?;"
		);
		prep.setInt(1, iteration);
		prep.setInt(2, id);
		ResultSet rs = prep.executeQuery(); //Execute query
		if (!rs.next()) throw new CommandException("No PlotMark found with ID: " + id + ChatColor.GRAY + " (Iteration: " + iteration + ")"); //If nothing found -> Throw error
		
		return new PlotMark(
			iteration, //Iteration
			id, //Id
			rs.getString(1), //Marked by
			rs.getString(2), //Comment - Can be null
			rs.getLong(3), //Marked time
			rs.getString(4), //World
			rs.getDouble(5), //X
			rs.getDouble(6), //Y
			rs.getDouble(7), //Z
			
			//Handled stuff -- All can be null
			rs.getString(8), //Handled by 
			rs.getLong(9), //handled time
			rs.getString(10) //Handled comment
		);
	}
	
	/**
	 * Teleport to a marked plot
	 * This will take the current iteration as iteration.
	 * @param p The player
	 * @param id The ID of the plot
	 * @throws CommandException if already doing command
	 */
	public void tpToPlot(final Player p, int id) throws CommandException {
		tpToPlot(p, id, currentIteration);
	}
	
	/**
	 * Teleport to a marked plot
	 * @param p The player
	 * @param id The ID of the plot
	 * @param iteration The iteration this plot mark is under
	 * @throws CommandException if already doing command
	 */
	public void tpToPlot(final Player p, final int id, final int iteration) throws CommandException {
		try { //Try teleporting to a unfinished plot mark
			unfinishedPlotmarks.get(iteration).get(id).teleportTo(p);			
		} catch (NullPointerException e) { //Not an unfinished plot mark
			checkDoingCommand(p, true);
			p.sendMessage(ChatColor.GRAY + "Getting plot mark location. Teleport might be delayed.");
			
			Util.runASync(new Runnable() { //Get Plot Location
				@Override
				public void run() {
					Connection con = SQLPool.getConnection();
					try {
						final PlotMark pm = getPlotMarkSQL(con, iteration, id); //Get PlotMark
						Util.run(new Runnable() { //Run in Sync
							@Override
							public void run() {
								try {
									if (p.isOnline()) pm.teleportTo(p); //Try to TP
								} catch (CommandException e) { //Invalid world
									Util.badMsg(p, e.getMessage());
								}
							}
						});
					} catch (SQLException e) { //SQL Error
						Log.severe("Failed to get plot mark. Exception: " + e.getMessage());
						Util.badMsg(p, "Failed to get Plot Mark. Warn Stoux if he's around.");
					} catch (CommandException e) { //No PlotMark with this ID
						Util.badMsg(p, e.getMessage());
					} finally {
						SQLPool.returnConnection(con);
						doingCommand.remove(p.getName());
					}
				}
			});
		}
	}
	
	/**
	 * Add a plot mark to the unfinished map
	 * @param pm The plotmark
	 */
	private void addToUnfinishedMap(PlotMark pm) {
		HashMap<Integer, PlotMark> idMap = unfinishedPlotmarks.get(pm.iteration);
		if (idMap == null) {
			idMap = new HashMap<>();
			unfinishedPlotmarks.put(pm.iteration, idMap);
		}
		idMap.put(pm.id, pm);
	}
	
	/**
	 * Check if a player is already doing a command
	 * Add to set if specified
	 * @param p The player
	 * @param add Add to set if not doing a command
	 * @throws CommandException if doing a command
	 */
	private void checkDoingCommand(Player p, boolean add) throws CommandException {
		if (doingCommand.contains(p.getName())) {
			throw new CommandException("A command is still pending. Please wait a bit.");
		} else {
			if (add) doingCommand.add(p.getName()); //Add to list if specified
		}
	}
	
	
	private class PlotMark implements Comparable<PlotMark> {
				
		int iteration;
		int id;
		
		long time;
		String byPlayer;
		String comment;
		
		String world;
		double x;
		double y;
		double z;
		
		long handledTime;
		String handledBy;
		String handledComment;
		
		public PlotMark(int iteration, int id, String byPlayer, String comment, long time, String world, double x, double y, double z) {
			this.iteration = iteration;
			this.id = id;
			this.time = time;
			this.byPlayer = byPlayer;
			this.comment = comment;
			this.world = world;
			this.x = x;
			this.y = y;
			this.z = z;
		}	
		
		public PlotMark(Player p, String comment) {
			this.iteration = currentIteration;
			this.id = ++currentID;
			this.time = System.currentTimeMillis();
			this.byPlayer = p.getName();
			this.comment = comment;
			Location loc = p.getLocation();
			this.world = loc.getWorld().getName();
			this.x = loc.getX();
			this.y = loc.getY();
			this.z = loc.getZ();
		}
		
		public PlotMark(int iteration, int id, String byPlayer, String comment, long time, String world, double x, double y, double z, String handledBy, long handled, String handledComment) {
			this.iteration = iteration;
			this.id = id;
			this.time = time;
			this.byPlayer = byPlayer;
			this.comment = comment;
			this.world = world;
			this.x = x;
			this.y = y;
			this.z = z;
			this.handledTime = handled;
			this.handledBy = handledBy;
			this.handledComment = handledComment;
		}



		/**
		 * Teleport the player to this plot mark
		 * @param p The player
		 * @throws CommandException If world doesn't exist anymore
		 */
		public void teleportTo(Player p) throws CommandException {
			World w = plugin.getServer().getWorld(world);
			if (w == null) throw new CommandException("This Plot Mark is in a non-existing world.");
			p.teleport(new Location(w, x, y, z));
		}
		
		@Override
		public int compareTo(PlotMark o) {
			int iterationDiff = iteration - o.iteration; //Calculate Iteration diff
			if (iterationDiff != 0) return iterationDiff; //If not same iteration
			return id - o.id; //Otherwise ID
		}
		
		/**
		 * Send info about the plot mark to the player
		 * @param p The player
		 */
		public void sendInfo(Player p) {
			p.sendMessage(
				ChatColor.WHITE + "ID: " + ChatColor.GREEN + ((currentIteration == iteration) ? "#"+id : "#" + iteration + "." + id) +
				ChatColor.WHITE + " - Time: " + ChatColor.GOLD + format.format(time) +
				ChatColor.WHITE + " - By: " + ChatColor.GREEN + byPlayer					
			);
			if (comment != null) { //If a comment specified
				p.sendMessage(ChatColor.GRAY + " \u2517\u25B6 Comment: " + comment);
			}
			if (handledBy != null) { //If handled
				p.sendMessage("  \u2517\u25B6 Handled by: " + ChatColor.GREEN + handledBy + ChatColor.WHITE + " - Time: " + ChatColor.GOLD + format.format(handledTime)); //Send handled info
				if (handledComment != null) { //Check if comment
					p.sendMessage(ChatColor.GRAY + "   \u2517\u25B6 Comment: " + handledComment);
				}
			}
		}
	}
	
	

}
