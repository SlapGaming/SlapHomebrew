package me.naithantu.SlapHomebrew.Commands.Staff;

import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

import me.naithantu.SlapHomebrew.Commands.AbstractCommand;
import me.naithantu.SlapHomebrew.Commands.Exception.CommandException;
import me.naithantu.SlapHomebrew.Commands.Exception.UsageException;
import me.naithantu.SlapHomebrew.Controllers.Whitelist;
import me.naithantu.SlapHomebrew.Util.Util;

public class WhitelistCommand extends AbstractCommand {

	public WhitelistCommand(CommandSender sender, String[] args) {
		super(sender, args);
	}

	@Override
	public boolean handle() throws CommandException {
		testPermission("whitelist"); //Test perm
		if (args.length == 0) return false; //Usage
		
		Whitelist whitelist = plugin.getWhitelist();
		
		OfflinePlayer offPlayer;
		
		switch (args[0].toLowerCase()) {
		case "on": //Turn whitelist on
			whitelist.turnWhitelist(true);
			hMsg("Turned whitelist on.");
			break;
			
		case "off": //Turn whitelist off
			whitelist.turnWhitelist(false);
			hMsg("Turned whitelist off.");
			break;
			
		case "add": case "addplayer": //Add a player to the whitelist
			if (args.length != 2) throw new UsageException("whitelist add [Player]"); //Usage
			offPlayer = getOfflinePlayer(args[1]); //Check if existing user
			whitelist.addPlayer(offPlayer.getName()); //Add to whitelist
			hMsg("Added " + offPlayer.getName() + " to the whitelist.");
			break;
			
		case "remove": case "removeplayer": //Remove a player from the whitelist
			if (args.length != 2) throw new UsageException("whitelist remove [Player]"); //Usage
			offPlayer = getOfflinePlayer(args[1]); //Check if existing user
			whitelist.removePlayer(offPlayer.getName()); //Remove to whitelist
			hMsg("Removed " + offPlayer.getName() + " to the whitelist.");
			break;
			
		case "status": //Get the status of the whitelist
			hMsg("Whitelist is currently " + (whitelist.isWhitelistOn() ? ChatColor.GREEN + "on" : ChatColor.RED + "off") + ChatColor.WHITE + ".");
			break;
			
		case "whitelisted": case "players": case "whitelistedplayers": //Get whitelisted players
			hMsg("Whitelisted players: " + Util.buildString(whitelist.getAllowedPlayers(), ", "));
			break;
			
		case "setmessage": case "setwhitelistmessage": //Set the whitelist message
			if (args.length < 2) throw new UsageException("whitelist setmessage [Message...]");
			whitelist.setWhitelistMessage(Util.buildString(args, " ", 1));
			hMsg("Whitelist message set!");
			break;
			
		case "getmessage": case "getwhitelistmessage": //Get the whitelist message
			hMsg("Current message: " + whitelist.getWhitelistMessage());
			break;
			
		default: //Usage
			return false;
		}
		return true;
	}

}
