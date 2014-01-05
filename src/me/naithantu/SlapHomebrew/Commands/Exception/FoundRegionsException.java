package me.naithantu.SlapHomebrew.Commands.Exception;

import java.util.HashSet;

import org.bukkit.ChatColor;

import me.naithantu.SlapHomebrew.Util.Util;


/**
 * Error thrown while trying to find regions at a player's location
 */
public class FoundRegionsException extends CommandException {

	private static final long serialVersionUID = -7357016952978863989L;
	
	public FoundRegionsException() {
		super("No regions found at this location.");
	}
	
	public FoundRegionsException(String player, boolean member) {
		super("You aren't a " + (member ? "member or owner" : "owner") + " of any of the found regions at this location.");
	}
	
	public FoundRegionsException(HashSet<String> regions) {
		super("Multiple regions found with the same priority." + "\n" + "Regions: " + ChatColor.WHITE + Util.buildString(regions, ChatColor.RED + ", " + ChatColor.WHITE));
	}
}
