package me.naithantu.SlapHomebrew.Commands.Lists;

import com.earth2me.essentials.User;
import com.earth2me.essentials.UserMap;
import me.naithantu.SlapHomebrew.Commands.AbstractCommand;
import me.naithantu.SlapHomebrew.Commands.Exception.CommandException;
import me.naithantu.SlapHomebrew.Controllers.AwayFromKeyboard;
import me.naithantu.SlapHomebrew.Util.Util;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import ru.tehkode.permissions.PermissionGroup;
import ru.tehkode.permissions.PermissionManager;
import ru.tehkode.permissions.PermissionUser;
import ru.tehkode.permissions.bukkit.PermissionsEx;

import java.util.ArrayList;
import java.util.List;

public class StaffListCommand extends AbstractCommand {
	
	private int mods;
	private int admins;
	private int adminPlus;
	private ArrayList<String> staff;
	private final String afkString = ChatColor.WHITE + " [AFK]";
	
	public StaffListCommand(CommandSender sender, String[] args) {
		super(sender, args);
	}

	@Override
	public boolean handle() throws CommandException {
		testPermission("stafflist");
		
		staff = new ArrayList<>();
		mods = admins = adminPlus = 0;
		
		if (args.length == 1 && args[0].equalsIgnoreCase("all")) {
			Util.runASync(new Runnable() {
				
				@Override
				public void run() {
					String[] groups = new String[]{"SuperAdmin", "Admin", "Mod"};
					PermissionManager pManager = PermissionsEx.getPermissionManager();
					UserMap uMap = plugin.getEssentials().getUserMap();
					for (String group : groups) {
						PermissionGroup pGroup = pManager.getGroup(group);
						if (pGroup != null) {
							for (PermissionUser user : pGroup.getUsers()) {
								User u = uMap.getUser(user.getName());
								if (u != null) {
									String userName = u.getName();
									switch (userName.toLowerCase()) {
									case "naithantu": case "telluur": case "hungryhomer": case "slapgaming":
										continue;
									}
									addToList(user.getPrefix() + userName);
								}
							}
						}
					}
					
					if (staff.isEmpty()) {
						Util.badMsg(sender, "There is no staff... That can't be right");
					} else {
						msg(Util.colorize("All staff: " + Util.buildString(staff, ChatColor.WHITE + ", ")));
					}
				};
			});
			return true;
		}
		
		AwayFromKeyboard afk = plugin.getAwayFromKeyboard();
		
		for (Player player : plugin.getServer().getOnlinePlayers()) {
			if (Util.testPermission(player, "staff")) {
				switch (player.getName().toLowerCase()) {
				case "naithantu": case "telluur": case "hungryhomer": case "slapgaming":
					continue;
				}
				PermissionUser user =  PermissionsEx.getUser(player);
				if (afk.isAfk(player)) {
					addToList(user.getPrefix() + player.getName() + afkString);
				} else {
					addToList(user.getPrefix() + player.getName());
				}
			}
		}
				
		if (staff.isEmpty()) {
			throw new CommandException("There is currently no staff online.");
		} else {		
			msg(Util.colorize("Staff: " + Util.buildString(staff, ChatColor.WHITE + ", ")));
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
    
    /**
	 * TabComplete on this command
	 * @param sender The sender of the command
	 * @param args given arguments
	 * @return List of options
	 */
	public static List<String> tabComplete(CommandSender sender, String[] args) {
		if (args.length == 1) {
			return filterResults(
				createNewList("online", "all"),
				args[0]
			);
		}
		return createEmptyList();
	}


}
