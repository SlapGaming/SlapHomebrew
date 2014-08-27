package me.naithantu.SlapHomebrew.Controllers;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import me.naithantu.SlapHomebrew.Commands.Exception.AlreadyVIPException;
import me.naithantu.SlapHomebrew.Commands.Exception.NotVIPException;
import me.naithantu.SlapHomebrew.Controllers.PlayerLogging.PromotionLogger;
import me.naithantu.SlapHomebrew.Controllers.PlayerLogging.VipForumControl;
import me.naithantu.SlapHomebrew.PlayerExtension.UUIDControl;
import me.naithantu.SlapHomebrew.Storage.YamlStorage;
import me.naithantu.SlapHomebrew.Util.DateUtil;
import me.naithantu.SlapHomebrew.Util.Log;
import me.naithantu.SlapHomebrew.Util.SQLPool;
import me.naithantu.SlapHomebrew.Util.Util;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import ru.tehkode.permissions.PermissionGroup;
import ru.tehkode.permissions.PermissionUser;
import ru.tehkode.permissions.bukkit.PermissionsEx;

public class Vip extends AbstractController {
	
	/**
	 * HashMap containing players who have VIP till a specified date
	 * K:[UUID of Player] => V:[Time (in milliseconds) when the VIP ends]
	 */
	private ConcurrentHashMap<String, Long> temporaryVIPs;
	
	/**
	 * HashSet containing all the lifetime players UUID's
	 */
	private HashSet<String> lifetimeVIPs;
		
	/**
	 * Is correctly setup
	 */
	private boolean setup;
	
	/**
	 * Tab Controller for updating groups
	 */
	private TabController tabController;
		
	/**
	 * Storage & Config for Used Grants
	 */
	private YamlStorage storage;
	private FileConfiguration config;
	
	public Vip(TabController tabController) {
		if (!SQLPool.isSetup()) {
			setup = false;
			return;
		}
		this.tabController = tabController;
		temporaryVIPs = new ConcurrentHashMap<>();
		lifetimeVIPs = new HashSet<>();
		setup = createTable();
		if (isSetup()) { //If correctly setup
			loadVIPs();
		}
		if (isSetup()) { //If still correctly setup (managed to load data)
			startTimer(); //Start Check timer
		}
		//Get Storage
		storage = new YamlStorage(plugin, "usedgrants");
		config = storage.getConfig();
		
		//Check if Used Grants should be reset
		checkUsedGrants();
	}
	
	/**
	 * Check if the VIP has setup correctly 
	 * @return is setup
	 */
	public boolean isSetup() {
		return setup;
	}
	
