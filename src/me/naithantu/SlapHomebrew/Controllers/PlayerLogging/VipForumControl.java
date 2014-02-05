package me.naithantu.SlapHomebrew.Controllers.PlayerLogging;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;

import me.naithantu.SlapHomebrew.Commands.Exception.CommandException;
import me.naithantu.SlapHomebrew.Util.Log;
import me.naithantu.SlapHomebrew.Util.SQLPool;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class VipForumControl extends AbstractLogger {

	private static VipForumControl instance;
	private SimpleDateFormat format;
	
	//SQL Statements
	private String sqlQuery = 
		"INSERT INTO `mcecon`.`vip_forum` (`iteration`, `id`, `player`, `added_time`, `promotion`, `handled_by_staff`, `handled_time`, `comment`) " +
			"VALUES (?, ?, ?, ?, ?, ?, ?, ?) " +
		"ON DUPLICATE KEY UPDATE `handled_by_staff` = ?, `handled_time` = ?, `comment` = ?;";
	
	//Batch with unfinished promotions
	private HashSet<Batchable> batch;
	
	/**
	 * HashMap that contains unfinished ForumPromotions. 
	 * K:[Iteration] -> V:[Iteration Map]
	 * 		=> K:[ForumPromotion ID] -> V:[ForumPromotion]
	 */
	private HashMap<Integer, HashMap<Integer, ForumPromotion>> unfinishedForumPromotions;
	
	//ForumPromotions Iteration && ID
	private int currentIteration;
	private int currentID;
	
	public VipForumControl() {
		super();
		if (!enabled) return;
		
		unfinishedForumPromotions = new HashMap<>();
		
		loadStoredData();
		if (!enabled) return;		
		
		batch = new HashSet<>();
		format = new SimpleDateFormat("dd MMM. HH:mm zzz");
		instance = this;
	}
	
	/**
	 * Load the iteration
	 * Load all unfinished plot marks
	 */
	private void loadStoredData() {
		Connection con = SQLPool.getConnection(); //Get a connection
		try {
			//Get Highest current iteration
			ResultSet rs = con.createStatement().executeQuery("SELECT MAX(`iteration`) FROM `vip_forum`;");
			rs.next(); //Next!
			currentIteration = rs.getInt(1); //Get Iteration
			if (rs.wasNull()) {  //If was null, set to default
				currentIteration = 1;
				currentID = 0;
				return;
			}
			
			//Get highest ID from this current iteration
			PreparedStatement prep = con.prepareStatement("SELECT MAX(`id`) FROM `vip_forum` WHERE `iteration` = ?;");
			prep.setInt(1, currentIteration); //Set current iteration
			ResultSet idRS = prep.executeQuery(); //Execute the Query
			idRS.next(); //Next!
			currentID = idRS.getInt(1); //Get the ID
			
			//Get unfinished plot marks
			ResultSet unfinishedRS = con.createStatement().executeQuery(
				"SELECT `iteration`, `id`, `player`, `added_time`, `promotion` FROM `vip_forum` WHERE `handled_by_staff` IS NULL;"
			);
			while (unfinishedRS.next()) { //Foreach unfinished promotion
				ForumPromotion fp = new ForumPromotion( //Create new Promotion 
						unfinishedRS.getInt(1),
						unfinishedRS.getInt(2),
						unfinishedRS.getString(3),
						unfinishedRS.getLong(4),
						unfinishedRS.getBoolean(5),
						null,
						null,
						null
				);
				addToUnfinishedMap(fp); //Add to map
			}		
		} catch (SQLException e) {
			e.printStackTrace();
			enabled = false;
			Log.severe("Failed to load saved data for VipForumControl.");
		} finally {
			SQLPool.returnConnection(con);
		}
	}
	
	/**
	 * Put the ForumPromotion in the unfinishedPromotionMap
	 * @param promotion The ForumPromotion
	 */
	private void addToUnfinishedMap(ForumPromotion promotion) {
		HashMap<Integer, ForumPromotion> iterationMap = unfinishedForumPromotions.get(promotion.iteration); //Get map for this iteration
		if (iterationMap == null) { //If no map for this iteration yet
			iterationMap = new HashMap<>(); //Create map
			unfinishedForumPromotions.put(promotion.iteration, iterationMap); //Put new IterationMap in the map
		}
		iterationMap.put(promotion.ID, promotion); //Put Promotion in the maps
	}

	
	/**
	 * Log a new Forum promotion
	 * @param player The player
	 * @param promotion The promotion
	 */
	public static void logForumPromotion(String player, boolean promotion) {
		if (instance != null) {
			instance.addForumPromotion(player, promotion);
		} else {
			Log.info("[Forum Promotion] Player " + player + " needs to be " + (promotion ? "promoted." : "demoted."));
		}
	}
	
	/**
	 * See {@link VipForumControl#logForumPromotion(String, boolean)}
	 */
	private void addForumPromotion(final String player, final boolean promotion) {
		ForumPromotion fp = new ForumPromotion(currentIteration, ++currentID, player, System.currentTimeMillis(), promotion, null, null, null);
		batch.add(fp); //Add to batch
		addToUnfinishedMap(fp); //Add to unfinished map
	}
	
	/**
	 * Send unfinished promotions to a player
	 * @param p The player
	 */
	public static void sendNumberOfUnfinishedPromotions(Player p) {
		if (instance == null) return; //Check if enabled
		if (instance.unfinishedForumPromotions.isEmpty()) return; //Check if any unfinished forum promotions
		int promotionsLeft = 0;
		for (HashMap<Integer, ForumPromotion> iterationMap : instance.unfinishedForumPromotions.values()) {
			promotionsLeft += iterationMap.size();
		}
		p.sendMessage(ChatColor.GREEN + "There " + (promotionsLeft == 1 ? "is 1 pending forum promotion." : "are " + promotionsLeft + " pending forum promotions."));
	}
	
	/**
	 * Get the VipForumControl instance
	 * @return the instance
	 * @throws CommandException if not enabled
	 */
	public static VipForumControl getInstance() throws CommandException {
		if (instance == null) throw new CommandException("VipForumControl is not enabled.");
		return instance;
	}
	
	/**
	 * Send pending forum promotions to a player
	 * @param cs The player
	 * @throws CommandException if no pending forum promotions
	 */
	public void sendPendingPromotions(CommandSender cs) throws CommandException {
		if (unfinishedForumPromotions.isEmpty()) throw new CommandException("There are no pending forum promotions."); //Check if any pending
		ArrayList<ForumPromotion> promotions = new ArrayList<>();
		for (HashMap<Integer, ForumPromotion> iterationMap : unfinishedForumPromotions.values()) { //Put all Promotions in a Array
			for (ForumPromotion fp : iterationMap.values()) {
				promotions.add(fp);
			}
		}
		int size = promotions.size();
		if (size > 1) { //Sort the array if needed
			Collections.sort(promotions);
		}
		cs.sendMessage(ChatColor.AQUA + "---------- " + size + " Forum " + ((size == 1) ? "Promotion" : "Promotions") + " Pending ----------");
		for (ForumPromotion fp : promotions) { //Send info
			fp.sendInfo(cs);
		}
	}
	
	/**
	 * Finish a pending forum promotion
	 * Uses the current iteration
	 * @param ID The ID
	 * @param handledBy Handled by CommandSender 
	 * @param comment Optional comment
	 * @throws CommandException if no pending FP with this ID
	 */
	public void finishPendingPromotion(int ID, String handledBy, String comment) throws CommandException {
		finishPendingPromotion(currentIteration, ID, handledBy, comment);
	}
	
	/**
	 * Finish a pending Forum Promotion
	 * @param iteration The iteration
	 * @param ID The ID
	 * @param handledBy Handled by CommandSender
	 * @param comment Optional comment
	 * @throws CommandException if no pending FP with this ID
	 */
	public void finishPendingPromotion(int iteration, int ID, String handledBy, String comment) throws CommandException {
		ForumPromotion fp;
		try {
			fp = unfinishedForumPromotions.get(iteration).get(ID); //Get ForumPromotion
			if (fp == null) throw new NullPointerException(); //If none found with that ID
		} catch (NullPointerException e) {
			throw new CommandException("There is no pending forum promotion with ID: #" + ID + ChatColor.GRAY + " (Iteration #" + iteration + ")");
		}
		//Set paramaters
		fp.handledBy = handledBy;
		fp.handledTime = System.currentTimeMillis();
		fp.comment = comment;
		
		//If not added to the batch
		if (!batch.contains(fp)) {
			batch.add(fp);
		}
		
		unfinishedForumPromotions.get(iteration).remove(ID); //Remove from Unfinished		
	}
	
	@Override
	public void batch() {
		batch(sqlQuery, batch);
	}

	@Override
	protected void createTables() throws SQLException {
		executeUpdate(
			"CREATE TABLE IF NOT EXISTS `vip_forum` ( " +
				"`iteration` int(11) NOT NULL, " +
				"`id` int(11) NOT NULL, " +
				"`player` varchar(255) NOT NULL, " +
				"`added_time` bigint(20) NOT NULL, " +
				"`promotion` tinyint(1) NOT NULL, " +
				"`handled_by_staff` varchar(255) DEFAULT NULL, " +
				"`handled_time` bigint(20) DEFAULT NULL, " +
				"`comment` varchar(1000) DEFAULT NULL, " +
			"PRIMARY KEY (`iteration`,`id`) " +
			") ENGINE=InnoDB DEFAULT CHARSET=latin1;"
		);
	}

	@Override
	public void shutdown() {
		batch();
		instance = null;
	}
	
	private class ForumPromotion implements Batchable, Comparable<ForumPromotion> {
		
		private int iteration;
		private int ID;
		
		private String player;
		private long promotionTime;
		private boolean promotion;
		private String handledBy;
		private Long handledTime;
		private String comment;
		
		public ForumPromotion(int iteration, int ID, String player, long promotionTime, boolean promotion, String handledBy, Long handledTime, String comment) {
			this.iteration = iteration;
			this.ID = ID;
			this.player = player;
			this.promotionTime = promotionTime;
			this.promotion = promotion;
			this.handledBy = handledBy;
			this.handledTime = handledTime;
			this.comment = comment;
		}
		
		@Override
		public void addBatch(PreparedStatement preparedStatement) throws SQLException {
			//Insert -> ID & Iteration
			preparedStatement.setInt(1, iteration);
			preparedStatement.setInt(2, ID);
			
			//Insert -> Main stuff
			preparedStatement.setString(3, player);
			preparedStatement.setLong(4, promotionTime);
			preparedStatement.setBoolean(5, promotion);
			
			//Insert -> Extra stuff (most likely null)
			preparedStatement.setString(6, handledBy);
			if (handledTime == null) { preparedStatement.setNull(7, Types.BIGINT); }
			else { preparedStatement.setLong(7, handledTime); }
			preparedStatement.setString(8, comment);
			
			//Update
			preparedStatement.setString(9, handledBy);
			if (handledTime == null) { preparedStatement.setNull(10, Types.BIGINT); }
			else { preparedStatement.setLong(10, handledTime); }
			preparedStatement.setString(11, comment);
		}	
		
		/**
		 * Send info about the ForumPromotion to a player
		 * @param cs The player
		 */
		public void sendInfo(CommandSender cs) {
			cs.sendMessage(
				ChatColor.WHITE + "ID: " + ChatColor.GREEN + ((currentIteration == iteration) ? "#"+ ID : "#" + iteration + "." + ID) +
				ChatColor.WHITE + " - Time: " + ChatColor.GOLD + format.format(promotionTime) +
				ChatColor.WHITE + " - " + (promotion ? ChatColor.GREEN + "Promote " : ChatColor.RED + "Demote") + ChatColor.GOLD + " " + player
			);
			if (handledBy != null) {
				cs.sendMessage(" \u2517\u25B6 Handled by: " + ChatColor.GREEN + handledBy + ChatColor.WHITE + " - Time: " + ChatColor.GOLD + format.format(handledTime)); //Send handled info
				if (comment != null) {
					cs.sendMessage(ChatColor.GRAY + "  \u2517\u25B6 Comment: " + comment);
				}
			}
		}
		
		@Override
		public int compareTo(ForumPromotion o) {
			int iterationDiff = iteration - o.iteration; //Calculate Iteration diff
			if (iterationDiff != 0) return iterationDiff; //If not same iteration
			return ID - o.ID; //Otherwise ID
		}
		
	}

}
