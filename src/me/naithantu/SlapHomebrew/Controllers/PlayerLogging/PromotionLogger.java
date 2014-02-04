package me.naithantu.SlapHomebrew.Controllers.PlayerLogging;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashSet;

public class PromotionLogger extends AbstractLogger {

	private static PromotionLogger instance;
	
	private HashSet<Batchable> batch;
	
	private String sqlQuery = "INSERT INTO `mcecon`.`logger_promotions` (`player`, `time`, `from_rank`, `to_rank`, `type`, `promoted_by`) VALUES (?, ?, ?, ?, ?, ?);";
	
	public PromotionLogger() {
		super();
		if (!enabled) return; //Return
		
		batch = new HashSet<>();
		instance = this;
	}

	@Override
	public void batch() {
		batch(sqlQuery, batch);
	}

	@Override
	protected void createTables() throws SQLException {
		executeUpdate(
			"CREATE TABLE IF NOT EXISTS `logger_promotions` ( " +
				"`player` varchar(20) NOT NULL, " +
				"`time` bigint(20) NOT NULL, " +
				"`from_rank` varchar(255) NOT NULL, " +
				"`to_rank` varchar(255) NOT NULL, " +
				"`type` enum('promoted','demoted') NOT NULL, " +
				"`promoted_by` varchar(255) DEFAULT NULL, " +
			"PRIMARY KEY (`player`,`time`) " +
			") ENGINE=InnoDB DEFAULT CHARSET=latin1;"
		);
	}
	
	/**
	 * Add a rank change to the batch
	 * @param playername The player's name
	 * @param fromRank From rank
	 * @param toRank To rank
	 * @param promotion Was a promotion
	 * @param promotedBy promoted by/what. Can be null
	 */
	public static void logRankChange(String playername, String fromRank, String toRank, boolean promotion, String promotedBy) {
		if (instance != null) instance.addRankChange(playername, fromRank, toRank, promotion, promotedBy);
	}
	
	/**
	 * See {@link PromotionLogger#logRankChange(String, String, String, boolean, String)}
	 */
	private void addRankChange(String playername, String fromRank, String toRank, boolean promotion, String promotedBy) {
		Promotion promo = new Promotion(playername, System.currentTimeMillis(), fromRank, toRank, promotion, promotedBy);
		batch.add(promo);
	}

	@Override
	public void shutdown() {
		batch();
		instance = null;
	}
	
	private class Promotion implements Batchable {
		
		private String player;
		private long promotedTime;
		private String fromRank;
		private String toRank;
		private boolean promotion;
		private String promotedBy;
		
		public Promotion(String player, long promotedTime, String fromRank, String toRank, boolean promotion, String promotedBy) {
			this.player = player;
			this.promotedTime = promotedTime;
			this.fromRank = fromRank;
			this.toRank = toRank;
			this.promotion = promotion;
			this.promotedBy = promotedBy;
		}
		
		@Override
		public void addBatch(PreparedStatement preparedStatement) throws SQLException {
			preparedStatement.setString(1, player);
			preparedStatement.setLong(2, promotedTime);
			preparedStatement.setString(3, fromRank);
			preparedStatement.setString(4, toRank);
			preparedStatement.setString(5, (promotion ? "promoted" : "demoted"));
			preparedStatement.setString(6, promotedBy);
		}
		
	}

}
