package me.naithantu.SlapHomebrew.Commands.VIP;

import me.naithantu.SlapHomebrew.Commands.AbstractVipCommand;
import me.naithantu.SlapHomebrew.Commands.Exception.CommandException;
import me.naithantu.SlapHomebrew.Commands.Exception.ErrorMsg;
import me.naithantu.SlapHomebrew.Commands.Exception.UsageException;
import me.naithantu.SlapHomebrew.Controllers.Vip;
import me.naithantu.SlapHomebrew.Util.Util;

import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import ru.tehkode.permissions.PermissionUser;
import ru.tehkode.permissions.bukkit.PermissionsEx;

import com.earth2me.essentials.User;

public class VipCommand extends AbstractVipCommand {

	private static Vip vip;

	public VipCommand(CommandSender sender, String[] args) {
		super(sender, args);
		if (vip == null) {
			vip = plugin.getVip();
		}
	}
	
	public boolean handle() throws CommandException {
		Player player; User u; int days; PermissionUser pUser; OfflinePlayer offPlayer;
		
		if (args.length == 0) {
			if (sender instanceof Player) {
				player = (Player) sender;
				int vipDays = vip.getVipDays(player.getName());
				switch(vipDays) {
				case 0: //Not a VIP
					hMsg("You are not a VIP! Go to www.slapgaming.com/donate for more info about VIP!");
					break;
				case -1: //Lifetime
					hMsg("You have lifetime VIP :D!");
					break;
				default: //Days
					hMsg("You have " + vipDays + " VIP days remaining!");
				}
				return true;
			} else {
				return false;
			}
		}
		
		String arg = args[0].toLowerCase();
		
		switch (arg) { //Console allowed commands
		case "add":
			testPermission("add"); //Test perm
			if (arg.length() != 3) throw new UsageException("vip add [Player] [Days]"); //Check usage
			
			offPlayer = getOfflinePlayer(args[1]); //Get player
			days = parseInt(args[2]); //Parse days
			
			if (days <= 0) throw new CommandException("You need to add atleast 1 day."); //Check if added atleast 1 day 
			
			String playername = offPlayer.getName();
			int daysLeft = vip.getVipDays(playername);
			if (daysLeft == -1) throw new CommandException("This player already has lifetime."); //Check for lifetime
			
			vip.setVipDays(playername, daysLeft + days);
			pUser = PermissionsEx.getUser(playername);
			if (pUser != null) {
				switch (pUser.getGroups()[0].getName().toLowerCase()) {
				case "builder": case "member": case "guide":
					vip.promoteVip(playername);
					break;
				}
			}
			if (daysLeft > 0) hMsg("Added " + days + " days of VIP to " + playername + ", this player now has " + (days + daysLeft) + " days of VIP.");
			else hMsg("Added " + days + " days of VIP to " + playername +".");
			vip.save();
			break;
		case "set":
			testPermission("set");
			if (args.length != 3) throw new UsageException("vip set [Player] [Days]"); //Check usage
			
			offPlayer = getOfflinePlayer(args[1]); //Get player
			days = parseInt(args[2]); //Parse days
			
			if (days < -1) throw new CommandException(ErrorMsg.notANumber); //Check if valid set number
			
			playername = offPlayer.getName(); //Get name
			daysLeft = vip.getVipDays(playername); //Get days left
			
			if (days == 0 && daysLeft != 0)	vip.resetVipDays(playername); //Remove VIP Days
			else vip.setVipDays(playername, days); //Set vip Days
			
			pUser = PermissionsEx.getUser(playername);
			if (pUser != null) {
				String groupname = pUser.getGroups()[0].getName().toLowerCase();
				switch (groupname){
				case "vip": case "vipguide":	vip.demoteVip(playername);	
					if (days == 0 && daysLeft != 0) {
						vip.demoteVip(playername);
						hMsg(playername + " is no longer a VIP.");
					} else {
						hMsg("Number of VIP days of player " + playername + " has been updated.");
					}
					break;
				case "builder": case "member": case "guide":
					if (days != 0 && daysLeft == 0) {
						vip.promoteVip(playername);
						hMsg(playername + " is now a VIP.");
					}
					break;
				default:
					hMsg("Player is not in a member group (Mod+)");
				}
			} else {
				hMsg("PermissionUser was not found.");
			}
			vip.save();
			break;
		case "remove":
			testVipPermission("remove");
			if (args.length != 3) {
				hMsg("Usage: /vip remove [Player] [Days]");
				return true;
			}
			u = plugin.getEssentials().getUserMap().getUser(args[1]);
			if (u == null) {
				hMsg("This player has never been on the server.");
				return true;
			}
			days = 0;
			try {
				days = Integer.parseInt(args[2]);
			} catch (NumberFormatException e) {
				hMsg(args[2] + " is not a number.");
				return true;
			}
			if (days < 1) {
				hMsg("You need to remove atleast 1 day.");
				return true;
			}
			playername = u.getName();
			daysLeft = vip.getVipDays(playername);
			
			if (daysLeft == 0) { hMsg("This player is not a VIP."); return true; }
			else if (daysLeft == -1) { hMsg("This player has lifetime VIP."); return true; }
			
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
						hMsg(playername + " is no longer a VIP.");
					} else {
						hMsg(playername + " has " + (daysLeft - days) + " days of VIP left.");
					}
					break;
				default:
					hMsg("Not a VIP.");
				}
			}
			break;
		case "days":
			testVipPermission("days");
			if (args.length != 2) {
				hMsg("Usage: /vip days [Player]");
				return true;
			}
			u = plugin.getEssentials().getUserMap().getUser(args[1]);
			if (u == null) {
				hMsg("This player has never been on the server.");
				return true;
			}
			playername = u.getName();
			daysLeft = vip.getVipDays(playername);
			switch(daysLeft) {
			case 0: //Not a VIP
				hMsg(playername + " is not a VIP.");
				break;
			case -1: //Lifetime
				hMsg(playername + " has lifetime VIP.");
				break;
			default: //Days
				hMsg(playername + " has " + daysLeft + " days of VIP left.");
			}
			break;
		case "addhomes":
			testVipPermission("addhomes");
			if (args.length != 2) {
				hMsg("Usage: /vip addhomes [Player]");
				return true;
			}
			u = plugin.getEssentials().getUserMap().getUser(args[1]);
			if (u == null) {
				hMsg("This player has never been on the server.");
				return true;
			}
			vip.addHomes(u.getName());
			hMsg("20 homes added to player " + u.getName());
			break;
		case "givemoney":
			testVipPermission("givemoney");
			if (args.length != 3) {
				hMsg("Usage: /vip givemoney [Player] [Ammount]");
				return true;
			}
			u = plugin.getEssentials().getUserMap().getUser(args[1]);
			if (u == null) {
				hMsg("This player has never been on the server.");
				return true;
			}
			double amount = 0;
			try {
				amount = Integer.parseInt(args[2]);
			} catch (NumberFormatException e) {
				hMsg(args[2] + " is not a number.");
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
			hMsg("Gave player " + playername + " " + amount + " dollars" + vipBonus);
			break;
		default:
			if (!(sender instanceof Player)) {
				break;
			}
			player = (Player) sender;
			daysLeft = vip.getVipDays(player.getName());
			if (!Util.testPermission(player, "staff") && daysLeft == 0) {
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
				if (!Util.testPermission(player, "grant")) {
					noVipPermission(player);
					return true;
				}
				String worldName = player.getLocation().getWorld().getName().toLowerCase();
				if (worldName.equals("world_sonic") || worldName.equals("world_creative") || worldName.equals("world_pvp")) {
					Util.badMsg(player, "You are not allowed to grant items in this world.");
					return true;
				}
				FileConfiguration dataConfig = plugin.getDataStorage().getConfig();
				 String configPath = "usedgrant." + player.getName();
				if (!dataConfig.contains(configPath)) {
					dataConfig.set(configPath, 0);
				}
				int timesUsed = dataConfig.getInt(configPath);
				if(timesUsed >= 3){
					hMsg("You have already used VIP grant 3 times today!");
					return true;
				}
				plugin.getExtras().getMenus().getVipMenu().open(player);			
				break;
			case "homes":
				hMsg("You are allowed to set " + vip.getHomes(sender.getName()) + " homes!");
				break;
			default:
				Util.badMsg(sender, "Invalid command.");
			}
		}
		return true;
	}
	
	private void testVipPermission(String permission) throws CommandException {
		testPermission("vip." + permission);
	}
}

