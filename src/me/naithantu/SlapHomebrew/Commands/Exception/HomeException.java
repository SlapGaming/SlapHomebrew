package me.naithantu.SlapHomebrew.Commands.Exception;

import java.util.List;

import me.naithantu.SlapHomebrew.Util.Util;

import org.bukkit.ChatColor;

public class HomeException extends CommandException {

	private static final long serialVersionUID = 8105076648201775662L;

	public HomeException(String homename, List<String> homes) {
		super("There is no home found with the name: " + ChatColor.YELLOW + homename + "\n" + ChatColor.WHITE + "Homes: " + ChatColor.GRAY + Util.buildString(homes, ChatColor.WHITE + ", " + ChatColor.GRAY));
	}

}
