package me.naithantu.SlapHomebrew.Controllers.PlayerLogging;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashSet;

public class PromotionLogger extends AbstractLogger {

	private static PromotionLogger instance;
	
	private HashSet<Batchable> batch;

    private String sqlQuery = "INSERT INTO `sh_logger_promotions`(`promoted_player`, `timestamp`, `from_rank`, `to_rank`, `type`, `promoted_by`) VALUES (?, ?, ?, ?, ?, ?);";
	
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

	}
	
	/**
	 * Add a rank change to the batch
	 * @param UUID The player's name
	 * @param fromRank From rank
	 * @param toRank To rank
	 * @param promotion Was a promotion
	 * @param promotedBy promoted by/what. Can be null
	 */
	public static void logRankChange(String UUID, String fromRank, String toRank, boolean promotion, String promotedBy) {
		if (instance != null) instance.addRankChange(UUID, fromRank, toRank, promotion, promotedBy);
	}
	
	/**
	 * See {@link PromotionLogger#logRankChange(String, String, String, boolean, String)}
	 */
	private void addRankChange(String UUID, String fromRank, String toRank, boolean promotion, String promotedBy) {
		Promotion promo = new Promotion(UUID, System.currentTimeMillis(), fromRank, toRank, promotion, promotedBy);
		batch.add(promo);
	}

	@Override
	public void shutdown() {
		batch();
		instance = null;
	}
	
	private class Promotion implements Batchable {
		
		private int userID;
        private String UUID;
		private long promotedTime;
		private String fromRank;
		private String toRank;
		private boolean promotion;
		private String promotedBy;
		
		public Promotion(String UUID, long promotedTime, String fromRank, String toRank, boolean promotion, String promotedBy) {
			this.UUID = UUID;
			this.promotedTime = promotedTime;
			this.fromRank = fromRank;
			this.toRank = toRank;
			this.promotion = promotion;
			this.promotedBy = promotedBy;
		}
		
		@Override
		public void addBatch(PreparedStatement preparedStatement) throws SQLException {
			preparedStatement.setInt(1, userID);
			preparedStatement.setLong(2, promotedTime);
			preparedStatement.setString(3, fromRank);
			preparedStatement.setString(4, toRank);
			preparedStatement.setString(5, (promotion ? "promoted" : "demoted"));
			preparedStatement.setString(6, promotedBy);
		}

        @Override
        public boolean isBatchable() {
            return ((userID = getUserID(UUID)) != -1);
        }
    }

}
