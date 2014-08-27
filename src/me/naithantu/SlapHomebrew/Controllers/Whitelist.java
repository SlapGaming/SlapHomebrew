package me.naithantu.SlapHomebrew.Controllers;

import java.util.ArrayList;
import java.util.HashSet;

import org.bukkit.configuration.file.FileConfiguration;

import me.naithantu.SlapHomebrew.Commands.Exception.CommandException;
import me.naithantu.SlapHomebrew.Storage.YamlStorage;
import me.naithantu.SlapHomebrew.Util.Log;

public class Whitelist extends AbstractController {

	//Config
	private YamlStorage yaml;
	private FileConfiguration config;
	
	private boolean whitelistOn;
	private HashSet<String> allowedPlayers;
	private String whitelistMessage;
	
	public Whitelist() {
		//Get config
		yaml = new YamlStorage(plugin, "whitelist");
		config = yaml.getConfig();
		
		if (config.contains("allowedplayers")) { //If config contains list
			allowedPlayers = new HashSet<>(config.getStringList("allowedplayers"));
		} else { //New set
			allowedPlayers = new HashSet<>();
		}
		
		if (config.contains("whiteliston")) { //If config contains whitelist on
			whitelistOn = config.getBoolean("whiteliston"); //Get current status
		} else { //Standard is off
			turnWhitelist(false);
		}
		
		if (config.contains("whitelistmessage")) { //Check if whitelist message specified
			whitelistMessage = config.getString("whitelistmessage"); //Get message
		} else { //Standard message
			setWhitelistMessage("SlapGaming is currently under maintenance, try again later!");
		}
	}
	
	/**
	 * Turn the whitelist on or off
	 * @param on [True = on] [False = off]
	 */
	public void turnWhitelist(boolean on) {
		whitelistOn = on;
		config.set("whiteliston", on);
		Log.info("Turned whitelist: " + (on ? "on" : "off"));
		yaml.saveConfig();
	}
	
	/**
	 * Add a player to the whitelist
	 * @param UUID The player's UUID
	 * @throws CommandException if already added
	 */
	public void addPlayer(String UUID) throws CommandException {
		if (allowedPlayers.contains(UUID)) {
			throw new CommandException("This player is already added to the whitelist.");
		}
		allowedPlayers.add(UUID);
		savePlayers();
	}
		
	/**
	 * Remove a player from the whitelist
	 * @param UUID The player's UUID
	 * @throws CommandException if not added
	 */
	public void removePlayer(String UUID) throws CommandException {
		if (!allowedPlayers.contains(UUID)) {
			throw new CommandException("This player is not whitelisted.");
		}
		allowedPlayers.remove(UUID);
		savePlayers();
	}
	
	/**
	 * Check if a player is whitelisted
	 * @param UUID The player's UUID
	 * @return whitelisted
	 */
	public boolean isWhitelisted(String UUID) {
		return allowedPlayers.contains(UUID);
	}
	
	/**
	 * Check if the whitelist is on
	 * @return is on
	 */
	public boolean isWhitelistOn() {
		return whitelistOn;
	}
	
	/**
	 * Save the players in the config
	 */
	private void savePlayers() {
		config.set("allowedplayers", new ArrayList<String>(allowedPlayers));
		yaml.saveConfig();
	}
	
	/**
	 * Get all allowed players
	 * @return The players
	 */
	public HashSet<String> getAllowedPlayers() {
		return allowedPlayers;
	}
	
	/**
	 * Get the whitelist message
	 * @return the message
	 */
	public String getWhitelistMessage() {
		return whitelistMessage;
	}
	
	/**
	 * Set the whitelist message
	 * @param message message
	 */
	public void setWhitelistMessage(String message) {
		config.set("whitelistmessage", message);
		yaml.saveConfig();
	}
	
	@Override
	public void shutdown() {
	}

}
