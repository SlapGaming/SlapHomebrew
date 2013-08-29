package me.naithantu.SlapHomebrew.Commands.Basics;

import java.util.List;

import me.naithantu.SlapHomebrew.SlapHomebrew;
import me.naithantu.SlapHomebrew.Commands.AbstractCommand;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.earth2me.essentials.User;

public class HomeCommand extends AbstractCommand {
	public HomeCommand(CommandSender sender, String[] args, SlapHomebrew plugin) {
		super(sender, args, plugin);
	}

	public boolean handle() {
		if (!testPermission(sender, "home")) {
			this.noPermission(sender);
			return true;
		}

		if (!(sender instanceof Player)) {
			this.badMsg(sender, "You need to be in-game to do that.");
			return true;
		}
		
		if (args.length == 3) {
			if (testPermission(sender, "home.other")) {
				// /home other [player] [home/list]
				if (args[0].toLowerCase().equals("other")) {
					User u = plugin.getEssentials().getUserMap().getUser(args[1]);
					if (u != null) {
						try {
							if (args[2].toLowerCase().equals("list")) {
								sendHomes(u.getHomes());
							} else {
								if (u.getHomes().contains(args[2].toLowerCase())) {
									Player player = (Player)sender;
									player.teleport(u.getHome(args[2].toLowerCase()));
								}
							}
							return true;
						} catch (Exception e) {
							sender.sendMessage(ChatColor.RED + "Something went wrong.");
							return true;
						}
					}
				}
			}
		}
		
		User targetPlayer = plugin.getEssentials().getUserMap().getUser(sender.getName()); //Fetch Essentials User (which extends Player)
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

	private void sendHomes(List<String> homes) {
		StringBuilder homeBuilder = new StringBuilder();
		int i = 0;
		for (String string : homes) {
			homeBuilder.append(string);
			i++;
			if (i != homes.size()) {
				homeBuilder.append(", ");
			}
		}
		sender.sendMessage("Homes (" + (homes.size()) + "): " + homeBuilder.toString());
	}
		
	public static void teleportToHome(User targetPlayer, String home){
		try {
			targetPlayer.getTeleport().home(targetPlayer.getHome(home), null);
		} catch (Exception e) {
			targetPlayer.sendMessage(ChatColor.RED + "Error: Invalid world.");
		}
	}
	
	
}
