package me.naithantu.SlapHomebrew.Controllers;

import java.util.HashMap;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

import com.earth2me.essentials.User;

import ru.tehkode.permissions.PermissionGroup;
import ru.tehkode.permissions.PermissionUser;
import ru.tehkode.permissions.bukkit.PermissionsEx;

import me.naithantu.SlapHomebrew.Commands.Exception.CommandException;
import me.naithantu.SlapHomebrew.Commands.Exception.HomeException;
import me.naithantu.SlapHomebrew.Storage.YamlStorage;

public class Homes extends AbstractController {

	/**
	 * HashMap containing players who have extra homes
	 * K:[Name of Player] => V:[Number of extra homes]
	 */
	private HashMap<String, Integer> boughtHomes;
	
	/**
	 * HashMap containing all groups & how many homes that group has acces to.
	 * K:[Name of group] => V:[Default number of homes]
	 */
	private HashMap<String, Integer> defaultNumberOfHomes;
	
	/**
	 * HashMap containing all logged in Essentials users
	 * K:[Name of player] => V:[Essentials user]
	 */
	private HashMap<String, User> essentialsUsers;
	
	/**
	 * Yaml Storage & Config
	 * Contains settings for homes.
	 */
	private YamlStorage yaml;
	private FileConfiguration config;
	
	public Homes() {
		//Create maps
		boughtHomes = new HashMap<>();
		defaultNumberOfHomes = new HashMap<>();
		essentialsUsers = new HashMap<>();
		
		//Get storage
		yaml = new YamlStorage(plugin, "homesettings");
		config = yaml.getConfig();
		
		//Set Default number of homes for each group.
		setDefaultNumberOfHomesForGroup(new String[]{"builder"}, 1); //Builders
		setDefaultNumberOfHomesForGroup(new String[]{"Member", "Slap", "Guide"}, 5); //Standard members
		setDefaultNumberOfHomesForGroup(new String[]{"VIP", "VIPGuide"}, 10); //VIPs
		setDefaultNumberOfHomesForGroup(new String[]{"Mod", "Admin"}, 20); //Staff
		setDefaultNumberOfHomesForGroup(new String[]{"SuperAdmin"}, 9001); //All perms
		
		//Save any changes
		yaml.saveConfig();
		
		//Get the bought homes from the config
		getBoughtHomes();
	}
	
	/**
	 * Set the default number of homes for a group.
	 * This will try to get the default number of homes from the config. If it's not in the config it will take the specified default number.
	 * @param groupnames
	 * @param defaultNr
	 */
	private void setDefaultNumberOfHomesForGroup(String[] groupnames, int defaultNr) {
		for (String groupname : groupnames) {
			String path = "defaulthomes." + groupname;
			if (config.contains(path)) { //If specified in config
				defaultNumberOfHomes.put(groupname, config.getInt(path)); //Get from config
			} else { //Set new default
				defaultNumberOfHomes.put(groupname, defaultNr);
				config.set(path, defaultNr);
			}
		}
	}
	
	/**
	 * Change the default number of homes for a group.
	 * @param groupname The name of the group
	 * @param defaultNr The default number of homes
	 * @return is valid group & has changed.
	 */
	public boolean changeDefaultNumberOfHomesForGroup(String groupname, int defaultNr) {
		PermissionGroup group = PermissionsEx.getPermissionManager().getGroup(groupname); //Get group
		if (group == null) return false; //Check if group exists
		groupname = group.getName(); //Set groupname to correct one (capital sensitive).
		//Set stuff
		config.set("defaulthomes." + groupname, defaultNr);
		defaultNumberOfHomes.put(groupname, defaultNr);
		yaml.saveConfig();
		return true;
	}
	
	/**
	 * A player joins the server
	 * @param p The player
	 */
	public void playerJoin(Player p) {
		getEssentialsUser(p.getName(), true); //Put the player in the Essentials map
	}
	
	/**
	 * A player quits the server
	 * @param p The player
	 */
	public void playerQuit(Player p) {
		essentialsUsers.remove(p.getName());
	}
	
	/*
	 ********************
	 * Getters/Checkers *
	 ********************
	 */
	
	/**
	 * Get the number of allowed homes for a player
	 * @param playername The player
	 * @return number of homes
	 */
	public int getTotalNumberOfHomes(String playername) {
		return getDefaultNumberOfHomes(playername) + getNumberOfBoughtHomes(playername);
	}
	
	/**
	 * Get the default number of homes for a player
	 * @param playername The player
	 * @return default number of homes
	 */
	private int getDefaultNumberOfHomes(String playername) {
		PermissionUser user = PermissionsEx.getUser(playername); //Get user
		if (user == null) return 0; //If user is null, return 0
		return defaultNumberOfHomes.get(user.getGroups()[0].getName()); //Get DefaultNumber
	}
	
	/**
	 * Add extra homes to a player
	 * @param playername The player
	 * @param homes The number of homes
	 */
	public void addHomesToPlayer(String playername, int homes) {
		String pLc = playername.toLowerCase(); //To LC
		if (boughtHomes.containsKey(pLc)) { //Check if already homes
			homes += boughtHomes.get(pLc); //Get homes
		}
		boughtHomes.put(pLc, homes); //Put in Map
		config.set("boughthome." + pLc, homes); //Set in config
		yaml.saveConfig();
	}
	
