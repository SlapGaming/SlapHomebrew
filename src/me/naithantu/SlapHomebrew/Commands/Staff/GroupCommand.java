package me.naithantu.SlapHomebrew.Commands.Staff;

import me.naithantu.SlapHomebrew.SlapHomebrew;
import me.naithantu.SlapHomebrew.Commands.AbstractCommand;
import me.naithantu.SlapHomebrew.Commands.Exception.CommandException;

import org.bukkit.command.CommandSender;
import ru.tehkode.permissions.PermissionUser;
import ru.tehkode.permissions.bukkit.PermissionsEx;

public class GroupCommand extends AbstractCommand {
	public GroupCommand(CommandSender sender, String[] args, SlapHomebrew plugin) {
		super(sender, args, plugin);
	}

	public boolean handle() throws CommandException {
		testPermission("group"); //Test permission
		if (args.length != 1) return false; //Check usage
		
		getOfflinePlayer(args[0]); //Get offline player
		
		PermissionUser user = PermissionsEx.getUser(args[0]); //Get PEX User
		hMsg(user.getName() + " is in group " + user.getGroupsNames()[0]);
		return true;
	}
}
