package me.naithantu.SlapHomebrew.Commands.Homes;

import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.naithantu.SlapHomebrew.Commands.AbstractCommand;
import me.naithantu.SlapHomebrew.Commands.Exception.CommandException;
import me.naithantu.SlapHomebrew.Controllers.Homes;
import me.naithantu.SlapHomebrew.Util.Util;

public class HomesCommand extends AbstractCommand {

	public HomesCommand(CommandSender sender, String[] args) {
		super(sender, args);
	}

	@Override
	public boolean handle() throws CommandException {
		Player p = getPlayer(); //Player
		testPermission("home"); //Perm
		
		String playername = p.getName();
		Homes homes = plugin.getHomes(); //Get Homes
		
		List<String> homeList = homes.getHomes(playername); //Get homes
		int homesSize = homeList.size(); //Get size
		hMsg("You have " + homesSize + " out of " + homes.getTotalNumberOfHomes(playername) + " homes."); //Send used homes
		if (homesSize != 0) {
			p.sendMessage("Homes: " + ChatColor.GRAY + Util.buildString(homeList, ChatColor.WHITE + ", " + ChatColor.GRAY)); //Send all possible homes
		}
		return true;
	}

}
