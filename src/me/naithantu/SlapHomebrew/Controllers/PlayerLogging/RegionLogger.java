package me.naithantu.SlapHomebrew.Controllers.PlayerLogging;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashSet;

import org.bukkit.entity.Player;

import com.sk89q.worldguard.protection.regions.ProtectedRegion;

public class RegionLogger extends AbstractLogger {

	private static RegionLogger instance;
	
	private HashSet<Batchable> changes;
	
	private String sqlQuery = 
			"INSERT INTO `mcecon`.`logger_regions` (`world`, `region`, `changed_time`, `changed_by`, `changer_is_a`, `type`, `parameter`) " +
			"VALUES (?, ?, ?, ?, ?, ?, ?);";
	
	public RegionLogger(LoggerSQL sql) {
		super(sql);
		if (!enabled) return;
		changes = new HashSet<>();
		instance = this;
	}
	
	@Override
	public void createTables() throws SQLException {
		executeUpdate(
			"CREATE TABLE IF NOT EXISTS `logger_regions` ( " +
			"`world` varchar(255) NOT NULL, " +
			"`region` varchar(255) NOT NULL, " +
			"`changed_time` bigint(20) NOT NULL, " +
			"`changed_by` varchar(20) NOT NULL, " +
			"`changer_is_a` enum('staff','owner','member','') NOT NULL, " +
			"`type` enum('addmember','removemember','addowner','removeowner','create','remove','flag','priority','redefine') NOT NULL, " +
			"`parameter` varchar(255) NOT NULL, KEY `region` (`region`), " +
			"KEY `changed_time` (`changed_time`), " +
			"KEY `world` (`world`) " +
			") ENGINE=InnoDB DEFAULT CHARSET=latin1;"
		);
	}
		
	/**
	 * Add a change to the MySQL DB
	 * @param world The world the region is in
	 * @param region The name of the region
	 * @param changedBy The name of the player who changed the region
	 * @param changer The type of the player who changed the region (Staff, Owner, Member)
	 * @param changeType The type of the change
	 * @param parameters The paramaters given to the change (players, flags, etc)
	 */
	private void addChange(String world, String region, String changedBy, ChangerIsA changer, ChangeType changeType, String parameters) {
		RegionChange change = new RegionChange(world, region, System.currentTimeMillis(), changedBy, changer, changeType, parameters);
		changes.add(change);
		if (changes.size() >= 20 && plugin.isEnabled()) {
			batch(sqlQuery, changes);
		}
	}

	/**
	 * Add a change to the MySQL DB
	 * @param region The changed region
	 * @param changedBy The player who changed the region
	 * @param changer The type of the player who changed the region (Staff, Owner, Member)
	 * @param changeType The type of change
	 * @param parameters Extra parameters. Can be null.
	 */
	public static void logRegionChange(ProtectedRegion region, Player changedBy, ChangerIsA changer, ChangeType changeType, String parameters) {
		if (instance != null) instance.addChange(changedBy.getWorld().getName(), region.getId(), changedBy.getName(), changer, changeType, parameters);
	}
			
	@Override
	public void shutdown() {
		batch(sqlQuery, changes);
		instance = null;
	}
	
	private class RegionChange implements Batchable {
		
		String world;
		String region;
		long changedTime;
		String changedBy;
		ChangerIsA aType;
		ChangeType type;
		String parameters;
		
		public RegionChange(String world, String region, long changedTime, String changedBy, ChangerIsA aType, ChangeType type, String parameters) {
			this.world = world;
			this.region = region;
			this.changedTime = changedTime;
			this.changedBy = changedBy;
			this.aType = aType;
			this.type = type;
			this.parameters = parameters;
		}
		
		@Override
		public void addBatch(PreparedStatement preparedStatement) throws SQLException {
			preparedStatement.setString(1, world);
			preparedStatement.setString(2, region);
			preparedStatement.setLong(3, changedTime);
			preparedStatement.setString(4, changedBy);
			preparedStatement.setString(5, aType.toString());
			preparedStatement.setString(6, type.toString());
			preparedStatement.setString(7, parameters);
		}
				
	}
	
	/**
	 * A type of region change
	 */
	public enum ChangeType {
		addmember, removemember, addowner, removeowner, create, remove, flag, priority, redefine
	}
	
	/**
	 * The type of a player who changes a region
	 */
	public enum ChangerIsA {
		staff, owner, member
	}

}
