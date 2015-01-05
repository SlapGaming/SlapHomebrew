package me.naithantu.SlapHomebrew.Commands.Jail;

import java.util.Arrays;
import java.util.List;

import me.naithantu.SlapHomebrew.SlapHomebrew;
import me.naithantu.SlapHomebrew.Commands.AbstractCommand;
import me.naithantu.SlapHomebrew.Commands.Exception.CommandException;
import me.naithantu.SlapHomebrew.Commands.Exception.ErrorMsg;
import me.naithantu.SlapHomebrew.Commands.Exception.UsageException;
import me.naithantu.SlapHomebrew.Controllers.Jails;
import me.naithantu.SlapHomebrew.Util.Util;

import nl.stoux.SlapPlayers.Model.Profile;
import nl.stoux.SlapPlayers.SlapPlayers;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class JailCommand extends AbstractCommand {	
	
	public JailCommand(CommandSender sender, String[] args) {
		super(sender, args);
	}

	@Override
	public boolean handle() throws CommandException {
		testPermission("jail"); //Test permission
		if (args.length == 0) return false; //Check usage
		
		Profile offPlayer;
		
		//Get jails controller
		Jails jails = plugin.getJails();
		
		switch (args[0].toLowerCase()) {
		case "list": //Send list of jails
			List<String> jailList = jails.getJailNames();
			if (jailList.size() == 1) {
				hMsg("There is 1 jail: " + jailList.get(0));
			} else if (jailList.size() > 1) {
				hMsg("There are " + jailList.size() + " jails: " + Arrays.toString(jailList.toArray()) + ".");
			} else {
				throw new CommandException("There are no jails yet.");
			}
			break;
			
		case "create": //Create a new jail -> /Jail create [name] <chat allowed> <msg commands allowed>
			Player player = getPlayer();
			testPermission("jail.create");
			if (args.length <= 1) throw new UsageException("jail create [name] <chat allowed: true/false> <msg commands allowed: true/false>"); //Check usage
			
			boolean chatAllowed = false, msgAllowed = false; //Standard settings
			switch (args.length) { //Parse chat settings
			case 4: msgAllowed = Boolean.parseBoolean(args[3]);
			case 3:	chatAllowed = Boolean.parseBoolean(args[2]);
			}

            //Check if jail already exists
			if (jails.doesJailExist(args[1])) throw new CommandException("This jail already exists.");
			
			jails.createJail(args[1].toLowerCase(), player.getLocation(), chatAllowed, msgAllowed); //Create the jail
			hMsg("Created a jail with name: " + args[1] + " | Chat: " + chatAllowed + " | Msg: " + msgAllowed);
			break;
			
		case "remove": //Remove a jail -> /jail remove [name]
			testPermission("jail.remove");
			if (args.length <= 1) throw new UsageException("jail remove [name]"); //Check usage

            //Check if the jail exists
            if (!jails.doesJailExist(args[1])) throw new CommandException(ErrorMsg.invalidJail);

            //Delete the jail
            jails.deleteJail(args[1]);
            hMsg("Jail removed.");
			break;

        case "teleport": case "tp": //Teleport to a jail
            Player p = getPlayer();
            testPermission("jail.teleport");
            if (args.length <= 1) throw new UsageException("jail teleport [name]"); //Check usage

            //Check if the jail exists
            if (!jails.doesJailExist(args[1])) throw new CommandException(ErrorMsg.invalidJail);

            //Get the jail location
            p.teleport(jails.getJailLocation(args[1]));
            hMsg("Teleported to jail: " + args[1]);
            break;
			
		case "info": //Get info about a jail sentence. Players who are able to jail are also able to get info -> /jail info [player]
			if (args.length != 2) throw new UsageException("jail info [player"); //Check usage

            //Get the player
            offPlayer = getOfflinePlayer(args[1]);

            //Check if in jail
            if (!jails.isJailed(offPlayer.getUUIDString())) throw new CommandException(ErrorMsg.notInJail);

            //Get JailTime info
            jails.sendStaffJailInfo(sender, offPlayer);
			break;
			
		default: //Default = Jailing people -> /Jail [player] [jail] [time][h/m/s] [reason]
			if (args.length <= 3) return false;

            //Get the player
			offPlayer = getOfflinePlayer(args[0]);

            //Get the Jail name
            String jail = args[1].toLowerCase();
            //=> Check if the jail exists
            if (!jails.doesJailExist(jail)) throw new CommandException(ErrorMsg.invalidJail);

            //Check if the player can be jailed
            if (Util.checkPermission(offPlayer.getUUIDString(), "jail.exempt")) throw new CommandException("This player cannot be jailed.");
            //=> Or already jailed
            if (jails.isJailed(offPlayer.getUUIDString())) throw new CommandException("Player is already jailed.");

            //Parse Argument
            long time = Util.parseToTime(args[2]);
			if (time > 10800000 || time <= 0) throw new CommandException("You can only jail someone up to 3 hours."); //Max jail time = 3 hours

            //Parse reason
			String reason = Util.buildString(args, " ", 3);

            //Get the jailer ID
            String jailerUUID = (sender instanceof Player ? ((Player) sender).getUniqueId().toString() : "CONSOLE");
            int jailerID = SlapPlayers.getUUIDController().getProfile(jailerUUID).getID();

            //Jail the player
            jails.jailPlayer(offPlayer, reason, jail, time, jailerID);
            hMsg("Jailed " + offPlayer.getCurrentName() + " for " + Util.getTimePlayedString(time) + ".");

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
		if (!Util.testPermission(sender, "jail")) return null;
		
		if (args.length == 1) {
			//List all players (exclude sender if a player)
			String[] exclude = ((sender instanceof Player) ? new String[]{sender.getName()} : new String[]{});
			List<String> options = listAllPlayers(exclude);
			
			//Add options
			if (Util.testPermission(sender, "jail.remove")) options.add(0, "remove");
			if (Util.testPermission(sender, "jail.create")) options.add(0, "create");
			options.add(0, "list");
			options.add(0, "info");
			
			//Filter results
			filterResults(options, args[0]);
			return options;
		} else {
			switch (args[0].toLowerCase()) {
			case "create": case "info": case "list": //No futher usage for create or info
				return null;
				
			case "remove": //Return jails for remove
				if (args.length > 2) {
					return null;
				} else {
					List<String> list = SlapHomebrew.getInstance().getJails().getJailNames(); //Get all jails
					filterResults(list, args[1]); //Filter the results
					return list;
				}
				
			default:
				switch (args.length) {
				case 2: //Playername given -> Jail name
					List<String> jails = SlapHomebrew.getInstance().getJails().getJailNames();
					filterResults(jails, args[1]);
					return jails;
					
				case 3:  //Jail name given -> Add time
					if (args[2].isEmpty()) {
						return createNewList("3", "5", "8", "10", "15");
					} else {
						return null;
					}
					
				case 4: //Time given -> Add time formats
					List<String> formats = createNewList("sec", "seconds", "min", "minutes", "hour", "hours");
					filterResults(formats, args[3]);
					return formats;	
				}
			}
		}
		return null;
	}
	
}
