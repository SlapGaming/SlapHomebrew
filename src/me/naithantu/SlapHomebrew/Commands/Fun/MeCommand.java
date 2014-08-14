package me.naithantu.SlapHomebrew.Commands.Fun;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import ru.tehkode.permissions.PermissionUser;
import ru.tehkode.permissions.bukkit.PermissionsEx;

import me.naithantu.SlapHomebrew.Commands.AbstractCommand;
import me.naithantu.SlapHomebrew.Commands.Exception.CommandException;
import me.naithantu.SlapHomebrew.Util.Util;

public class MeCommand extends AbstractCommand {

	public MeCommand(CommandSender sender, String[] args) {
		super(sender, args);
	}

	@Override
	public boolean handle() throws CommandException {
		Player p = getPlayer();
		testPermission("medescribe");
		
		if (args.length < 1) return false; //Usage
		
		//Base message
		String message = ChatColor.GRAY + " ** ";
		
		//Get user & Prefix => Add to message
		PermissionUser user = PermissionsEx.getUser(p);
		if (user != null && user.getPrefix() != null && user.getPrefix().length() > 1) {
			message += ChatColor.translateAlternateColorCodes('&', user.getPrefix().substring(0, 2));
		} else {
			message += ChatColor.WHITE;
		}
		message += p.getName();
		
		//Add message
		String buildString = Util.buildString(args, " ", 0);
		message += ChatColor.GRAY + " " + (p.hasPermission("") ? ChatColor.translateAlternateColorCodes('&', buildString) : buildString);
		
		//Broadcast
		Util.broadcast(message);

		return true;
	}

}
