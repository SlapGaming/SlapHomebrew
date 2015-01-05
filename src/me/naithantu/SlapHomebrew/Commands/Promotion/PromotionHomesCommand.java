package me.naithantu.SlapHomebrew.Commands.Promotion;

import java.util.List;

import me.naithantu.SlapHomebrew.Commands.AbstractCommand;
import me.naithantu.SlapHomebrew.Commands.Exception.CommandException;
import me.naithantu.SlapHomebrew.Commands.Exception.UsageException;
import me.naithantu.SlapHomebrew.Controllers.Homes;

import nl.stoux.SlapPlayers.Model.Profile;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

public class PromotionHomesCommand extends AbstractCommand {

	private String usage;
	
	public PromotionHomesCommand(CommandSender sender, String[] args) {
		super(sender, args);
		usage = "promotion homes <addhomes | removehomes | gethomes>";
	}
	
	@Override
	public boolean handle() throws CommandException {
		if (args.length == 1) throw new UsageException(usage);
		
		Homes homeControl = plugin.getHomes();
		
		Profile offPlayer;
        int homes;
        String currentName;
		
		switch (args[1].toLowerCase()) {
		case "addhomes": case "add": case "addhome":
			testPermission("addhomes");
			if (args.length != 4) throw new UsageException("promotion homes addhomes [Player] [Homes]"); //Usage
			offPlayer = getOfflinePlayer(args[2]); //Get player
			homes = parseIntPositive(args[3]);
            currentName = offPlayer.getCurrentName();
			homeControl.addHomesToPlayer(offPlayer.getID(), homes); //Add homes to a player
			hMsg("Added " + homes + (homes == 1 ? " home" : " homes") + " to the player: " + currentName); //Msg
			break;
			
		case "removehomes": case "removehome": case "remove":
			testPermission("removehomes");
			if (args.length != 4) throw new UsageException("promotion homes removehomes [Player] [Homes]"); //Usage
			offPlayer = getOfflinePlayer(args[2]); //Get player
			homes = parseIntPositive(args[3]);
            currentName = offPlayer.getCurrentName();
			homeControl.removeHomesFromPlayer(offPlayer.getID(), homes); //Try to remove homes
			hMsg("Removed " + homes + (homes == 1 ? " home " : " homes ") + " from player: " + currentName); //Message how many homes removed
			break;
			
		case "gethomes": case "get": //Get the number of homes
			testPermission("gethomes"); //Test perm
			if (args.length != 3) throw new UsageException("promotion homes gethomes [Player]"); //Usage
			offPlayer = getOfflinePlayer(args[2]); //Get player
            currentName = offPlayer.getCurrentName();
			homes = homeControl.getNumberOfBoughtHomes(offPlayer.getID()); //Get number of bought homes
			hMsg(currentName + " has bought " + homes + (homes == 1 ? " home." : " homes.")); //Message
			break;
		
		default:
			throw new UsageException(usage); //Usage
		}
		
		return true;
	}
	
	@Override
	protected void testPermission(String perm) throws CommandException {
		super.testPermission("promotion.homes." + perm);
	}
	
	/**
	 * TabComplete on this command
	 * @param sender The sender of the command
	 * @param args given arguments
	 * @return List of options
	 */
	public static List<String> tabComplete(CommandSender sender, String[] args) {
		if (args.length == 2) {
			return filterResults(createNewList("addhomes", "removehomes", "gethomes"), args[1]); //Return sub commands
		} else if (args.length == 3) {
			return listAllPlayers(sender.getName()); //Return online players
		} else {
			return createEmptyList(); //Everything behind that ignore
		}
	}

}
