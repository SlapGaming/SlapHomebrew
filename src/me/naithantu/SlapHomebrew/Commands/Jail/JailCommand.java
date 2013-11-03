package me.naithantu.SlapHomebrew.Commands.Jail;

import java.util.Arrays;
import java.util.List;

import me.naithantu.SlapHomebrew.SlapHomebrew;
import me.naithantu.SlapHomebrew.Commands.AbstractCommand;
import me.naithantu.SlapHomebrew.Controllers.Jails;
import me.naithantu.SlapHomebrew.Util.Util;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.earth2me.essentials.Essentials;
import com.earth2me.essentials.User;

public class JailCommand extends AbstractCommand {

	private static Jails jails = null;
	private static Essentials ess = null;
	
	
	public JailCommand(CommandSender sender, String[] args, SlapHomebrew plugin) {
		super(sender, args, plugin);
		if (jails == null) {
			jails = plugin.getJails();
		}
		if (ess == null) {
			ess = plugin.getEssentials();
		}
	}

	@Override
	public boolean handle() {
		if (!testPermission(sender, "jail")) {
			noPermission(sender);
			return true;
		}
		
		if (args.length == 0) {
			return false;
		}
		
		switch (args[0].toLowerCase()) {
		case "list":
			List<String> jailList = jails.getJailList();
			if (jailList.size() == 1) {
				sender.sendMessage(Util.getHeader() + "There is 1 jail: " + jailList.get(0));
			} else if (jailList.size() > 1) {
				sender.sendMessage(Util.getHeader() + "There are " + jailList.size() + " jails: " + Arrays.toString(jailList.toArray()) + ".");
			} else {
				sender.sendMessage(ChatColor.RED + "There are no jails yet.");
			}
			break;
		case "create":
			//Jail create [name] <chat allowed> <msg commands allowed>
			if (!testPermission(sender, "jail.create")) {
				noPermission(sender);
				return true;
			}
			if (!(sender instanceof Player)) {
				badMsg(sender, "You need to be ingame to do that.");
				return true;
			}
			Player player = (Player) sender;
			if (args.length <= 1) { badMsg(sender, ChatColor.RED + "/jail create [name] <chat allowed: true/false> <msg commands allowed: true/false>"); return true; }
			boolean chatAllowed = false; boolean msgAllowed = false;
			if (args.length > 2) {
				chatAllowed = Boolean.parseBoolean(args[2]);
				if (args.length > 2) msgAllowed = Boolean.parseBoolean(args[3]);
			}
			if (jails.jailExists(args[1].toLowerCase())) {
				badMsg(sender, "Jail already exists.");
				return true;
			}
			jails.createJail(args[1].toLowerCase(), player.getLocation(), chatAllowed, msgAllowed);
			player.sendMessage(Util.getHeader() + "Jail created.");
			break;
		case "remove":
			//jail remove [name]
			if (!testPermission(sender, "jail.remove")) {
				noPermission(sender);
				return true;
			}
			if (args.length <= 1) {
				badMsg(sender, ChatColor.RED + "/jail remove [name]");
			} else {
				if (jails.jailExists(args[1].toLowerCase())) {
					jails.deleteJail(args[1].toLowerCase());
					sender.sendMessage(Util.getHeader() + "Jail removed.");
				} else {
					badMsg(sender, "This jail doesn't exist.");
				}
			}
			break;
		case "info":
			if (args.length == 2) {
				User u = ess.getUserMap().getUser(args[1]);
				if (u != null) {
					if (jails.isInJail(u.getName())) {
						jails.getJailInfo(sender, u.getName());
					} else badMsg(sender, "Player not in jail.");
				} else badMsg(sender, "This player doesn't exist.");
			} else {
				badMsg(sender, "/jail info [player]");
			}
			break;
		default:
			//Jail [player] [jail] [time] [h/m/s] [reason]
			if (args.length > 4) {
				User u = ess.getUserMap().getUser(args[0]);
				if (u == null) {
					badMsg(sender, "Player doesn't exist.");
					return true;
				}
				if (jails.isInJail(u.getName())) {
					badMsg(sender, "Player already jailed.");
					return true;
				}
				if (testPermission(u.getPlayer(), "jail.except")) {
					badMsg(sender, "This player cannot be jailed.");
					return true;
				}
				if (!jails.jailExists(args[1].toLowerCase())) {
					badMsg(sender, "Jail doesn't exist.");
					return true;
				}
				int time = -1;
				try {
					time = Integer.parseInt(args[2]);
				} catch (NumberFormatException e) { badMsg(sender, "This is not a valid time."); return true; }
				Long timeInJail = (long) 0;
				switch (args[3].toLowerCase()) {
				case "h": case "hour": case "hours":
					timeInJail = (long) (time * 1000 * 60 * 60); break;
				case "m": case "minute": case "min": case "minutes":
					timeInJail = (long) (time * 1000 * 60); break;
				case "s": case "seconds": case "sec": case "second":
					timeInJail = (long) (time * 1000); break;
				default:
					badMsg(sender, "Not a valid time type. Types: h/m/s");
					return true;
				}
				if (timeInJail > 10800000) {
					badMsg(sender, "You can't jail someone for that long."); return true;
				}
				String reason = null; boolean first = true; int xCount = 4;
				while (xCount < args.length) {
					if (first) { reason = args[xCount]; first = false;}
					else { reason = reason + " " + args[xCount]; }
					xCount++;
				}
				Player targetPlayer = plugin.getServer().getPlayer(u.getName());
				if (targetPlayer == null) {
					jails.putOfflinePlayerInJail(u.getName(), reason, args[1], timeInJail);
				} else {
					jails.putOnlinePlayerInJail(targetPlayer, reason, args[1], timeInJail);
				}
			} else {
				return false;
			}
		}
		return true;
	}
	
}