	/**
	 * Remove extra bought homes from a player
	 * @param playername The player
	 * @param homes The number of homes
	 * @throws CommandException if no extra homes bought or trying to remove more homes than bought
	 */
	public void removeHomesFromPlayer(String playername, int homes) throws CommandException {
		String pLc = playername.toLowerCase();
		if (!boughtHomes.containsKey(pLc)) { //Check if bought any homes
			throw new CommandException("This player hasn't bought any homes!");
		}
		int currentHomes = getNumberOfBoughtHomes(playername); //Get number of bought homes
		if (currentHomes < homes) { //If trying to remove more homes than bought
			throw new CommandException("The player has only bought " + currentHomes + (currentHomes == 1 ? " home." : " homes."));
		}
		currentHomes -= homes;
		if (currentHomes == 0) {
			boughtHomes.remove(pLc); //Remove from map
			config.set("boughthome." + pLc, null); //Remove from config
		} else {
			boughtHomes.put(pLc, currentHomes); //Set new amount
			config.set("boughthome." + pLc, currentHomes); //Set in config
		}
		yaml.saveConfig();
	}
	
	/**
	 * Get number of homes the player has bought
	 * @param playername The player
	 * @return number of homes
	 */
	public int getNumberOfBoughtHomes(String playername) {
		String pLc = playername.toLowerCase();
		if (boughtHomes.containsKey(pLc)) {
			return boughtHomes.get(pLc);
		} else {
			return 0;
		}
	}
	
	/**
	 * Get all boughthomes from the config
	 */
	private void getBoughtHomes() {
		if (config.contains("boughthome")) { //See if the config contains the boughthomes section
			ConfigurationSection boughtConfig = config.getConfigurationSection("boughthome"); //Get the section
			for (String key : boughtConfig.getKeys(false)) { //Loop thru all players
				boughtHomes.put(key, boughtConfig.getInt(key)); //Put the player in the map
			}
		}
	}
	
	/*
	 ****************
	 * Home methods *
	 ****************
	 */
	
	/**
	 * Get all homes of a player
	 * @param playername The player
	 * @return The list of homes
	 * @throws CommandException if User is null
	 */
	public List<String> getHomes(String playername) throws CommandException {
		User u = getEssentialsUser(playername);
		return u.getHomes();
	}
	
	/**
	 * Teleport a player to a location (using Essentials /back)
	 * @param p The player
	 * @param loc The location
	 * @throws CommandException if user not found or failed to teleport
	 */
	public void teleportToLocation(Player p, Location loc) throws CommandException {
		User user = getEssentialsUser(p.getName());
		try {
			user.getTeleport().teleport(p, null, TeleportCause.COMMAND);
		} catch (Exception e) {
			throw new CommandException(e.getMessage());
		}
		
	}
	
	/**
	 * Teleport a player to a home (using Essentials /back)
	 * @param p The player
	 * @param home The name of the home
	 * @throws CommandException if no home with that name found
	 */
	public void teleportToHome(Player p, String home) throws CommandException {
		Location loc = getHome(p.getName(), home);
		teleportToLocation(p, loc);
	}
	
	/**
	 * Get the location of a home of a Player
	 * @param playername The Player
	 * @param home The name of the home
	 * @return The location of the home
	 * @throws CommandException if user is null or no home with this name
	 */
	public Location getHome(String playername, String home) throws CommandException {
		User u = getEssentialsUser(playername);
		try {
			Location loc = u.getHome(home);
			if (loc == null) {
				throw new HomeException(home, u.getHomes());
			}
			return loc;
		} catch (Exception e) {
			throw new CommandException(e.getMessage());
		}
	}
	
	/**
	 * Set a home at the given location, with the given name for the specified player
	 * @param playername The player
	 * @param home The name of the home
	 * @param loc The location of the home
	 * @throws CommandException user is null
	 */
	public void setHome(String playername, String home, Location loc) throws CommandException {
		User u = getEssentialsUser(playername);
		u.setHome(home, loc);
	}
	
	/**
	 * Delete a home that belongs to a player
	 * @param playername The palyer
	 * @param home The name of the home
	 * @throws CommandException if user is null or no home with this name
	 */
	public void deleteHome(String playername, String home) throws CommandException {
		User u = getEssentialsUser(playername);
		try {
			u.delHome(home);
		} catch (Exception e) {
			throw new HomeException(home, u.getHomes());
		}
	}
	
	/**
	 * Get the Essentials user based on their playername
	 * @param playername The player
	 * @return the user or null if not found
	 * @throws CommandException if user is null
	 */
	private User getEssentialsUser(String playername) throws CommandException {
		User u;
		if (essentialsUsers.containsKey(playername)) {
			u = essentialsUsers.get(playername);
		} else {
			u = getEssentialsUser(playername, false);
		}
		if (u == null) throw new CommandException("User not found.");
		return u;
	}
	
	/**
	 * Get the Essentials user based on their playername
	 * @param playername The player
	 * @param putInMap Put the found user in the essentials map
	 * @return The user or null if not found
	 */
	private User getEssentialsUser(String playername, boolean putInMap) {
		User u = plugin.getEssentials().getUserMap().getUser(playername);
		if (u != null && putInMap) { //Put in map if needed & possible
			essentialsUsers.put(u.getName(), u);
		}
		return u;
	}
	
	

	@Override
	public void shutdown() {
		// TODO Auto-generated method stub

	}

}
