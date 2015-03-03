package me.naithantu.SlapHomebrew.Controllers;

import me.naithantu.SlapHomebrew.Commands.Exception.CommandException;
import me.naithantu.SlapHomebrew.Commands.Exception.StateException;
import me.naithantu.SlapHomebrew.Storage.YamlStorage;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.HashSet;
import java.util.Set;

public class Mention extends AbstractController {

	private YamlStorage storage;
	private FileConfiguration config;
	
	private HashSet<String> banned; //Anyone in this set will not be able to do @
	private HashSet<String> noSound; //Anyone in this set will not recieve Ping/Pop/Ding noise
	
	public Mention() {
		//Get storage
		storage = new YamlStorage(plugin, "mention");
		config = storage.getConfig();
		
		//Create sets
		banned = new HashSet<>();
		noSound = new HashSet<>();
		
		//Fill sets
		Set<String> players = config.getKeys(false);
		for (String player : players) {
			if (config.getBoolean(player + ".isbanned")) { //Check if banned
				banned.add(player); 
			}
			if (config.getBoolean(player + ".hassoundoff")) {
				noSound.add(player);
			}
		}		
	}
	
	/**
	 * Check if a player is banned from using @Mention
	 * @param playername The player
	 * @return is Banned
	 */
	public boolean isBanned(String playername) {
		playername = playername.toLowerCase();
		return banned.contains(playername);
	}
		
	/**
	 * Check if a player has annoying ding pop sound off
	 * @param playername The player
	 * @return has sound off
	 */
	public boolean hasSoundOff(String playername) {
		playername = playername.toLowerCase();
		return noSound.contains(playername);
	}
	
	/**
	 * Ban a player from using @Mention
	 * @param playername The player
	 * @param ban ban
	 * @throws CommandException 
	 */
	public void setBanned(String playername, boolean ban) throws CommandException {
		playername = playername.toLowerCase(); //To LowerCase
		boolean isBanned = banned.contains(playername); //CHeck if player is in set. 
		if (isBanned == ban) { //Already in that state
			throw new StateException(ban);
		}
		
		if (ban) { //Ban the player
			banned.add(playername);
			config.set(playername + ".isbanned", true);
		} else { //Unban
			banned.remove(playername);
			config.set(playername + ".isbanned", null);
		}
		storage.saveConfig();
	}
		
	/**
	 * Set the notification sound on or off for a player
	 * @param playername The player
	 * @param on Sound on or off 
	 * @throws CommandException if already on that state
	 */
	public void setSound(String playername, boolean on) throws CommandException {
		playername = playername.toLowerCase(); //To LowerCase
		boolean currentlyOn = !noSound.contains(playername); //CHeck if player is in set. 
		if (currentlyOn == on) { //Already in that state
			throw new StateException(on);
		}
		if (on) { //Turn it on
			noSound.remove(playername);
			config.set(playername + ".hassoundoff", null);
		} else { //Turn it off
			noSound.add(playername);
			config.set(playername + ".hassoundoff", true);
		}
		storage.saveConfig();
	}
	

	@Override
	public void shutdown() {
		
	}

}
