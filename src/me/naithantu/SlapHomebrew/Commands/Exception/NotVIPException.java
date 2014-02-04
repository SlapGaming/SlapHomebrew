package me.naithantu.SlapHomebrew.Commands.Exception;

import org.bukkit.ChatColor;

public class NotVIPException extends CommandException {

	private static final long serialVersionUID = 1224336088915200044L;

	public NotVIPException() {
		super("You are not VIP. Check " + ChatColor.YELLOW + "http://www.slapgaming.com/donate" + ChatColor.RED + " for more info about VIP!");
	}

	public NotVIPException(String aboutPlayer) {
		super(aboutPlayer + " is not VIP.");
	}

}
