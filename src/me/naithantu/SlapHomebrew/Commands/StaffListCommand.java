package me.naithantu.SlapHomebrew.Commands;

import me.naithantu.SlapHomebrew.SlapHomebrew;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import ru.tehkode.permissions.PermissionUser;
import ru.tehkode.permissions.bukkit.PermissionsEx;

public class StaffListCommand extends AbstractCommand {
	
	protected StaffListCommand(CommandSender sender, String[] args, SlapHomebrew plugin) {
		super(sender, args, plugin);
	}

	@Override
	public boolean handle() {
		
		if (!(sender.hasPermission("slaphomebrew.stafflist"))) {
			noPermission(sender);
			return true;
		}
		
		String staffOnline = "Staff online: "; boolean first = true; int xCount = 0;
		for (Player player : plugin.getServer().getOnlinePlayers()) {
			if (player.hasPermission("reportrts.mod")) {
				PermissionUser user =  PermissionsEx.getUser(player);
				if (first) {
					first = false;
					staffOnline = staffOnline + user.getPrefix() + player.getName();
				} else {
					staffOnline = staffOnline + ChatColor.WHITE + ", " + user.getPrefix() + player.getName();
				}
				xCount++;
			}
		}
		if (xCount == 0) {
			sender.sendMessage(ChatColor.RED + "There is currently no staff online.");
		} else {
			sender.sendMessage(colorize(staffOnline));
		}
		return true;
	}
	
    public static String colorize(String s){
    	if(s == null) return null;
    	return s.replaceAll("&([0-9a-f])", "\u00A7$1");
    }

}
