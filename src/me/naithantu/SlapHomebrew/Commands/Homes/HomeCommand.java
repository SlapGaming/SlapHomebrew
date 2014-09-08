package me.naithantu.SlapHomebrew.Commands.Homes;

import java.util.List;

import me.naithantu.SlapHomebrew.SlapHomebrew;
import me.naithantu.SlapHomebrew.Commands.AbstractCommand;
import me.naithantu.SlapHomebrew.Commands.Exception.CommandException;
import me.naithantu.SlapHomebrew.Controllers.Homes;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class HomeCommand extends AbstractCommand {
	
	private Homes homes;
	
	public HomeCommand(CommandSender sender, String[] args) {
		super(sender, args);
	}

	public boolean handle() throws CommandException {
		Player p = getPlayer(); //Player
		testPermission("home"); //Perm
		String playername = p.getName();

		homes = plugin.getHomes(); //Get Homes
		
		if (args.length > 0) { //If homename given
			if (args[0].equalsIgnoreCase("list")) { //If list
				new HomesCommand(p, args).handle();
			} else { //Teleport to home with name
				homes.teleportToHome(p, args[0]);
			}
		} else { //If no homename given
			List<String> homeList = homes.getHomes(p.getName()); //Get homes
			int homelistSize = homeList.size(); //Check number of homes set
			if (homelistSize == 0) { //No homes set, move to /homes
				new HomesCommand(p, args).handle();
				return true;
			}
			int homesAllowed = homes.getTotalNumberOfHomes(getUUIDProfile().getUserID()); //Get number of allowed homes
			if (homesAllowed == 1 && homelistSize == 1) { //If only 1 home set, and 1 home allowed
				
				homes.teleportToHome(p, homeList.get(0));
			} else { //Multiple homes allowed
				new HomesCommand(p, args).handle();
			}
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
		if (args.length > 1 || !(sender instanceof Player)) { //Usage
			return createEmptyList();
		} else {
			try { //Try to return homes
				List<String> list = SlapHomebrew.getInstance().getHomes().getHomes(sender.getName());
				filterResults(list, args[0]);
				return list;
			} catch (CommandException e) { //Else null
				System.out.println(e.getMessage());
				return createEmptyList();
			}
		}
	}
	
}
