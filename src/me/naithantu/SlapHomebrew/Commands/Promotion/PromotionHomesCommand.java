package me.naithantu.SlapHomebrew.Commands.Promotion;

import me.naithantu.SlapHomebrew.Commands.AbstractCommand;
import me.naithantu.SlapHomebrew.Commands.Exception.CommandException;
import me.naithantu.SlapHomebrew.Commands.Exception.UsageException;
import me.naithantu.SlapHomebrew.Controllers.Homes;

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
		
		OfflinePlayer offPlayer; int homes;
		
		switch (args[1].toLowerCase()) {
		case "addhomes": case "add": case "addhome":
			testPermission("addhomes");
			if (args.length != 4) throw new UsageException("promotion homes addhomes [Player] [Homes]"); //Usage
			offPlayer = getOfflinePlayer(args[2]); //Get player
			homes = parseIntPositive(args[3]);
			homeControl.addHomesToPlayer(offPlayer.getName(), homes); //Add homes to a player
			hMsg("Added " + homes + (homes == 1 ? " home" : " homes") + " to the player: " + offPlayer.getName()); //Msg
			break;
			
		case "removehomes": case "removehome": case "remove":
			testPermission("removehomes");
			if (args.length != 4) throw new UsageException("promotion homes removehomes [Player] [Homes]"); //Usage
			offPlayer = getOfflinePlayer(args[2]); //Get player
			homes = parseIntPositive(args[3]);
			homeControl.removeHomesFromPlayer(offPlayer.getName(), homes); //Try to remove homes
			hMsg("Removed " + homes + (homes == 1 ? " home " : " homes ") + " from player: " + offPlayer.getName()); //Message how many homes removed
			break;
			
		case "gethomes": case "get": //Get the number of homes
			testPermission("gethomes"); //Test perm
			if (args.length != 3) throw new UsageException("promotion homes gethomes [Player]"); //Usage
			offPlayer = getOfflinePlayer(args[2]); //Get player
			homes = homeControl.getNumberOfBoughtHomes(offPlayer.getName()); //Get number of bought homes
			hMsg(offPlayer.getName() + " has bought " + homes + (homes == 1 ? " home." : " homes.")); //Message
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

}
