package me.naithantu.SlapHomebrew.Commands;

import java.util.List;

import me.naithantu.SlapHomebrew.SlapHomebrew;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

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

		Player player = (Player) sender;

		List<String> homes = plugin.getEssentials().getUserMap().getUser("naithantu").getHomes();
		if (args.length > 0) {
			if (homes.contains(args[0])) {
				try {
					player.teleport(plugin.getEssentials().getUserMap().getUser("naithantu").getHome(args[0]));
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else {
				sendHomes(homes, player);
			}
		} else if (homes.size() == 1) {
			try {
				player.teleport(plugin.getEssentials().getUserMap().getUser("naithantu").getHome(homes.get(0)));
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			sendHomes(homes, player);
		}

		return true;
	}

	private void sendHomes(List<String> homes, CommandSender sender) {
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
}
