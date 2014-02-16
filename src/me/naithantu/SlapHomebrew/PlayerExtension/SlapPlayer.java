package me.naithantu.SlapHomebrew.PlayerExtension;

import me.naithantu.SlapHomebrew.Controllers.MessageStringer.MessageCombiner;

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
	private boolean rideOnRightClick; //Ride a clicked entity
	private boolean moved; //The player has moved since login
	private boolean rageQuit; //Player ragequited the server
	
	
	/**
	 * The last Epoch time the player did anything
	 */
	private long lastActivity;
	
	/**
	 * Teleporting mobs to other players
	 */
	private boolean teleportingMob;
	private Player teleportingTo;
	
	/**
	 * MessageCombiner for combining multiple lines of text
	 */
	private MessageCombiner messageCombiner;
	
	
	public SlapPlayer(Player p) {
		//Player
		player = p;
		playername = p.getName();
		
		//Bools
		doingCommand = false;
		toggledRegion = false;
		rideOnRightClick = false;
		moved = false;
		rageQuit = false;
		
		//Activity
		lastActivity = System.currentTimeMillis();
		
		//Teleporting mobs
		teleportingMob = false;
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
	
	/*
	 * Ride on right click
	 */
	/**
	 * Check if the player is waiting on a right click to ride an entity
	 * @return is right click to ride
	 */
	public boolean isRideOnRightClick() {
		return rideOnRightClick;
	}
	
	/**
	 * Set waiting for Right click to ride
	 * @param rideOnRightClick ride on click
	 */
	public void setRideOnRightClick(boolean rideOnRightClick) {
		this.rideOnRightClick = rideOnRightClick;
	}
	
	/*
	 * Teleporting mobs
	 */
	/**
	 * Check if the player is teleporting mobs
	 * @return is teleporting mobs
	 */
	public boolean isTeleportingMob() {
		return teleportingMob;
	}
	
	/**
	 * Get the player this player is teleporting mobs to
	 * @return The player that is being teleported to or null
	 */
	public Player getTeleportingTo() {
		return teleportingTo;
	}
	
	/**
	 * Set teleporting mobs to the specified player
	 * @param toPlayer The player
	 */
	public void setTeleportingMob(Player toPlayer) {
		teleportingMob = true;
		teleportingTo = toPlayer;
	}
	
	/**
	 * Remove teleporting mobs to a player
	 */
	public void removeTeleportingMob() {
		teleportingMob = false;
		teleportingTo = null;
	}
	
	/*
	 * Message combining
	 */
	
	/**
	 * Check if the player is combining a message
	 * @return
	 */
	public boolean isCombiningMessage() {
		return (messageCombiner != null);
	}
	
	/**
	 * Get message combiner
	 * @return the combiner
	 */
	public MessageCombiner getMessageCombiner() {
		return messageCombiner;
	}
	
	/**
	 * Set a message combiner
	 * @param messageCombiner
	 */
	public void setMessageCombiner(MessageCombiner messageCombiner) {
		this.messageCombiner = messageCombiner;
	}
	
	/**
	 * Remove the message combiner (if there is any)
	 */
	public void removeMessageCombiner() {
		this.messageCombiner = null;
	}
	
	/*
	 * Ragequit
	 */
	/**
	 * Set if the player is going to rage quit
	 * @param rageQuit is going to ragequit
	 */
	public void setRageQuit(boolean rageQuit) {
		this.rageQuit = rageQuit;
	}
	
	/**
	 * Check if the player is going to rage quit
	 * @return is going to rage quit
	 */
	public boolean isRageQuit() {
		return rageQuit;
	}
	
	
}
