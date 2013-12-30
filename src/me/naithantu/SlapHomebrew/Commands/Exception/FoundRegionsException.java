package me.naithantu.SlapHomebrew.Commands.Exception;


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
	
	public FoundRegionsException(int foundPriority) {
		super("Multiple regions found with same priority (highest: " + foundPriority + "). You'll need to enter the region's name.");
	}
}
