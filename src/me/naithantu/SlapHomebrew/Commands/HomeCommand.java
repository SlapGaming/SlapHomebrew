package me.naithantu.SlapHomebrew.Commands;

import java.util.List;

import me.naithantu.SlapHomebrew.SlapHomebrew;
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
		
		User targetPlayer = plugin.getEssentials().getUserMap().getUser(sender.getName()); //Fetch Essentials User (which extends Player)
		List<String> homes = targetPlayer.getHomes();
		if (args.length > 0) {
			if (homes.contains(args[0])) {
				teleportPlayer(targetPlayer, args[0]);
			} else {
				sendHomes(homes);
			}
		} else if (homes.size() == 1) {
			teleportPlayer(targetPlayer, homes.get(0));
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
		
	private void teleportPlayer(User targetPlayer, String home){
		try {
			targetPlayer.teleport(targetPlayer.getHome(home));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
}
