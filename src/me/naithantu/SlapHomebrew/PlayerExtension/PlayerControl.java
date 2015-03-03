package me.naithantu.SlapHomebrew.PlayerExtension;

import me.naithantu.SlapHomebrew.Controllers.AbstractController;
import me.naithantu.SlapHomebrew.Util.Util;
import org.bukkit.entity.Player;

import java.util.HashMap;

public class PlayerControl extends AbstractController {

	//Singleton
	private static PlayerControl instance;
	
	/**
	 * HashMap containing online players.
	 * K:[Name of the player] => V:[SlapPlayer]
	 */
	private HashMap<String, SlapPlayer> nameToSlapPlayer;
	
	/**
	 * HashMap containing online players.
	 * K:[Player instance] => V:[SlapPlayer]
	 */
	private HashMap<Player, SlapPlayer> playerToSlapPlayer;
	
	
	public PlayerControl() {
		//Create maps
		nameToSlapPlayer = new HashMap<>();
		playerToSlapPlayer = new HashMap<>();
		
		//Set instance
		instance = this;
		
		//Get online players (if reloaded, even tho reload is horrible).
		getOnlinePlayers();
	}
	
	/**
	 * Load all online players
	 */
	private void getOnlinePlayers() {
		for (Player p : Util.getOnlinePlayers()) {
			addSlapPlayer(p);
		}
	}
	
	/**
	 * Add a SlapPlayer
	 * @param p The player
	 */
	public void addSlapPlayer(Player p) {
		//Create SlapPlayer
		SlapPlayer sp = new SlapPlayer(p);
		//Put in maps
		nameToSlapPlayer.put(p.getName(), sp);
		playerToSlapPlayer.put(p, sp);
	}
	
	/**
	 * Remove a SlapPlayer
	 * @param p The player
	 */
	public void removeSlapPlayer(Player p) {
		//Remove from maps
		nameToSlapPlayer.remove(p.getName());
		playerToSlapPlayer.remove(p);
	}
	
	/**
	 * Get PlayerControl instance
	 * @return the instance
	 */
	public static PlayerControl getInstance() {
		return instance;
	}

	/**
	 * Get the SlapPlayer based on their name, caps sensitive
	 * @param player The playername
	 * @return The SlapPlayer or null if not existing
	 */
	public static SlapPlayer getPlayer(String player) {
		return instance.nameToSlapPlayer.get(player);
	}
	
	/**
	 * Get the SlapPlayer based on the player
	 * @param player The player
	 * @return The SlapPlayer or null if not existing
	 */
	public static SlapPlayer getPlayer(Player player) {
		return instance.playerToSlapPlayer.get(player);
	}
	
	@Override
	public void shutdown() {
		//Clear maps
		nameToSlapPlayer.clear();
		playerToSlapPlayer.clear();
		
		//Set instance to null
		instance = null;
	}
}
