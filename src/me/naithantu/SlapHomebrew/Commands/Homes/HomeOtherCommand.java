package me.naithantu.SlapHomebrew.Commands.Homes;

import me.naithantu.SlapHomebrew.Commands.AbstractCommand;
import me.naithantu.SlapHomebrew.Commands.Exception.CommandException;
import me.naithantu.SlapHomebrew.Commands.Exception.UsageException;
import me.naithantu.SlapHomebrew.Controllers.Homes;
import me.naithantu.SlapHomebrew.SlapHomebrew;
import me.naithantu.SlapHomebrew.Util.Util;
import nl.stoux.SlapPlayers.Model.Profile;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class HomeOtherCommand extends AbstractCommand {

	public HomeOtherCommand(CommandSender sender, String[] args) {
		super(sender, args);
	}

	@Override
	public boolean handle() throws CommandException {
		Player p = getPlayer();
		testPermission("homeother");
		if (args.length != 2) throw new UsageException("homeother [Player] <[Homename] | list>"); //Usage
		
		Profile offPlayer = getOfflinePlayer(args[0]); //Get player
		String playername = offPlayer.getCurrentName();
		
		Homes homes = plugin.getHomes();
		if (args[1].equalsIgnoreCase("list")) { //Get list of all homes
			hMsg("Homes: " + ChatColor.GRAY + Util.buildString(homes.getHomes(playername), ChatColor.WHITE + ", " + ChatColor.GRAY));
		} else { //Teleport to a home
			homes.teleportToLocation(p, homes.getHome(playername, args[1]));
		}
		return true;
	}

	/**
	 * TabComplete on this command
	 * @param sender The sender of the command
	 * @param args given arguments
	 * @return List of options
	 */
	public static List<String> tabComplete(CommandSender sender, String[] args) {
		if (!Util.testPermission(sender, "homeother") || args.length != 2) return null;
		
		List<String> list = createEmptyList();
		OfflinePlayer offPlayer = SlapHomebrew.getInstance().getServer().getOfflinePlayer(args[0]);
		if (offPlayer.getPlayer() == null && !offPlayer.hasPlayedBefore()) { //If player doesn't exist
			return list;
		}
				
		//Add list and all homes
		list.add("list");
		try {
			list.addAll(SlapHomebrew.getInstance().getHomes().getHomes(args[0]));
		} catch (CommandException e) {}
		
		//Filter
		filterResults(list, args[1]);
		
		return list; //Return
	}
}
