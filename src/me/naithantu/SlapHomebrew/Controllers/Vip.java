package me.naithantu.SlapHomebrew.Controllers;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import me.naithantu.SlapHomebrew.Commands.Exception.AlreadyVIPException;
import me.naithantu.SlapHomebrew.Commands.Exception.NotVIPException;
import me.naithantu.SlapHomebrew.Controllers.PlayerLogging.PromotionLogger;
import me.naithantu.SlapHomebrew.Storage.YamlStorage;
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
	 * K:[Name of Player] => V:[Time (in milliseconds) when the VIP ends]
	 */
	private ConcurrentHashMap<String, Long> temporaryVIPs;
	
	/**
	 * HashSet containing all the lifetime players
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
	 * Format for when a players VIP ends
	 */
	private SimpleDateFormat format;
	
	/**
	 * Storage & Config for Used Grants
	 */
	private YamlStorage storage;
	private FileConfiguration config;
	
	public Vip(TabController tabController) {
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
		
		//Create format
		format = new SimpleDateFormat("dd MMM. yyyy HH:mm zzz");
		
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
			ResultSet rs = con.createStatement().executeQuery("SELECT `player`, `till_time`, `lifetime` FROM `vip_time`;"); //Get All VIPs from DB
			while (rs.next()) {
				//Get stuff from ResultSet
				String playername = rs.getString(1);
				Long tillTime = rs.getLong(2);
				boolean lifetime = rs.getBoolean(3);
				
				if (lifetime) { //If lifetime VIP
					lifetimeVIPs.add(playername); //Add to lifetime VIP set
				} else { //If not VIP
					temporaryVIPs.put(playername, tillTime); //Add to Temporary map
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
		Util.runASyncTimer(plugin, new Runnable() {
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
	 * @param playername The player
	 * @return is VIP
	 */
	public boolean isVip(String playername) {
		playername = playername.toLowerCase(); //To LC
		return (lifetimeVIPs.contains(playername) || temporaryVIPs.containsKey(playername)); //Check if in Map or Set
	}
	
	/**
	 * Check if a player is lifetime VIP
	 * @param playername The player
	 * @return is lifetime VIP
	 */
	public boolean isLifetimeVIP(String playername) {
		playername = playername.toLowerCase(); //To LC
		return (lifetimeVIPs.contains(playername)); //Check if lifetime VIP
	}
	
	/**
	 * Get the time when the player's VIP ends.
	 * Will return -1 if the player is lifetime VIP.
	 * @param playername The player
	 * @return The system time when the player's VIP ends or -1 if the player is lifetime VIP
	 * @throws NotVIPException if the player is not VIP
	 */
	public long getVIPExpiration(String playername) throws NotVIPException{
		playername = playername.toLowerCase(); //To LC
		if (lifetimeVIPs.contains(playername)) {
			return -1L;
		} else if (temporaryVIPs.containsKey(playername)) {
			return temporaryVIPs.get(playername);
		} else {
			throw new NotVIPException(playername);
		}
	}
	
	
	/*
	 * Add/Set/Remove functions
	 */
	
	
	/**
	 * Add the specified number of VIP days to the player
	 * @param playername The player
	 * @param days The number of days
	 * @return rank changed
	 * @throws AlreadyVIPException if the player is already a lifetime VIP
	 */
	public boolean addVipDays(String playername, int days) throws AlreadyVIPException {
		playername = playername.toLowerCase(); //To LC
		long vipEnds;
		if (lifetimeVIPs.contains(playername)) { //Player is lifetime VIP
			throw new AlreadyVIPException(true);
		} else if (temporaryVIPs.containsKey(playername)) { //Player is temporary VIP 
			vipEnds = temporaryVIPs.get(playername); //Get current end
			if (vipEnds < System.currentTimeMillis()) { //If end is in the past, update current end to now. 
				vipEnds = System.currentTimeMillis();
			}
			vipEnds += daysToMilliseconds(days); //Add days
			temporaryVIPs.put(playername, vipEnds); //Update in map
		} else { //Player not VIP yet
			vipEnds = System.currentTimeMillis() + daysToMilliseconds(days); //Calculate new end time
			temporaryVIPs.put(playername, vipEnds); //Put in map
		}
		updateVipTime(playername, vipEnds, false); //Update VIP time
		return checkRank(Util.getOfflinePlayer(playername)); //Check the rank
	}
	
	/**
	 * Set the number of VIP days for a player
	 * @param playername The player
	 * @param days The number of days
	 * @return rank changed
	 */
	public boolean setVipDays(String playername, int days) {
		playername = playername.toLowerCase(); //To LC
		if (lifetimeVIPs.contains(playername)) { //If currently lifetime VIP, remove
			lifetimeVIPs.remove(playername);
		}
		long vipEnds = System.currentTimeMillis() + daysToMilliseconds(days); //New Vip ends time
		temporaryVIPs.put(playername, vipEnds); //Put in temp vip
		updateVipTime(playername, vipEnds, false); //Update VIP time
		return checkRank(Util.getOfflinePlayer(playername)); //Check the rank
	}
	
	/**
	 * Give the player lifetime VIP
	 * @param playername The player
	 * @return rank changed
	 * @throws AlreadyVIPException if the player is already lifetime VIP
	 */
	public boolean setLifetimeVIP(String playername) throws AlreadyVIPException {
		playername = playername.toLowerCase(); //To LC
		if (lifetimeVIPs.contains(playername)) { //Check if already VIP
			throw new AlreadyVIPException(true);
		}
		if (temporaryVIPs.containsKey(playername)) { //Check if a temporary VIP
			temporaryVIPs.remove(playername);
		}
		lifetimeVIPs.add(playername); //Add to lifetime map
		updateVipTime(playername, null, true); //Update VIP time to Lifetime
		return checkRank(Util.getOfflinePlayer(playername)); //Check the rank
	}
	
	/**
	 * Remove the player's VIP
	 * @param playername The player
	 * @return Rank changed
	 * @throws NotVIPException if player is not a VIP
	 */
	public boolean removeVIP(String playername) throws NotVIPException {
		playername = playername.toLowerCase(); //To LC1
		if (lifetimeVIPs.contains(playername)) { //Check if lifetime VIP
			lifetimeVIPs.remove(playername);
		} else if (temporaryVIPs.containsKey(playername)) { //Check if temporary VIP
			temporaryVIPs.remove(playername);
		} else { //Not a VIP
			throw new NotVIPException(playername);
		}
		removeVipFromDB(playername); //Remove VIP from a player
		return checkRank(Util.getOfflinePlayer(playername)); //Check the rank
	}
	
	/**
	 * Convert the days into milliseconds
	 * @param days The days
	 * @return milliseconds
	 */
	private long daysToMilliseconds(int days) {
		return (days * 24 * 60 * 60 * 1000);
	}
		
	/**
	 * Check if the player's rank is still correct.
	 * This will promote/demote the player if needed, based on their VIP status.
	 * This will also log the promotion/demotion, if there was any.
	 * @param p The player.
	 * @param log [0] = Log the change in the DB (Standard true), [1] = Change group in TAB (Standard true)
	 * @return rank changed
	 */
	public boolean checkRank(OfflinePlayer p, boolean... logChange) {
		String playername = p.getName().toLowerCase();
		PermissionUser user = PermissionsEx.getUser(playername); //Get player
		if (user == null) { //Check if not null.
			Log.warn("Tried to check rank for " + playername + ", but User was null.");
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
		boolean isVIP = isVip(playername); //Check if VIP
		
		switch(groupname.toLowerCase()) { //Switch group
		case "builder": case "member": //Player is Builder or Member
			if (isVIP) { //if VIP
				user.setGroups(getPermissionGroup("VIP")); //Promote to VIP
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
				PromotionLogger.logRankChange(p.getName(), groupname, newGroupname, !isVIP, "Rank Check"); //Log the rank change
			}
			if (tabGroup) { //Change Tab Group
				Player onlinePlayer = p.getPlayer();
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
	
	/**
	 * Get SimpleDateFormat format.
	 * Format: Day Month Year Hour:Minute
	 * @return The format
	 */
	public SimpleDateFormat getFormat() {
		return format;
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
		Connection con = SQLPool.getConnection();
		try {
			con.createStatement().executeUpdate(
				"CREATE TABLE IF NOT EXISTS `vip_time` ( " +
					"`player` varchar(20) NOT NULL, " +
					"`till_time` bigint(20) DEFAULT NULL, " +
					"`lifetime` tinyint(1) NOT NULL, " +
				"PRIMARY KEY (`player`) " +
				") ENGINE=InnoDB DEFAULT CHARSET=latin1;");
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		} finally {
			SQLPool.returnConnection(con);
		}
	}
	
	/**
	 * Insert/Update the VIP time for a player in the DB
	 * @param playername The player
	 * @param tillTime The time when it ends, or null if lifetime
	 * @param lifetime is lifetime
	 */
	private void updateVipTime(final String playername, final Long tillTime, final boolean lifetime) {
		Util.runASync(plugin, new Runnable() {
			@Override
			public void run() {
				Connection con = SQLPool.getConnection();
				try {
					PreparedStatement prep = con.prepareStatement(
						"INSERT INTO `mcecon`.`vip_time` (`player`, `till_time`, `lifetime`) VALUES (?, ?, ?) " +
						"ON DUPLICATE KEY UPDATE `till_time` = ?, `lifetime` = ?;"
					);
					//Insert
					prep.setString(1, playername);
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
					plugin.getMail().sendConsoleMail(Bukkit.getConsoleSender(), "Stoux2", "Warning! Failed to update vip time. Player: " + playername + " | Till: " + tillTime + " | Lifetime: " + lifetime);
				} finally {
					SQLPool.returnConnection(con);
				}
			}
		});
	}

	/**
	 * Remove VIP from a player
	 * @param playername The player
	 */
	private void removeVipFromDB(final String playername) {
		Util.runASync(plugin, new Runnable() {
			@Override
			public void run() {
				Connection con = SQLPool.getConnection(); //Get connection
				try {
					PreparedStatement prep = con.prepareStatement("DELETE FROM `mcecon`.`vip_time` WHERE `vip_time`.`player` = ?"); //Prep statement
					prep.setString(1, playername); //Set name
					prep.executeUpdate(); //Execute update
				} catch (SQLException e) {
					e.printStackTrace();
					plugin.getMail().sendConsoleMail(Bukkit.getConsoleSender(), "Stoux2", "Warning! Failed to remove VIP. Player: " + playername); //Warn Stoux if failed
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
		SimpleDateFormat format = new SimpleDateFormat("dd");
		int day = Integer.parseInt(format.format(System.currentTimeMillis())); //Parse today into day
		int founDay = config.getInt("day"); //Get saved day
		if (day != founDay) { //If not equal, reset
			config.set("usedgrant", null); //Reset all uses
			config.set("day", day); //Set new day
			storage.saveConfig(); //Save
		}
	}
	
	/**
	 * Get VIP Grant uses left
	 * @param playername The player
	 * @return number of uses left
	 * @throws NotVIPException if player is not a VIP
	 */
	public int getVipGrantUsesLeft(String playername) throws NotVIPException {
		playername = playername.toLowerCase();
		if (!isVip(playername)) throw new NotVIPException();
		return 3 - config.getInt("usedgrant." + playername);
	}
	
	/**
	 * Use a VIP Grant
	 * @param playername The player
	 * @return number of uses left
	 * @throws NotVIPException if player is not a VIP
	 */
	public int useVipGrant(String playername) throws NotVIPException {
		playername = playername.toLowerCase(); //to LC
		if (!isVip(playername)) throw new NotVIPException(); //Check if VIP
		int uses = config.getInt("usedgrant." + playername); //Get number of uses
		config.set("usedgrant." + playername, ++uses); //Increate the use
		return 3 - uses; //Return number of uses left
	}

}
