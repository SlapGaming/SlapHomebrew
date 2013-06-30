package me.naithantu.SlapHomebrew.Commands;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import me.naithantu.SlapHomebrew.Extras;
import me.naithantu.SlapHomebrew.SlapHomebrew;
import me.naithantu.SlapHomebrew.Vip;
import me.naithantu.SlapHomebrew.Storage.YamlStorage;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import ru.tehkode.permissions.PermissionUser;
import ru.tehkode.permissions.bukkit.PermissionsEx;

public class VipCommand extends AbstractVipCommand {

	String header = ChatColor.DARK_AQUA + "[VIP] " + ChatColor.WHITE;

	HashSet<String> vipItemsList = new HashSet<String>();
	private static YamlStorage vipStorage = null;
	private static FileConfiguration vipConfig = null;
	private static Vip vipUtil = null;

	List<String> vipCommands = Arrays.asList("grant", "copybook", "homes", "list", "grantlist", "resetgrant", "add", "set", "remove", "days", "addhomes", "givemoney", "mark", "check", "done");

	public VipCommand(CommandSender sender, String[] args, SlapHomebrew plugin) {
		super(sender, args, plugin);
		if (vipStorage == null || vipUtil == null) {
			vipStorage = plugin.getVipStorage();
			vipConfig = vipStorage.getConfig();
			vipUtil = plugin.getVip();
		}
	}

