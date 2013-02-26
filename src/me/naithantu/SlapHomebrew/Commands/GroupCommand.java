package me.naithantu.SlapHomebrew.Commands;

import me.naithantu.SlapHomebrew.SlapHomebrew;

import org.bukkit.command.CommandSender;
import ru.tehkode.permissions.PermissionUser;
import ru.tehkode.permissions.bukkit.PermissionsEx;

public class GroupCommand extends AbstractCommand {
	public GroupCommand(CommandSender sender, String[] args, SlapHomebrew plugin) {
		super(sender, args, plugin);
	}

	public boolean handle() {
		if (!testPermission(sender, "group")) {
			this.noPermission(sender);
			return true;
		}

		if (args.length != 1) {
			return false;
		}
		
		PermissionUser user = PermissionsEx.getUser(args[0]);
		String[] groupNames = user.getGroupsNames();
		this.msg(sender, user.getName() + " is in group " + groupNames[0]);
		return true;
	}
}
