package me.naithantu.SlapHomebrew.Commands.Exception;

public enum ErrorMsg {

	//Dev
	runningDev("Running a dev server. This is disabled."),
	notSupportedYet("This function/command is not supported yet!"),
	
	//General
	notAPlayer("You need to be in-game to do this!"),
	playerNotOnline("There is no player with this name online!"),
	playerNotFound("There is no player with that name on this server!"),
	noPermission("You do not have permission to do this."),
	somethingWrong("Something went wrong."),
	alreadyInVehicle("You are already in a vehicle."),
	cannotUseHere("You can not use that here!"),
	noMoney("You do not have enough money to do this!"),
	tooFarFromBlock("You're too far away from any blocks!"),
	breakingServer("You trying to break the server m8?"),
	wrongWorld("You cannot do that in this world!"),
	
	//Parsing
	notANumber("This is not a valid number."),
	
	//Lottery
	alreadyRolled("You have already rolled in this lottery!"),
	
	//Jails
	invalidJail("No jail found with this name."),
	notInJail("The player is not in a jail."),
	
	//Mail
	mailServer("You cannot mail the server!"),
	invalidMailID("The specified mail ID is invalid."),
	
	//Horses
	notOnAHorse("You need to be on a horse to do that!"),
	
	//VIP
	alreadyUsedVipGrant("You have already used VIP Grant 3 times today!"),
	
	//Improved Regions
	notTheOwner("You are not the owner of this region!");
	
	private String message;
	
	private ErrorMsg(String message) {
		this.message = message;
	}
	
	@Override
	public String toString() {
		return message;
	}
	
}
