package me.naithantu.SlapHomebrew.PlayerExtension;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class SlapPlayer {
	
	/**
	 * Player data
	 */
	private String playername;
	private Player player;
	
	/**
	 * Booleans
	 */
	private boolean doingCommand; //Is doing command
	private boolean toggledRegion; //Toggled /irg into /rg
	private boolean moved; //The player has moved since login
	
	/**
	 * The last Epoch time the player did anything
	 */
	private long lastActivity;
	
	
	public SlapPlayer(Player p) {
		//Player
		player = p;
		playername = p.getName();
		
		//Bools
		doingCommand = false;
		toggledRegion = false;
		moved = false;
		
		//Activity
		lastActivity = System.currentTimeMillis();		
	}
	
	/*
	 * General stuff
	 */
	/**
	 * Get the player instance
	 * @return The player
	 */
	public Player getPlayer() {
		return player;
	}
	
	/**
	 * Get the player instance
	 * @return The player
	 */
	public Player p() {
		return player;
	}
	
	/**
	 * Get the playername
	 * @return The name
	 */
	public String getName() {
		return playername;
	}
	
	
	/*
	 * Doing a command
	 */
	
	/**
	 * Check if the player is doing a command
	 * @return is doing a command
	 */
	public boolean isDoingCommand() {
		return doingCommand;
	}
	
	/**
	 * Set is doing a command
	 * @param doingCommand is doing a command
	 */
	public void setDoingCommand(boolean doingCommand) {
		this.doingCommand = doingCommand;
	}
	
	/*
	 * Toggled region
	 */
	/**
	 * Check if the player has toggled irg
	 * @return has toggled irg
	 */
	public boolean hasToggledRegion() {
		return toggledRegion;
	}

	/**
	 * Set if the player has toggled region
	 * @param toggledRegion has toggled
	 */
	public void setToggledRegion(boolean toggledRegion) {
		this.toggledRegion = toggledRegion;
	}
	
	/*
	 * Activity
	 */
	/**
	 * Set the last activity to now
	 */
	public void active() {
		lastActivity = System.currentTimeMillis();
	}
	
	/**
	 * Get the player's last Activity
	 * @return the player's last Activity
	 */
	public long getLastActivity() {
		return lastActivity;
	}
	
	/*
	 * Player moved
	 */
	/**
	 * The player moved
	 * Will also call active();
	 */
	public void moved() {
		moved = true;
		active();
	}
	
	/**
	 * Check if the player has moved
	 * @return has moved
	 */
	public boolean hasMoved() {
		return moved;
	}
	
	/**
	 * Send a player a message that he cannot chat or do command until he has moved
	 */
	public void sendNotMovedMessage() {
		player.sendMessage(ChatColor.GRAY + "You're not allowed to do commands/chat until you have moved.");
	}
}
