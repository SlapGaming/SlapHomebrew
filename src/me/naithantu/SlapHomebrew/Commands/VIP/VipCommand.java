package me.naithantu.SlapHomebrew.Commands.VIP;

import me.naithantu.SlapHomebrew.SlapHomebrew;
import me.naithantu.SlapHomebrew.Commands.AbstractVipCommand;
import me.naithantu.SlapHomebrew.Controllers.Vip;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import com.earth2me.essentials.User;

import ru.tehkode.permissions.PermissionUser;
import ru.tehkode.permissions.bukkit.PermissionsEx;

public class VipCommand extends AbstractVipCommand {

	private static Vip vip;

	public VipCommand(CommandSender sender, String[] args, SlapHomebrew plugin) {
		super(sender, args, plugin);
		if (vip == null) {
			vip = plugin.getVip();
		}
	}
	
	public boolean handle() {
		Player player; User u; int days; PermissionUser pUser;
		
		if (args.length == 0) {
			if (sender instanceof Player) {
				player = (Player) sender;
				int vipDays = vip.getVipDays(player.getName());
				switch(vipDays) {
				case 0: //Not a VIP
					msg(player, "You are not a VIP! Go to www.slapgaming.com/donate for more info about VIP!");
					break;
				case -1: //Lifetime
					msg(player, "You have lifetime VIP :D!");
					break;
				default: //Days
					msg(player, "You have " + vipDays + " VIP days remaining!");
				}
				return true;
			} else {
				return false;
			}
		}
		
		String arg = args[0].toLowerCase();
		
		switch (arg) { //Console allowed commands
		case "add":
			if (!isAllowed(sender, "add")) {
				return true;
			}
			if (arg.length() != 3) {
				msg(sender, "Usage: /vip add [Player] [Days]");
				return true;
			}
			u = plugin.getEssentials().getUserMap().getUser(args[1]);
			if (u == null) {
				msg(sender, "This player has never been on the server.");
				return true;
			}
			days = 0;
			try {
				days = Integer.parseInt(args[2]);
			} catch (NumberFormatException e) {
				msg(sender, args[2] + " is not a number.");
				return true;
			}
			if (days <= 0) {
				msg(sender, "You need to add atleast 1 day.");
				return true;
			}
			if (days == -1) {
				msg(sender, "This player already has lifetime VIP");
				return true;
			}
			String playername = u.getName();
			int daysLeft = vip.getVipDays(playername);
			vip.setVipDays(playername, daysLeft + days);
			pUser = PermissionsEx.getUser(playername);
			if (pUser != null) {
				switch (pUser.getGroups()[0].getName().toLowerCase()) {
				case "builder": case "member": case "guide":
					vip.promoteVip(playername);
					break;
				}
			}
			if (daysLeft > 0) msg(sender, "Added " + days + " days of VIP to " + playername + ", this player now has " + (days + daysLeft) + " days of VIP.");
			else msg(sender, "Added " + days + " days of VIP to " + playername +".");
			vip.save();
			break;
		case "set":
			if (!isAllowed(sender, "set")) {
				return true;
			}
			if (args.length != 3) {
				msg(sender, "Usage: /vip set [Player] [Days]");
				return true;
			}
			u = plugin.getEssentials().getUserMap().getUser(args[1]);
			if (u == null) {
				msg(sender, "This player has never been on the server.");
				return true;
			}
			
			days = 0;
			try {
				days = Integer.parseInt(args[2]);
			} catch (NumberFormatException e) {
				msg(sender, args[2] + " is not a number.");
				return true;
			}
			if (days < -1) {
				msg(sender, "This is not a valid number.");
				return true;
			}
			
			playername = u.getName();
			daysLeft = vip.getVipDays(playername);
			
			if (days == 0 && daysLeft != 0) { vip.resetVipDays(playername); }
			else { vip.setVipDays(playername, days); }
			
			pUser = PermissionsEx.getUser(playername);
			if (pUser != null) {
				String groupname = pUser.getGroups()[0].getName().toLowerCase();
				switch (groupname){
				case "vip": case "vipguide":	vip.demoteVip(playername);	
					if (days == 0 && daysLeft != 0) {
						vip.demoteVip(playername);
						msg(sender, playername + " is no longer a VIP.");
					} else {
						msg(sender, "Number of VIP days of player " + playername + " has been updated.");
					}
					break;
				case "builder": case "member": case "guide":
					if (days != 0 && daysLeft == 0) {
						vip.promoteVip(playername);
						msg(sender, playername + " is now a VIP.");
					}
					break;
				default:
					msg(sender, "Not a VIP.");
				}
			} else {
				msg(sender, "PermissionUser was not found.");
			}
			vip.save();
			break;
		case "remove":
			if (!isAllowed(sender, "remove")) {
				return true;
			}
			if (args.length != 3) {
				msg(sender, "Usage: /vip remove [Player] [Days]");
				return true;
			}
			u = plugin.getEssentials().getUserMap().getUser(args[1]);
			if (u == null) {
				msg(sender, "This player has never been on the server.");
				return true;
			}
			days = 0;
			try {
				days = Integer.parseInt(args[2]);
			} catch (NumberFormatException e) {
				msg(sender, args[2] + " is not a number.");
				return true;
			}
			if (days < 1) {
				msg(sender, "You need to remove atleast 1 day.");
				return true;
			}
			playername = u.getName();
			daysLeft = vip.getVipDays(playername);
			
			if (daysLeft == 0) { msg(sender, "This player is not a VIP."); return true; }
			else if (daysLeft == -1) { msg(sender, "This player has lifetime VIP."); return true; }
			
			if ((daysLeft - days) < 1) {
				vip.resetVipDays(playername);
			} else {
				vip.setVipDays(playername, daysLeft - days);
			}
			
			pUser = PermissionsEx.getUser(playername);
			if (pUser != null) {
				switch (pUser.getGroups()[0].getName().toLowerCase()) {
				case "vip": case "vipguide":
					if ((daysLeft - days) < 1) {
						vip.demoteVip(playername);
						msg(sender, playername + " is no longer a VIP.");
					} else {
						msg(sender, playername + " has " + (daysLeft - days) + " days of VIP left.");
					}
					break;
				default:
					msg(sender, "Not a VIP.");
				}
			}
			break;
		case "days":
			if (!isAllowed(sender, "days")) {
				return true;
			}
			if (args.length != 2) {
				msg(sender, "Usage: /vip days [Player]");
				return true;
			}
			u = plugin.getEssentials().getUserMap().getUser(args[1]);
			if (u == null) {
				msg(sender, "This player has never been on the server.");
				return true;
			}
			playername = u.getName();
			daysLeft = vip.getVipDays(playername);
			switch(daysLeft) {
			case 0: //Not a VIP
				msg(sender, playername + " is not a VIP.");
				break;
			case -1: //Lifetime
				msg(sender, playername + " has lifetime VIP.");
				break;
			default: //Days
				msg(sender, playername + " has " + daysLeft + " days of VIP left.");
			}
			break;
		case "addhomes":
			if (!isAllowed(sender, "addhomes")) {
				return true;
			}
			if (args.length != 2) {
				msg(sender, "Usage: /vip addhomes [Player]");
				return true;
			}
			u = plugin.getEssentials().getUserMap().getUser(args[1]);
			if (u == null) {
				msg(sender, "This player has never been on the server.");
				return true;
			}
			vip.addHomes(u.getName());
			msg(sender, "20 homes added to player " + u.getName());
			break;
		case "givemoney":
			if (!isAllowed(sender, "givemoney")) {
				return true;
			}
			if (args.length != 3) {
				msg(sender, "Usage: /vip givemoney [Player] [Ammount]");
				return true;
			}
			u = plugin.getEssentials().getUserMap().getUser(args[1]);
			if (u == null) {
				msg(sender, "This player has never been on the server.");
				return true;
			}
			double amount = 0;
			try {
				amount = Integer.parseInt(args[2]);
			} catch (NumberFormatException e) {
				msg(sender, args[2] + " is not a number.");
				return true;
			}
			playername = u.getName();
			pUser = PermissionsEx.getUser(playername);
			String vipBonus = ".";
			if (pUser != null) {
				switch (pUser.getGroups()[0].getName().toLowerCase()) {
				case "vip": case "vipguide":
					amount = amount * 1.2;
					vipBonus = " (with VIP bonus).";
					break;
				}
			}
			plugin.getEconomy().depositPlayer(playername, amount);
			msg(sender, "Gave player " + playername + " " + amount + " dollars" + vipBonus);
			break;
		default:
			if (!(sender instanceof Player)) {
				break;
			}
			player = (Player) sender;
			daysLeft = vip.getVipDays(player.getName());
			if (!testPermission(player, "staff") && daysLeft == 0) {
				noVipPermission(player);
				return true;
			}
			switch(arg) {
			case "help":
				String is = "========================";
				String[] helpArray = new String[] {
						ChatColor.YELLOW + is + ChatColor.DARK_AQUA + " VIP " + ChatColor.YELLOW + is,
						ChatColor.DARK_AQUA + "/vip : " + ChatColor.WHITE + "Check your remaining days of VIP.",
						ChatColor.DARK_AQUA + "/vip grant : " + ChatColor.WHITE + "Open the VIP grant menu.",
						ChatColor.DARK_AQUA + "/te [player] : " + ChatColor.WHITE + "Teleport to a player.",
						ChatColor.DARK_AQUA + "/tpa [player] : " + ChatColor.WHITE + "Request a player to teleport to him/her.",
						ChatColor.DARK_AQUA + "/tpahere [player] : " + ChatColor.WHITE + "Request a player to teleport to you.",
						ChatColor.DARK_AQUA + "/backdeath : " + ChatColor.WHITE + "Teleport to your death location."
				};
				player.sendMessage(helpArray);
				break;
			case "grant":
				if (!testPermission(player, "grant")) {
					noVipPermission(player);
					return true;
				}
				String worldName = player.getLocation().getWorld().getName().toLowerCase();
				if (worldName.equals("world_sonic") || worldName.equals("world_creative") || worldName.equals("world_pvp")) {
					badMsg(player, "You are not allowed to grant items in this world.");
					return true;
				}
				String configPath = "usedgrant." + player.getName();
				FileConfiguration dataConfig = plugin.getDataStorage().getConfig();

				if (!dataConfig.contains(configPath)) {
					dataConfig.set(configPath, 0);
				}
				int timesUsed = dataConfig.getInt(configPath);
				if(timesUsed >= 3){
					msg(player, "You have already used VIP grant 3 times today!");
					return true;
				}
				plugin.getExtras().getMenus().getVipMenu().open(player);			
				break;
			case "homes":
				msg(player, "You are allowed to set " + vip.getHomes(sender.getName()) + " homes!");
				break;
			default:
				badMsg(sender, "Invalid command.");
			}
		}
		return true;
	}
	
	private boolean isAllowed(CommandSender sender, String permission) {
		if (!testVipPermission(sender, permission)) {
			noPermission(sender);
			return false;
		}
		return true;
	}
}