	/**
	 * Load all VIPs from DB
	 */
	private void loadVIPs() {
		Connection con = SQLPool.getConnection();
		try {
			ResultSet rs = con.createStatement().executeQuery("SELECT `user_id`, `till_time`, `lifetime` FROM `sh_vip_time`;"); //Get All VIPs from DB
			while (rs.next()) {
				//Get stuff from ResultSet
				int userID = rs.getInt(1);
				Long tillTime = rs.getLong(2);
				boolean lifetime = rs.getBoolean(3);
                String UUID = UUIDControl.getInstance().getUUIDProfile(userID).getUUID();
				
				if (lifetime) { //If lifetime VIP
					lifetimeVIPs.add(UUID); //Add to lifetime VIP set
				} else { //If not VIP
					temporaryVIPs.put(UUID, tillTime); //Add to Temporary map
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
			setup = false;
		} finally {
			SQLPool.returnConnection(con);
		}
	}
	
	/**
	 * Start a timer for checking VIPs, every 10 minutes.
	 * Start checking after a minute.
	 */
	private void startTimer() {
		Util.runASyncTimer(new Runnable() {
			@Override
			public void run() {
				for (Entry<String, Long> entry : temporaryVIPs.entrySet()) { //Check all temporary VIPs
					if (entry.getValue() < System.currentTimeMillis()) { //Check if their end date is in the past
						try {
							removeVIP(entry.getKey()); //Remove their VIP
						} catch (NotVIPException e) {
							//This should be impossible.
						}
					}
				}
			}
		}, 1200, 12000);
	}
	
	
	
	/*
	 *****************
	 * VIP Functions *
	 *****************
	 */
	
	/**
	 * Check if a player is a VIP
	 * @param UUID The player's UUID
	 * @return is VIP
	 */
	public boolean isVip(String UUID) {
		return (lifetimeVIPs.contains(UUID) || temporaryVIPs.containsKey(UUID)); //Check if in Map or Set
	}
	
	/**
	 * Check if a player is lifetime VIP
	 * @param UUID The player's UUID
	 * @return is lifetime VIP
	 */
	public boolean isLifetimeVIP(String UUID) {
		return (lifetimeVIPs.contains(UUID)); //Check if lifetime VIP
	}
	
	/**
	 * Get the time when the player's VIP ends.
	 * Will return -1 if the player is lifetime VIP.
	 * @param UUID The player's UUID
	 * @return The system time when the player's VIP ends or -1 if the player is lifetime VIP
	 * @throws NotVIPException if the player is not VIP
	 */
	public long getVIPExpiration(String UUID) throws NotVIPException{
		if (lifetimeVIPs.contains(UUID)) {
			return -1L;
		} else if (temporaryVIPs.containsKey(UUID)) {
			return temporaryVIPs.get(UUID);
		} else {
			throw new NotVIPException(UUID);
		}
	}
	
	
	/*
	 * Add/Set/Remove functions
	 */
	
	
	/**
	 * Add the specified number of VIP days to the player
	 * @param UUID The player's UUID
	 * @param days The number of days
	 * @return rank changed
	 * @throws AlreadyVIPException if the player is already a lifetime VIP
	 */
	public boolean addVipDays(String UUID, int days) throws AlreadyVIPException {
		long vipEnds;
		if (lifetimeVIPs.contains(UUID)) { //Player is lifetime VIP
			throw new AlreadyVIPException(true);
		} else if (temporaryVIPs.containsKey(UUID)) { //Player is temporary VIP
			vipEnds = temporaryVIPs.get(UUID); //Get current end
			if (vipEnds < System.currentTimeMillis()) { //If end is in the past, update current end to now. 
				vipEnds = System.currentTimeMillis();
			}
			vipEnds += daysToMilliseconds(days); //Add days
			temporaryVIPs.put(UUID, vipEnds); //Update in map
		} else { //Player not VIP yet
			vipEnds = System.currentTimeMillis() + daysToMilliseconds(days); //Calculate new end time
			temporaryVIPs.put(UUID, vipEnds); //Put in map
		}
		updateVipTime(UUID, vipEnds, false); //Update VIP time
		return checkRank(UUID); //Check the rank
	}
	
	/**
	 * Set the number of VIP days for a player
	 * @param UUID The player's UUID
	 * @param days The number of days
	 * @return rank changed
	 */
	public boolean setVipDays(String UUID, int days) {
		if (lifetimeVIPs.contains(UUID)) { //If currently lifetime VIP, remove
			lifetimeVIPs.remove(UUID);
		}
		long vipEnds = System.currentTimeMillis() + daysToMilliseconds(days); //New Vip ends time
		temporaryVIPs.put(UUID, vipEnds); //Put in temp vip
		updateVipTime(UUID, vipEnds, false); //Update VIP time
		return checkRank(UUID); //Check the rank
	}
	
	/**
	 * Give the player lifetime VIP
	 * @param UUID The player's UUID
	 * @return rank changed
	 * @throws AlreadyVIPException if the player is already lifetime VIP
	 */
	public boolean setLifetimeVIP(String UUID) throws AlreadyVIPException {
		if (lifetimeVIPs.contains(UUID)) { //Check if already VIP
			throw new AlreadyVIPException(true);
		}
		if (temporaryVIPs.containsKey(UUID)) { //Check if a temporary VIP
			temporaryVIPs.remove(UUID);
		}
		lifetimeVIPs.add(UUID); //Add to lifetime map
		updateVipTime(UUID, null, true); //Update VIP time to Lifetime
		return checkRank(UUID); //Check the rank
	}
	
	/**
	 * Remove the player's VIP
	 * @param UUID The player's UUID
	 * @return Rank changed
	 * @throws NotVIPException if player is not a VIP
	 */
	public boolean removeVIP(String UUID) throws NotVIPException {
		if (lifetimeVIPs.contains(UUID)) { //Check if lifetime VIP
			lifetimeVIPs.remove(UUID);
		} else if (temporaryVIPs.containsKey(UUID)) { //Check if temporary VIP
			temporaryVIPs.remove(UUID);
		} else { //Not a VIP
			throw new NotVIPException(UUID);
		}
		removeVipFromDB(UUID); //Remove VIP from a player
		return checkRank(UUID); //Check the rank
	}
	
	/**
	 * Convert the days into milliseconds
	 * @param days The days
	 * @return milliseconds
	 */
	private long daysToMilliseconds(int days) {
		return ((long) days) * 24L * 60L * 60L * 1000L;
	}
		
	/**
	 * Check if the player's rank is still correct.
	 * This will promote/demote the player if needed, based on their VIP status.
	 * This will also log the promotion/demotion, if there was any.
	 * @param UUID The player's UUID
	 * @param logChange [0] = Log the change in the DB (Standard true), [1] = Change group in TAB (Standard true)
	 * @return rank changed
	 */
	public boolean checkRank(String UUID, boolean... logChange) {
		PermissionUser user = PermissionsEx.getPermissionManager().getUser(UUID); //Get player
		if (user == null) { //Check if not null.
			Log.warn("Tried to check rank for " + UUID + ", but User was null.");
			return false;
		}
		
		boolean 
			log = true, //Log changes
			tabGroup = true; //Update group in TAB
		if (logChange.length > 0) { //If DB given
			log = logChange[0];
			if (logChange.length > 1) { //If tabgroup given
				tabGroup = logChange[1];
			}
		}
				
		String groupname = user.getGroupsNames()[0]; //Get groupname
		boolean isVIP = isVip(UUID); //Check if VIP
		
		switch(groupname.toLowerCase()) { //Switch group
		case "builder": case "member": //Player is Builder or Member
			if (isVIP) { //if VIP
				user.setGroups(getPermissionGroup("VIP")); //Promote to VIP
				VipForumControl.logForumPromotion(user.getName(), true); //Log ForumPromotion
			}
			break;
		case "guide": //Player is Guide
			if (isVIP) { //if VIP
				user.setGroups(getPermissionGroup("VIPGuide")); //Promote to VIPGuide
			}
			break;
		case "vip": //Player is a VIP
			if (!isVIP) { //if not VIP anymore
				user.setGroups(getPermissionGroup("Member")); //Demote to Member
				VipForumControl.logForumPromotion(user.getName(), false); //Log ForumPromotion
			}
			break;
		case "vipguide": //Player is a VIP Guide
			if (!isVIP) { //If not VIP anymore
				user.setGroups(getPermissionGroup("Guide")); //Demote to Guide
			}
			break;
			//If different rank -> Staff. Do not change the rank.
		}
		String newGroupname = user.getGroupsNames()[0]; //Get new groupname
		if (groupname.equalsIgnoreCase(newGroupname)) { //Group is still the same
			return false;
		} else { //Group changed
			if (log) { //Log in DB
				PromotionLogger.logRankChange(UUID, groupname, newGroupname, !isVIP, "Rank Check"); //Log the rank change
			}
			if (tabGroup) { //Change Tab Group
				Player onlinePlayer = Bukkit.getPlayer(java.util.UUID.fromString(UUID));
				if (onlinePlayer != null) { //If player is online
					tabController.playerSwitchGroup(onlinePlayer); //Switch group in TAB
				}
			}
			return true;
		}
	}
	
	/**
	 * Get a permission group based on its name.
	 * This will be packed into an array. This array only contains the single specified group.
	 * @param groupname The name of the group
	 * @return The array containing the group
	 */
	private PermissionGroup[] getPermissionGroup(String groupname) {
		return new PermissionGroup[]{PermissionsEx.getPermissionManager().getGroup(groupname)};
	}
	
	@Override
	public void shutdown() {
		temporaryVIPs.clear();
		lifetimeVIPs.clear();
		setup = false;
	}
	
	
	
	/*
	 ************* 
	 * SQL Stuff *
	 *************
	 */
	/**
	 * Create the table in the SQL Database
	 * @return succes
	 */
	private boolean createTable() {
        return true;
	}
	
	/**
	 * Insert/Update the VIP time for a player in the DB
	 * @param UUID The player's UUID
	 * @param tillTime The time when it ends, or null if lifetime
	 * @param lifetime is lifetime
	 */
	private void updateVipTime(final String UUID, final Long tillTime, final boolean lifetime) {
		Util.runASync(new Runnable() {
			@Override
			public void run() {
				Connection con = SQLPool.getConnection();
				try {
					PreparedStatement prep = con.prepareStatement(
						"INSERT INTO `sh_vip_time` (`user_id`, `till_time`, `lifetime`) VALUES (?, ?, ?) " +
						"ON DUPLICATE KEY UPDATE `till_time` = ?, `lifetime` = ?;"
					);
					//Insert
					prep.setInt(1, UUIDControl.getInstance().getUUIDProfile(UUID).getUserID());
					if (lifetime) {
						prep.setNull(2, java.sql.Types.BIGINT);
					} else {
						prep.setLong(2, tillTime);
					}
					prep.setBoolean(3, lifetime);
					
					//Update
					if (lifetime) {
						prep.setNull(4, java.sql.Types.BIGINT);
					} else {
						prep.setLong(4, tillTime);
					}
					prep.setBoolean(5, lifetime);
					
					//Execute
					prep.executeUpdate();
				} catch (SQLException e) {
					e.printStackTrace();
					plugin.getMail().sendConsoleMail(Bukkit.getConsoleSender(), "Stoux2", "Warning! Failed to update vip time. Player: " + UUID + " | Till: " + tillTime + " | Lifetime: " + lifetime);
				} finally {
					SQLPool.returnConnection(con);
				}
			}
		});
	}

	/**
	 * Remove VIP from a player
	 * @param UUID The player's UUID
	 */
	private void removeVipFromDB(final String UUID) {
		Util.runASync(new Runnable() {
			@Override
			public void run() {
				Connection con = SQLPool.getConnection(); //Get connection
				try {
					PreparedStatement prep = con.prepareStatement("DELETE FROM `sh_vip_time` WHERE `user_id` = ?"); //Prep statement
					prep.setInt(1, UUIDControl.getInstance().getUUIDProfile(UUID).getUserID()); //Set name
					prep.executeUpdate(); //Execute update
				} catch (SQLException e) {
					e.printStackTrace();
					plugin.getMail().sendConsoleMail(Bukkit.getConsoleSender(), "Stoux2", "Warning! Failed to remove VIP. Player: " + UUID); //Warn Stoux if failed
				} finally {
					SQLPool.returnConnection(con); //Return the connection
				}
			}
		});
	}
	
	
	/*
	 ************** 
	 * Used Grant *
	 **************
	 */
	
	/**
	 * Check if the Used Grant should be reset.
	 */
	private void checkUsedGrants() {
		int day = Integer.parseInt(DateUtil.format("dd")); //Parse today into day
		int founDay = config.getInt("day"); //Get saved day
		if (day != founDay) { //If not equal, reset
			config.set("usedgrant", null); //Reset all uses
			config.set("day", day); //Set new day
			storage.saveConfig(); //Save
		}
	}
	
	/**
	 * Get VIP Grant uses left
	 * @param UUID The player's UUID
	 * @return number of uses left
	 * @throws NotVIPException if player is not a VIP
	 */
	public int getVipGrantUsesLeft(String UUID) throws NotVIPException {
		if (!isVip(UUID)) throw new NotVIPException();
		return 3 - config.getInt("usedgrant." + Util.sanitizeYamlString(UUID));
	}
	
	/**
	 * Use a VIP Grant
	 * @param UUID The player's UUID
	 * @return number of uses left
	 * @throws NotVIPException if player is not a VIP
	 */
	public int useVipGrant(String UUID) throws NotVIPException {
		if (!isVip(UUID)) throw new NotVIPException(); //Check if VIP
        String sanitizedUUID = Util.sanitizeYamlString(UUID);
		int uses = config.getInt("usedgrant." + sanitizedUUID); //Get number of uses
		config.set("usedgrant." + sanitizedUUID, ++uses); //Increate the use
		return 3 - uses; //Return number of uses left
	}

}
