package me.naithantu.SlapHomebrew.Commands.AFK;

import me.naithantu.SlapHomebrew.Commands.AbstractCommand;
import me.naithantu.SlapHomebrew.Commands.Exception.CommandException;
import me.naithantu.SlapHomebrew.Controllers.AwayFromKeyboard;
import me.naithantu.SlapHomebrew.PlayerExtension.PlayerControl;
import me.naithantu.SlapHomebrew.Util.Util;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import ru.tehkode.permissions.PermissionUser;
import ru.tehkode.permissions.bukkit.PermissionsEx;

import java.util.List;

public class AfkInfoCommand extends AbstractCommand {

	public AfkInfoCommand(CommandSender sender, String[] args) {
		super(sender, args);
	}

	@Override
	public boolean handle() throws CommandException {
		testPermission("afkinfo"); //Test for permission
		
		if (args.length != 1) return false; //Check for correct usage
		
		AwayFromKeyboard afkController = plugin.getAwayFromKeyboard();
		
		if (args[0].equalsIgnoreCase("list")) { //Get full list of AFK players
			boolean afkPlayer = false;
			hMsg("--- AFK Players ---");
			for (Player p : plugin.getServer().getOnlinePlayers()) { //Loop thru players
				String pName = p.getName();
				if (afkController.isAfk(p)) { //Check if player is afk
					afkPlayer = true;
					PermissionUser pexUser = PermissionsEx.getUser(p);
					String prefix = "";
					if (pexUser != null) {
						if (pexUser.getPrefix() != null && pexUser.getPrefix().length() > 2) {
							prefix = pexUser.getPrefix().substring(0, 2);
						}
					}
					msg(" Player: " + ChatColor.AQUA + ChatColor.translateAlternateColorCodes('&', prefix + pName) + ChatColor.WHITE + " - Reason: " + ChatColor.GRAY + afkController.getAfkReason(p)); //Send AFK Reason
				}
			}
			if (!afkPlayer) { //If no players AFK
				msg(" There are no players AFK.");
			}
		} else { //Check for a single player
			Player foundPlayer = getOnlinePlayer(args[0], false); //Get the player
			String pName = foundPlayer.getName();
			boolean afk = afkController.isAfk(foundPlayer);
			
			PermissionUser pexUser = PermissionsEx.getUser(foundPlayer);
			String prefix = "";
			if (pexUser != null) {
				if (pexUser.getPrefix() != null && pexUser.getPrefix().length() > 2) {
					prefix = pexUser.getPrefix().substring(0, 2);
				}
			}
			
			hMsg("Player: " +  ChatColor.translateAlternateColorCodes('&', prefix + pName) + ChatColor.WHITE + " | AFK: " + (afk ? ChatColor.AQUA + "Yes" : ChatColor.RED + "No")); //Send AFK string
			if (afk) { //If Player is AFK
				String afkReason = afkController.getAfkReason(foundPlayer); //Get AFK Reason
				if (!afkReason.equals("AFK")) { //Check if custom AFK reason
					hMsg("AFK Reason: " + afkReason); //Send AFK Reason
				}
			}
			if (Util.testPermission(sender, "afkinfo.extended")) { //If CommandSender has extended info
				long lastActive = PlayerControl.getPlayer(foundPlayer).getLastActivity(); //Get last activity
				hMsg("Last activity: " + Util.getTimePlayedString(System.currentTimeMillis() - lastActive) + " ago."); //Get time ago
			}
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
		if (args.length > 1) return createEmptyList();
		List<String> list;
		if (sender instanceof Player) { //If sender = a player, exclude it from the afkinfo list
			list = listAllPlayers(sender.getName());
		} else { //List all players
			list = listAllPlayers();
		}
		list.add(0, "list"); //Add 'list' to the list.
		
		filterResults(list, args[0]); //Filter results
		return list;
	}
	
}
