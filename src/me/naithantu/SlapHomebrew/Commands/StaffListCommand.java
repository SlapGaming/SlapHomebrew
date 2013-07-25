package me.naithantu.SlapHomebrew.Commands;

import java.util.ArrayList;

import me.naithantu.SlapHomebrew.SlapHomebrew;
import me.naithantu.SlapHomebrew.Util;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import ru.tehkode.permissions.PermissionUser;
import ru.tehkode.permissions.bukkit.PermissionsEx;

public class StaffListCommand extends AbstractCommand {
	
	private String onlineStaff;
	private int mods;
	private int admins;
	private int adminPlus;
	private ArrayList<String> staff;
	private boolean first;
	
	protected StaffListCommand(CommandSender sender, String[] args, SlapHomebrew plugin) {
		super(sender, args, plugin);
	}

	@Override
	public boolean handle() {
		if (!testPermission(sender, "stafflist")) {
			noPermission(sender);
			return true;
		}
		
		staff = new ArrayList<>();
		mods = admins = adminPlus = 0;
		first = true;
		
		for (Player player : plugin.getServer().getOnlinePlayers()) {
			if (player.hasPermission("reportrts.mod")) {
				PermissionUser user =  PermissionsEx.getUser(player);
				addToList(user.getPrefix() + player.getName());
			}
		}
				
		if (staff.size() == 0) {
			sender.sendMessage("There is currently no staff online.");
		} else {		
			onlineStaff = "Staff: ";
			for (String staffMember : staff) {
				addToString(staffMember);
			}
			sender.sendMessage(Util.colorize(onlineStaff));
		}
		return true;
	}
	    
    private void addToList(String staffMember) {
    	if (staffMember.contains("Guide")) {
    		staff.add(0, staffMember);
    		mods++;
    		admins++;
    		adminPlus++;
    	} else if (staffMember.contains("Mod")) {
    		staff.add(mods, staffMember);
    		admins++;
    		adminPlus++;
    	} else if (staffMember.contains("Admin")) {
    		staff.add(admins, staffMember);
    		adminPlus++;
    	} else {
    		staff.add(adminPlus, staffMember);
    	}
    }
        
    private void  addToString(String staffMember) {
    	if (first) {
    		onlineStaff = onlineStaff + staffMember;
    		first = false;
    	} else {
    		onlineStaff = onlineStaff + ChatColor.WHITE + ", " + staffMember;
    	}
    }

}
