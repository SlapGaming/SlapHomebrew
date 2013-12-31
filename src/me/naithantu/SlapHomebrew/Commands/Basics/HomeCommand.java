package me.naithantu.SlapHomebrew.Commands.Basics;

import java.util.List;

import me.naithantu.SlapHomebrew.Commands.AbstractCommand;
import me.naithantu.SlapHomebrew.Commands.Exception.CommandException;
import me.naithantu.SlapHomebrew.Commands.Exception.ErrorMsg;
import me.naithantu.SlapHomebrew.Util.Util;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

import com.earth2me.essentials.User;

public class HomeCommand extends AbstractCommand {
	public HomeCommand(CommandSender sender, String[] args) {
		super(sender, args);
	}

	public boolean handle() throws CommandException {
		Player p = getPlayer();
		testPermission("home");

		
		if (args.length == 3) {
			if (args[0].toLowerCase().equals("other")) { //Check if /home other [name] [X]
				testPermission("home.other"); //Test permission
				User u = plugin.getEssentials().getUserMap().getUser(args[1]);
				if (u != null) { //Check if player found with that name
					try {
						if (args[2].toLowerCase().equals("list")) { //If list -> Send all homes of that player
							sendHomes(u.getHomes());
						} else { //Probably trying to teleport to a home
							if (u.getHomes().contains(args[2].toLowerCase())) {
								p.teleport(u.getHome(args[2].toLowerCase()));
							} else {
								throw new CommandException("No home with this name found.");
							}
						}
						return true;
					} catch (Exception e) {
						throw new CommandException(ErrorMsg.somethingWrong);
					}
				} else {
					throw new CommandException(ErrorMsg.playerNotFound);
				}
			}
		}
		
		User targetPlayer = plugin.getEssentials().getUserMap().getUser(p.getName());
		List<String> homes = targetPlayer.getHomes();
		if (args.length > 0) {
			if (homes.contains(args[0])) {
				teleportToHome(targetPlayer, args[0]);
			} else {
				sendHomes(homes);
			}
		} else if (homes.size() == 1) {
			teleportToHome(targetPlayer, homes.get(0));
		} else {
			sendHomes(homes);
		}
		return true;
	}

	/**
	 * Send a list of all the homes to the CommandSender
	 * @param homes The homes
	 */
	private void sendHomes(List<String> homes) {
		sender.sendMessage("Homes (" + (homes.size()) + "): " + Util.buildString(homes, ", "));
	}
		
	public static void teleportToHome(User targetPlayer, String home){
		try {
			targetPlayer.getTeleport().teleport(targetPlayer.getHome(home), null, TeleportCause.COMMAND);
		} catch (Exception e) {
			targetPlayer.sendMessage(ChatColor.RED + "Error: " + e.getMessage());
		}
	}
	
	
}
