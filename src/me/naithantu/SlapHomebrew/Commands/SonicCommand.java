package me.naithantu.SlapHomebrew.Commands;

import java.util.List;

import me.naithantu.SlapHomebrew.SlapHomebrew;
import me.naithantu.SlapHomebrew.Sonic;
import me.naithantu.SlapHomebrew.Util;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public class SonicCommand extends AbstractCommand {
	Sonic sonic;

	public SonicCommand(CommandSender sender, String[] args, SlapHomebrew plugin) {
		super(sender, args, plugin);
		sonic = plugin.getSonic();
	}

	public boolean handle() {
		if (!testPermission(sender, "sonic")) {
			this.noPermission(sender);
			return true;
		}

		if (args.length < 1) {
			this.msg(sender, "=======================================");
			this.msg(sender, "Map made by FVDisco.");
			this.msg(sender, "www.ocddisco.com/2012/11/minecraft-sonic-the-hedgehog/");
			this.msg(sender, "Made available for multiplayer by naithantu.");
			this.msg(sender, "Type /warpsonic to go to the racetrack. Enjoy! :)");
			this.msg(sender, "=======================================");
		} else {
			if (args[0].equalsIgnoreCase("addcheckpoint")) {
				sonic.addCheckpoint(args[1], Integer.parseInt(args[2]));
			} else if (args[0].equalsIgnoreCase("leaderboard") || args[0].equalsIgnoreCase("leaderboards")) {
				int page = 0;
				if (args.length > 1) {
					try {
						page = Integer.parseInt(args[1]) - 1;
					} catch (NumberFormatException e) {
						this.badMsg(sender, "Usage: /sonic leaderboard <page>");
						return true;
					}
				}

				List<String> leaderboard = sonic.getLeaderboard();

				if (page * 10 > leaderboard.size()) {
					this.badMsg(sender, "No players on that page.");
				}

				if (page == 0) {
					sender.sendMessage(ChatColor.YELLOW + "---- " + ChatColor.GOLD + "The current top 10 players:" + ChatColor.YELLOW + " ----");
				} else {
					sender.sendMessage(ChatColor.YELLOW + "---- " + ChatColor.GOLD + "Players " + (page * 10 + 1) + " to " + (page * 10 + 10) + ChatColor.YELLOW + " ----");
				}

				for (int i = page * 10; i < leaderboard.size(); i++) {
					String name = leaderboard.get(i);
					sender.sendMessage(ChatColor.GOLD + "" + (i + 1) + ". " + ChatColor.WHITE + name + " - " + Util.changeTimeFormat(plugin.getSonicStorage().getConfig().getLong("players." + name)));
					if (i == (page * 10) + 9)
						break;
				}
			} else if (args[0].equalsIgnoreCase("time") || args[0].equalsIgnoreCase("highscore")) {
				String name = sender.getName();
				if (args.length > 1) {
					name = args[1];
					List<String> leaderboard = sonic.getLeaderboard();
					for (int i = 0; i < leaderboard.size(); i++) {
						if (leaderboard.get(i).equalsIgnoreCase(name)) {
							this.msg(sender, name + " has a highscore of " + Util.changeTimeFormat(plugin.getSonicStorage().getConfig().getLong("players." + name)));
							this.msg(sender, name + " is ranked #" + (i + 1));
							return true;
						}
					}
					this.msg(sender, args[1] + " has never played sonic!");
				} else {
					List<String> leaderboard = sonic.getLeaderboard();
					for (int i = 0; i < leaderboard.size(); i++) {
						if (leaderboard.get(i).equalsIgnoreCase(name)) {
							this.msg(sender, "You have a highscore of " + Util.changeTimeFormat(plugin.getSonicStorage().getConfig().getLong("players." + name)) + ".");
							this.msg(sender, "You are ranked #" + (i + 1));
							return true;
						}
					}
					this.msg(sender, "You have never played sonic!");
				}

			}
		}
		return true;
	}
}
