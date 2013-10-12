package me.naithantu.SlapHomebrew.Commands.Lists;

import java.util.ArrayList;

import me.naithantu.SlapHomebrew.SlapHomebrew;
import me.naithantu.SlapHomebrew.Commands.AbstractCommand;
import me.naithantu.SlapHomebrew.Controllers.AwayFromKeyboard;
import me.naithantu.SlapHomebrew.Util.Util;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.earth2me.essentials.User;
import com.earth2me.essentials.UserMap;

import ru.tehkode.permissions.PermissionGroup;
import ru.tehkode.permissions.PermissionManager;
import ru.tehkode.permissions.PermissionUser;
import ru.tehkode.permissions.bukkit.PermissionsEx;

public class StaffListCommand extends AbstractCommand {
	
	private String onlineStaff;
	private int mods;
	private int admins;
	private int adminPlus;
	private ArrayList<String> staff;
	private boolean first;
	private final String afkString = ChatColor.WHITE + " [AFK]";
	
	private static AwayFromKeyboard afk = null;
	
	public StaffListCommand(CommandSender sender, String[] args, SlapHomebrew plugin) {
		super(sender, args, plugin);
		if (afk == null) {
			afk = plugin.getAwayFromKeyboard();
		}
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
		
		if (args.length == 1) {
			if (args[0].equalsIgnoreCase("all")) {
				Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
					
					@Override
					public void run() {
						String[] groups = new String[]{"SuperAdmin", "Admin", "VIPGuide", "Guide", "Mod"};
						PermissionManager pManager = PermissionsEx.getPermissionManager();
						UserMap uMap = plugin.getEssentials().getUserMap();
						for (String group : groups) {
							PermissionGroup pGroup = pManager.getGroup(group);
							if (pGroup != null) {
								for (PermissionUser user : pGroup.getUsers()) {
									User u = uMap.getUser(user.getName());
									if (u != null) {
										if (!u.getName().equals("naithantu") && !u.getName().equals("Telluur")) {
											addToList(user.getPrefix() + u.getName());
										}
									}
								}
							}
						}
						if (staff.size() == 0) {
							badMsg(sender, "There is no staff... That can't be right");
						} else {
							onlineStaff = "All staff: ";
							for (String staffMember: staff) {
								addToString(staffMember);
							}
							sender.sendMessage(Util.colorize(onlineStaff));
						}
					}
				});		
				return true;
			}
		}
		
		staff = new ArrayList<>();
		mods = admins = adminPlus = 0;
		first = true;
		
		for (Player player : plugin.getServer().getOnlinePlayers()) {
			if (player.hasPermission("reportrts.mod")) {
				PermissionUser user =  PermissionsEx.getUser(player);
				if (afk.isAfk(player.getName())) {
					addToList(user.getPrefix() + player.getName() + afkString);
				} else {
					addToList(user.getPrefix() + player.getName());
				}
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
