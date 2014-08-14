package me.naithantu.SlapHomebrew.Commands.Fun;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import ru.tehkode.permissions.PermissionUser;
import ru.tehkode.permissions.bukkit.PermissionsEx;

import me.naithantu.SlapHomebrew.Commands.AbstractCommand;
import me.naithantu.SlapHomebrew.Commands.Exception.CommandException;
import me.naithantu.SlapHomebrew.Util.Util;

public class WaveCommand extends AbstractCommand {

	public WaveCommand(CommandSender sender, String[] args) {
		super(sender, args);
	}

	@Override
	public boolean handle() throws CommandException {
		Player fromPlayer = getPlayer();
		testPermission("wave");
				
		if (args.length != 1) return false;
		
		String gray = ChatColor.GRAY.toString(); //Short for grays due to heavy usage
				
		if (args[0].equalsIgnoreCase("everyone") || args[0].equalsIgnoreCase("all")) { //If waving to everyone
			Util.broadcast(gray + " ** " + getName(fromPlayer) + gray + " waves to " + ChatColor.GOLD + "Everyone" + gray + " **");
			
		} else { //Waving to player
			String toName = "";
			String toColoredName = "";

            //=> Get online player. Throw error if not found
            Player toPlayer = getOnlinePlayer(args[0], false); //Get target
            toName = toPlayer.getName();
            toColoredName = getName(toPlayer);
			
			//Check if not waving to self, and if allowed to wave to self
			if (fromPlayer.getName().equals(toName) && !Util.testPermission(fromPlayer, "wave.self")) {
				throw new CommandException("You cannot wave to yourself..");
			}
			
			//Broadcast
			Util.broadcast(gray + " ** " + getName(fromPlayer) + gray + " waves to " + toColoredName + gray + " **");
		}
		return true;
	}
	
	private String getName(Player p) {
		PermissionUser user = PermissionsEx.getUser(p);
		String name;
		if (user.getPrefix() != null && user.getPrefix().length() > 1) {
			name = Util.colorize(user.getPrefix().substring(0, 2));
		} else {
			name = ChatColor.WHITE.toString();
		}
		name += p.getName();
		return name;
	}

}