	public boolean handle() {
		String arg = null;
		try {
			arg = args[0];
		} catch (ArrayIndexOutOfBoundsException e) {
			Player player = (Player) sender;
			if (vipConfig.getConfigurationSection("vipdays").getInt(player.getName().toLowerCase()) == 0) {
				player.sendMessage(ChatColor.DARK_AQUA + "[VIP] " + ChatColor.WHITE + "You are not a VIP! Go to www.www.slapgaming.com/vip for more info about VIP!");
			} else if (vipConfig.getConfigurationSection("vipdays").getInt(player.getName().toLowerCase()) == -1) {
				player.sendMessage(ChatColor.DARK_AQUA + "[VIP] " + ChatColor.WHITE + "You have lifetime VIP! :D");
			} else {
				player.sendMessage(ChatColor.DARK_AQUA + "[VIP] " + ChatColor.WHITE + "You have " + vipConfig.getConfigurationSection("vipdays").getInt(player.getName().toLowerCase())
						+ " VIP days remaining!");
			}
			return true;
		}

		if (arg.equalsIgnoreCase("help")) {
			Player player = (Player) sender;
			player.sendMessage(ChatColor.DARK_AQUA + "[VIP] " + ChatColor.WHITE + "As a VIP you are allowed to spawn 3 stacks of items every day!");
			player.sendMessage(ChatColor.DARK_AQUA + "[VIP] " + ChatColor.WHITE + "Use /vip list for a list of all the items and /vip grant [itemname] to spawn the item!");
			player.sendMessage(ChatColor.DARK_AQUA + "[VIP] " + ChatColor.WHITE + "Not a VIP? Go to www.slapgaming.com/vip!");
			return true;
		}

		if (vipCommands.contains(arg)) {
			if (arg.equalsIgnoreCase("grant")) {
				if (!(sender instanceof Player)) {
					this.badMsg(sender, "You need to be in-game to do that.");
					return true;
				}
				
				if(!testPermission(sender, "grant")){
					this.badMsg(sender, "You need to be a vip to do that. Go to www.slapgaming.com/donate!");
					return true;
				}
		
				Extras extras = plugin.getExtras();
				Player player = (Player) sender;
				
				if(player.getWorld().getName().equals("world_sonic")||player.getWorld().getName().equals("world_creative")){
					this.badMsg(sender, "You may not grant items in this world!");
					return true;
				}
				String playerName = player.getName();
				YamlStorage dataStorage = plugin.getDataStorage();
				FileConfiguration dataConfig = dataStorage.getConfig();

				if (!dataConfig.contains("usedgrant." + playerName)) {
					dataConfig.set("usedgrant." + playerName, 0);
				}
				
				int timesUsed = dataConfig.getInt("usedgrant." + playerName);
				
				if(timesUsed >= 3){
					player.sendMessage(ChatColor.DARK_AQUA + "[VIP] " + ChatColor.WHITE + "You have already used this 3 times today!");
					return true;
				}

				extras.getMenus().getVipMenu().open((Player) sender);
				return true;
			}

			if (arg.equalsIgnoreCase("homes")) {
				sender.sendMessage(header + "You are allowed to set " + vipUtil.getHomes(sender.getName()) + " homes!");
			}

			//VIP expiry date commands:
			if (arg.equalsIgnoreCase("add")) {
				Player player = null;
				if (sender instanceof Player) {
					player = (Player) sender;
					if (!player.hasPermission("slaphomebrew.vip.add")) {
						player.sendMessage(ChatColor.DARK_AQUA + "[VIP] " + ChatColor.WHITE + "You don't have permission for that!");
						return true;
					}
				}
				if (args.length != 3)
					return false;
				String arg2 = args[1].toLowerCase();
				int arg3;
				try {
					arg3 = Integer.valueOf(args[2]);
				} catch (NumberFormatException e) {
					return false;
				}
				if (arg3 < 1 && arg3 != -1) {
					if (sender instanceof Player)
						player.sendMessage(ChatColor.RED + "Error: You must add at least 1 day!");
					return true;
				}
				//If arg3 == -1, set to -1 (infinite)
				if (arg3 == -1) {
					vipConfig.getConfigurationSection("vipdays").set(arg2, -1);
					vipUtil.promoteVip(arg2);
					sender.sendMessage(ChatColor.DARK_AQUA + "[VIP] " + ChatColor.WHITE + "Player " + args[1] + " now has lifetime VIP!");
					return true;
				}

				int daysLeft = 0;
				if (vipConfig.getConfigurationSection("vipdays").contains(arg2)) {
					daysLeft = vipConfig.getConfigurationSection("vipdays").getInt(arg2);
				}
				vipConfig.getConfigurationSection("vipdays").set(arg2, arg3 + daysLeft);
				PermissionUser user = PermissionsEx.getUser(args[1]);
				String[] groupNames = user.getGroupsNames();
				if (!groupNames[0].contains("VIP")) {
					vipUtil.promoteVip(arg2);
				}
				vipStorage.saveConfig();
				if (daysLeft > 0) {
					sender.sendMessage(ChatColor.DARK_AQUA + "[VIP] " + ChatColor.WHITE + "Added " + arg3 + " days to player " + args[1] + ", this player now has " + (arg3 + daysLeft)
							+ " VIP days remaining!");
				} else {
					sender.sendMessage(ChatColor.DARK_AQUA + "[VIP] " + ChatColor.WHITE + "Added " + arg3 + " days to player " + args[1] + "!");
				}
				return true;
			}

			if (arg.equalsIgnoreCase("set")) {
				if (sender instanceof Player) {
					Player player = (Player) sender;
					if (!player.hasPermission("slaphomebrew.vip.set")) {
						player.sendMessage(ChatColor.DARK_AQUA + "[VIP] " + ChatColor.WHITE + "You don't have permission for that!");
						return true;
					}
				}
				if (args.length != 3)
					return false;
				String arg2 = args[1].toLowerCase();
				int arg3;
				try {
					arg3 = Integer.valueOf(args[2]);
				} catch (NumberFormatException e) {
					return false;
				}
				if (arg3 == 0) {
					sender.sendMessage(ChatColor.DARK_AQUA + "[VIP] " + ChatColor.WHITE + "Player " + args[1] + " is no longer a VIP!");
					vipConfig.getConfigurationSection("vipdays").set(arg2, null);
					vipUtil.demoteVip(arg2);
				} else {
					sender.sendMessage(ChatColor.DARK_AQUA + "[VIP] " + ChatColor.WHITE + "Player " + args[1] + " now has " + arg3 + " days remaining!");
					vipConfig.getConfigurationSection("vipdays").set(arg2, arg3);
					PermissionUser user = PermissionsEx.getUser(args[1]);
					String[] groupNames = user.getGroupsNames();
					if (!groupNames[0].contains("VIP")) {
						vipUtil.promoteVip(arg2);
					}
				}
				vipStorage.saveConfig();
				return true;
			}

			if (arg.equalsIgnoreCase("remove")) {
				if (sender instanceof Player) {
					Player player = (Player) sender;
					if (!player.hasPermission("slaphomebrew.vip.remove")) {
						player.sendMessage(ChatColor.DARK_AQUA + "[VIP] " + ChatColor.WHITE + "You don't have permission for that!");
						return true;
					}
				}
				if (args.length != 3)
					return false;
				String arg2 = args[1].toLowerCase();
				int arg3;
				try {
					arg3 = Integer.valueOf(args[2]);
				} catch (NumberFormatException e) {
					return false;
				}
				int daysLeft = 0;
				if (vipConfig.getConfigurationSection("vipdays").contains(arg2)) {
					daysLeft = vipConfig.getConfigurationSection("vipdays").getInt(arg2);
				} else {
					sender.sendMessage(ChatColor.DARK_AQUA + "[VIP] " + ChatColor.WHITE + "Error: That player is not a VIP!");
					return true;
				}
				if (daysLeft == -1) {
					sender.sendMessage(ChatColor.DARK_AQUA + "[VIP] " + ChatColor.WHITE + "Error: That player has lifetime VIP!");
					return true;
				}
				if (arg3 > daysLeft) {
					daysLeft = 0;
				} else {
					daysLeft = daysLeft - arg3;
				}
				if (daysLeft > 0) {
					sender.sendMessage(ChatColor.DARK_AQUA + "[VIP] " + ChatColor.WHITE + "Removed " + arg3 + " days from player " + args[1] + ", this player now has " + daysLeft
							+ " VIP days remaining!");
					vipConfig.getConfigurationSection("vipdays").set(arg2, daysLeft);
					PermissionUser user = PermissionsEx.getUser(args[1]);
					String[] groupNames = user.getGroupsNames();
					if (!groupNames[0].contains("VIP")) {
						vipUtil.promoteVip(arg2);
					}
				} else {
					sender.sendMessage(ChatColor.DARK_AQUA + "[VIP] " + ChatColor.WHITE + "Removed " + arg3 + " days from player " + args[1] + ", this player is no longer a VIP!");
					vipConfig.getConfigurationSection("vipdays").set(arg2, null);
					vipUtil.demoteVip(arg2);
				}
				vipStorage.saveConfig();
				return true;
			}

			if (arg.equalsIgnoreCase("days")) {
				Player player = (Player) sender;
				if (!player.hasPermission("slaphomebrew.vip.days")) {
					player.sendMessage(ChatColor.DARK_AQUA + "[VIP] " + ChatColor.WHITE + "You don't have permission for that!");
					return true;
				}
				if (args.length < 2)
					return false;
				String arg2 = args[1].toLowerCase();
				if (vipConfig.getConfigurationSection("vipdays").getInt(arg2) == 0) {
					player.sendMessage(ChatColor.DARK_AQUA + "[VIP] " + ChatColor.WHITE + args[1] + " is not a VIP!");
				} else if (vipConfig.getConfigurationSection("vipdays").getInt(arg2) == -1) {
					player.sendMessage(ChatColor.DARK_AQUA + "[VIP] " + ChatColor.WHITE + args[1] + " has lifetime VIP!");
				} else {
					player.sendMessage(ChatColor.DARK_AQUA + "[VIP] " + ChatColor.WHITE + args[1] + " has " + vipConfig.getConfigurationSection("vipdays").getInt(arg2) + " VIP days remaining!");
				}
				return true;
			}

			if (arg.equalsIgnoreCase("addhomes")) {
				if (sender instanceof Player) {
					Player player = (Player) sender;
					if (!player.hasPermission("slaphomebrew.vip.addhomes")) {
						player.sendMessage(ChatColor.DARK_AQUA + "[VIP] " + ChatColor.WHITE + "You don't have permission for that!");
						return true;
					}
				}
				String playerName;
				if (args.length > 1) {
					playerName = args[1];
				} else {
					return false;
				}
				vipUtil.addHomes(playerName);
				sender.sendMessage(ChatColor.DARK_AQUA + "[VIP] " + ChatColor.WHITE + "20 homes added to player " + playerName);
				return true;
			}

			if (arg.equalsIgnoreCase("givemoney")) {
				if (sender instanceof Player) {
					Player player = (Player) sender;
					if (!player.hasPermission("slaphomebrew.vip.givemoney")) {
						player.sendMessage(ChatColor.DARK_AQUA + "[VIP] " + ChatColor.WHITE + "You don't have permission for that!");
						return true;
					}
				}
				String playerName;
				double amount;
				if (args.length > 2) {
					playerName = args[1];
					try {
						amount = Integer.parseInt(args[2]);
					} catch (NumberFormatException e) {
						return false;
					}
				} else {
					return false;
				}
				PermissionUser user = PermissionsEx.getUser(playerName);
				String[] groupNames = user.getGroupsNames();
				if (groupNames[0].contains("VIP") || groupNames[0].contains("VIPGuide")) {
					amount = amount * 1.2;
					plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), "mail send " + playerName + " " + ChatColor.DARK_AQUA + "[VIP] " + ChatColor.WHITE + "Your account had "
							+ amount + " dollar credited (Including 20% extra VIP bonus)!");
				} else {
					plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), "mail send " + playerName + " " + ChatColor.DARK_AQUA + "[VIP] " + ChatColor.WHITE + "Your account had "
							+ amount + " dollar credited!");
				}
				SlapHomebrew.econ.depositPlayer(playerName, amount);
				sender.sendMessage(ChatColor.DARK_AQUA + "[VIP] " + ChatColor.WHITE + "Gave player " + playerName + " " + amount + " dollars!");
				return true;
			}
		} else {
			Player player = (Player) sender;
			player.sendMessage(ChatColor.DARK_AQUA + "[VIP] " + ChatColor.WHITE + "That command does not exist!");
		}

		return true;
	}
}

