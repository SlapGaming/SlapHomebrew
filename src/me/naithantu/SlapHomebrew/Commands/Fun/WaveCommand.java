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
		Player toPlayer = getOnlinePlayer(args[0], false); //Get target
		
		String gray = ChatColor.GRAY.toString();
		Util.broadcast(gray + "** " + getName(fromPlayer) + gray + " waves to " + getName(toPlayer) + gray + " **");
		return true;
	}
	
	private String getName(Player p) {
		PermissionUser user = PermissionsEx.getUser(p);
		String name = ChatColor.WHITE + "";
		if (user.getPrefix() != null && user.getPrefix().length() > 1) {
			name += ChatColor.translateAlternateColorCodes('&', user.getPrefix().substring(0, 2));
		}
		name += p.getName();
		return name;
	}

}
