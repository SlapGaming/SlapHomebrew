package me.naithantu.SlapHomebrew.Commands.Homes;

import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.naithantu.SlapHomebrew.Commands.AbstractCommand;
import me.naithantu.SlapHomebrew.Commands.Exception.CommandException;
import me.naithantu.SlapHomebrew.Commands.Exception.UsageException;
import me.naithantu.SlapHomebrew.Controllers.Homes;
import me.naithantu.SlapHomebrew.Util.Util;

public class HomeOtherCommand extends AbstractCommand {

	public HomeOtherCommand(CommandSender sender, String[] args) {
		super(sender, args);
	}

	@Override
	public boolean handle() throws CommandException {
		Player p = getPlayer();
		testPermission("homeother");
		if (args.length != 2) throw new UsageException("homeother [Player] <[Homename] | list>"); //Usage
		
		OfflinePlayer offPlayer = getOfflinePlayer(args[0]); //Get player
		String playername = offPlayer.getName();
		
		Homes homes = plugin.getHomes();
		if (args[1].equalsIgnoreCase("list")) { //Get list of all homes
			hMsg("Homes: " + ChatColor.GRAY + Util.buildString(homes.getHomes(playername), ChatColor.WHITE + ", " + ChatColor.GRAY));
		} else { //Teleport to a home
			p.teleport(homes.getHome(playername, args[1]));
		}
		return true;
	}

}
