package me.naithantu.SlapHomebrew;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import com.earth2me.essentials.Essentials;
import com.google.common.base.Joiner;

import ru.tehkode.permissions.PermissionUser;
import ru.tehkode.permissions.bukkit.PermissionsEx;

public class VipCommands implements CommandExecutor {

	String header = ChatColor.DARK_AQUA + "[VIP] " + ChatColor.WHITE;
	SlapHomebrew slapRef = new SlapHomebrew();

	Integer used = 0;
	HashSet<String> vipItemsList = new HashSet<String>();

	private SlapHomebrew plugin;

	public VipCommands(SlapHomebrew instance) {
		plugin = instance;
	}
	@SuppressWarnings("rawtypes")
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {

		if (cmd.getName().equalsIgnoreCase("backdeath")) {
			Player player = (Player) sender;
			if (player.hasPermission("slaphomebrew.backdeath")) {
				if (SlapHomebrew.backDeath.containsKey(player.getName())) {
					player.teleport(SlapHomebrew.backDeath.get(player.getName()));
					player.sendMessage(ChatColor.DARK_AQUA + "[SLAP] " + ChatColor.WHITE + "You have been warped to your death location!");
				}
			} else {
				player.sendMessage(ChatColor.DARK_AQUA + "[VIP] " + ChatColor.WHITE + "You don't have permission for that! Go to slap-gaming.com/vip!");
			}
		}

		if (cmd.getName().equalsIgnoreCase("vip")) {
			String arg = null;
			String arg1 = null;
			try {
				arg = args[0];
			} catch (ArrayIndexOutOfBoundsException e) {
				Player player = (Player) sender;
				if (plugin.getVipConfig().getConfigurationSection("vipdays").getInt(player.getName().toLowerCase()) == 0) {
					player.sendMessage(ChatColor.DARK_AQUA + "[VIP] " + ChatColor.WHITE + "You are not a VIP! Go to www.slap-gaming.com/vip for more info about VIP!");
				} else if (plugin.getVipConfig().getConfigurationSection("vipdays").getInt(player.getName().toLowerCase()) == -1) {
					player.sendMessage(ChatColor.DARK_AQUA + "[VIP] " + ChatColor.WHITE + "You have lifetime VIP! :D");
				} else {
					player.sendMessage(ChatColor.DARK_AQUA + "[VIP] " + ChatColor.WHITE + "You have " + plugin.getVipConfig().getConfigurationSection("vipdays").getInt(player.getName().toLowerCase())
							+ " VIP days remaining!");
				}
				return true;
			}

			if (arg.equalsIgnoreCase("help")) {
				Player player = (Player) sender;
				player.sendMessage(ChatColor.DARK_AQUA + "[VIP] " + ChatColor.WHITE + "As a VIP you are allowed to spawn 3 stacks of items every day!");
				player.sendMessage(ChatColor.DARK_AQUA + "[VIP] " + ChatColor.WHITE + "Use /vip list for a list of all the items and /vip grant [itemname] to spawn the item!");
				player.sendMessage(ChatColor.DARK_AQUA + "[VIP] " + ChatColor.WHITE + "Not a VIP? Go to slap-gaming.com/vip!");
			}

			if (arg.equalsIgnoreCase("givemoney") || arg.equalsIgnoreCase("addhomes") || arg.equalsIgnoreCase("resetvip") || arg.equalsIgnoreCase("grantlist") || arg.equalsIgnoreCase("list")
					|| arg.equalsIgnoreCase("grant") || arg.equalsIgnoreCase("grantlistamount") || arg.equalsIgnoreCase("listamount") || arg.equalsIgnoreCase("resetvipgrantpermissions")
					|| arg.equalsIgnoreCase("resetgrant") || arg.equalsIgnoreCase("changevipspawnpermissions") || arg.equalsIgnoreCase("changegrant")
					|| arg.equalsIgnoreCase("removevipspawnpermissions") || arg.equalsIgnoreCase("removegrant") || arg.equalsIgnoreCase("help") || arg.equalsIgnoreCase("resetconfig")
					|| arg.equalsIgnoreCase("testconfig") || arg.equalsIgnoreCase("makeconfig") || arg.equalsIgnoreCase("saveuses") || arg.equalsIgnoreCase("loaduses")
					|| arg.equalsIgnoreCase("clearuses") || arg.equalsIgnoreCase("useslist") || arg.equalsIgnoreCase("saveconfig") || arg.equalsIgnoreCase("add") || arg.equalsIgnoreCase("remove")
					|| arg.equalsIgnoreCase("set") || arg.equalsIgnoreCase("days") || arg.equalsIgnoreCase("vips") || arg.equalsIgnoreCase("message") || arg.equalsIgnoreCase("check")
					|| arg.equalsIgnoreCase("mark") || arg.equalsIgnoreCase("done") || arg.equalsIgnoreCase("homes")) {

				if (arg.equalsIgnoreCase("grant")) {
					int type;
					Player player = (Player) sender;
					if (player.hasPermission("slaphomebrew.grant")) {
						
						try {
							arg = args[1];
						} catch (ArrayIndexOutOfBoundsException e) {
							player.sendMessage(ChatColor.DARK_AQUA + "[VIP] " + ChatColor.WHITE + "You haven't chosen an item to be given!");
							return true;
						}

						if (player.getInventory().firstEmpty() == -1) {
							player.sendMessage(ChatColor.DARK_AQUA + "[VIP] " + ChatColor.WHITE + "Your inventory is already full!");
							return true;
						}
						
						Essentials ess = new Essentials();
						try {
							System.out.println(arg);
							ItemStack stack = ess.getItemDb().get(arg);
							System.out.println(stack.getType());
						} catch (Exception e3) {
							e3.printStackTrace();
						}

						try {
							type = Integer.valueOf(arg);
						} catch (NumberFormatException e) {
							PlayerInventory inventory = player.getInventory();
							String name = player.getName();
							String typeName = arg;
							typeName = typeName.toUpperCase();
							Material typeMaterial = Material.getMaterial(typeName);
							ItemStack stackData;
							try {
								stackData = new ItemStack(typeMaterial, SlapHomebrew.vipItems.get(typeMaterial.getId()));
							} catch (NullPointerException e2) {
								player.sendMessage(ChatColor.DARK_AQUA + "[VIP] " + ChatColor.WHITE + "Unknown item name!");
								return true;
							}
							if (SlapHomebrew.vipItems.containsKey(typeMaterial.getId())) {
								if (SlapHomebrew.usedGrant.containsKey(name)) {
									try {
										used = ((Integer) SlapHomebrew.usedGrant.get(name).intValue());
									} catch (NullPointerException e1) {
									}
									if (used < 3) {
										used += 1;
										player.sendMessage(ChatColor.DARK_AQUA + "[VIP] " + ChatColor.WHITE + "Stack of items given.");
										int timesleft = 3 - used;
										player.sendMessage(ChatColor.DARK_AQUA + "[VIP] " + ChatColor.WHITE + timesleft + " times left today.");
										SlapHomebrew.usedGrant.put(name, used);
										inventory.addItem(stackData);
									} else {
										player.sendMessage(ChatColor.DARK_AQUA + "[VIP] " + ChatColor.WHITE + "You have already used this 3 times today!");
									}
								} else {
									used = 1;
									SlapHomebrew.usedGrant.put(name, used);
									inventory.addItem(stackData);
									int timesleft = 3 - used;
									player.sendMessage(ChatColor.DARK_AQUA + "[VIP] " + ChatColor.WHITE + "Stack of items given.");
									player.sendMessage(ChatColor.DARK_AQUA + "[VIP] " + ChatColor.WHITE + timesleft + " times left today.");
								}
							} else {
								player.sendMessage(ChatColor.DARK_AQUA + "[VIP] " + ChatColor.WHITE + "You are not allowed to spawn that!");
							}
							return true;
						}
						System.out.println(SlapHomebrew.vipItems.get(type));
						

						if (SlapHomebrew.vipItems.containsKey(type)) {
							PlayerInventory inventory = player.getInventory();

							int material = type;
							ItemStack stackData = new ItemStack(material, SlapHomebrew.vipItems.get(material));
							String name = player.getName();
							if (SlapHomebrew.usedGrant.containsKey(name)) {
								try {
									used = ((Integer) SlapHomebrew.usedGrant.get(name).intValue());
								} catch (NullPointerException e) {
								}
								if (used < 3) {
									used += 1;
									player.sendMessage(ChatColor.DARK_AQUA + "[VIP] " + ChatColor.WHITE + "Stack of items given.");
									int timesleft = 3 - used;
									player.sendMessage(ChatColor.DARK_AQUA + "[VIP] " + ChatColor.WHITE + timesleft + " times left today.");
									SlapHomebrew.usedGrant.put(name, used);
									inventory.addItem(stackData);
								} else {
									player.sendMessage(ChatColor.DARK_AQUA + "[VIP] " + ChatColor.WHITE + "You have already used this 3 times today!");
								}
							} else {
								used = 1;
								SlapHomebrew.usedGrant.put(name, used);
								inventory.addItem(stackData);
								int timesleft = 3 - used;
								player.sendMessage(ChatColor.DARK_AQUA + "[VIP] " + ChatColor.WHITE + "Stack of items given.");
								player.sendMessage(ChatColor.DARK_AQUA + "[VIP] " + ChatColor.WHITE + timesleft + " times left today.");
							}
						} else {
							player.sendMessage(ChatColor.DARK_AQUA + "[VIP] " + ChatColor.WHITE + "You are not allowed to spawn that!");
						}
					} else {
						player.sendMessage(ChatColor.DARK_AQUA + "[VIP] " + ChatColor.WHITE + "You don't have permission for that! Go to slap-gaming.com/vip!");
					}
				}
				
				if (arg.equalsIgnoreCase("homes")){
					sender.sendMessage(header + "You are allowed to set " + plugin.getHomes(sender.getName()) + " homes!");
				}

				if (arg.equalsIgnoreCase("grantlistamount") || arg.equalsIgnoreCase("listamount")) {
					Player player = (Player) sender;
					if (player.hasPermission("slaphomebrew.grantlist")) {
						//vipItemsList.clear();
						player.sendMessage(ChatColor.DARK_AQUA + "[VIP] " + ChatColor.WHITE + "VIPs can spawn the item codes: " + SlapHomebrew.vipItems.toString());
					} else {
						player.sendMessage(ChatColor.DARK_AQUA + "[VIP] " + ChatColor.WHITE + "You don't have permission for that! Go to slap-gaming.com/vip!");
					}
				}

				if (arg.equalsIgnoreCase("grantlist") || arg.equalsIgnoreCase("list")) {
					Player player = (Player) sender;
					if (player.hasPermission("slaphomebrew.grantlist")) {
						vipItemsList.clear();
						Set<?> set = SlapHomebrew.vipItems.entrySet();
						Iterator<?> i = set.iterator();
						while (i.hasNext()) {
							Map.Entry me = (Map.Entry) i.next();
							Material vipItemsMaterial = Material.getMaterial((Integer) me.getKey());
							vipItemsList.add(vipItemsMaterial.toString().toLowerCase());
						}
						player.sendMessage(ChatColor.DARK_AQUA + "[VIP] " + ChatColor.WHITE + "VIPs can spawn the items: " + vipItemsList.toString());
					} else {
						player.sendMessage(ChatColor.DARK_AQUA + "[VIP] " + ChatColor.WHITE + "You don't have permission for that! Go to slap-gaming.com/vip!");
					}
				}

				if (arg.equalsIgnoreCase("resetvip")) {
					Player player = (Player) sender;
					if (player.hasPermission("slaphomebrew.managevip")) {
						player.sendMessage(ChatColor.DARK_AQUA + "[VIP] " + ChatColor.WHITE + "All uses have been reset!");
						SlapHomebrew.usedGrant.clear();
					} else {
						player.sendMessage(ChatColor.DARK_AQUA + "[VIP] " + ChatColor.WHITE + "You don't have permission for that!");
					}
				}

				if (arg.equalsIgnoreCase("resetvipspawnpermissions") || arg.equalsIgnoreCase("resetgrant")) {
					Player player = (Player) sender;
					if (player.hasPermission("slaphomebrew.managevip")) {
						SlapHomebrew.vipItems.clear();
						SlapHomebrew.vipItems.put(1, 64);
						SlapHomebrew.vipItems.put(2, 64);
						SlapHomebrew.vipItems.put(3, 64);
						SlapHomebrew.vipItems.put(4, 64);
						SlapHomebrew.vipItems.put(5, 64);
						SlapHomebrew.vipItems.put(12, 64);
						SlapHomebrew.vipItems.put(13, 64);
						SlapHomebrew.vipItems.put(17, 64);
						SlapHomebrew.vipItems.put(18, 64);
						SlapHomebrew.vipItems.put(20, 64);
						SlapHomebrew.vipItems.put(24, 64);
						SlapHomebrew.vipItems.put(35, 64);
						SlapHomebrew.vipItems.put(37, 64);
						SlapHomebrew.vipItems.put(38, 64);
						SlapHomebrew.vipItems.put(43, 64);
						SlapHomebrew.vipItems.put(44, 64);
						SlapHomebrew.vipItems.put(53, 64);
						SlapHomebrew.vipItems.put(65, 64);
						SlapHomebrew.vipItems.put(67, 64);
						SlapHomebrew.vipItems.put(81, 64);
						SlapHomebrew.vipItems.put(86, 64);
						SlapHomebrew.vipItems.put(87, 64);
						SlapHomebrew.vipItems.put(89, 64);
						SlapHomebrew.vipItems.put(91, 64);
						SlapHomebrew.vipItems.put(98, 64);
						SlapHomebrew.vipItems.put(109, 64);
						SlapHomebrew.vipItems.put(110, 64);// 32
						SlapHomebrew.vipItems.put(112, 64);// 32
						SlapHomebrew.vipItems.put(337, 64);
						plugin.saveItems();
						player.sendMessage(ChatColor.DARK_AQUA + "[VIP] " + ChatColor.WHITE + "The vip items list has been reset to default!");
					} else {
						player.sendMessage(ChatColor.DARK_AQUA + "[VIP] " + ChatColor.WHITE + "You don't have permission for that!");
					}
				}
				if (arg.equalsIgnoreCase("changevipspawnpermissions") || arg.equalsIgnoreCase("changegrant")) {
					Player player = (Player) sender;
					if (player.hasPermission("slaphomebrew.managevip")) {
						try {
							arg = args[1];
						} catch (ArrayIndexOutOfBoundsException e) {
							player.sendMessage(ChatColor.DARK_AQUA + "[VIP] " + ChatColor.WHITE + "Usage: /changevipspawnpermissions itemcode amount!");
							return true;
						}
						try {
							arg1 = args[2];
						} catch (ArrayIndexOutOfBoundsException e) {
							player.sendMessage(ChatColor.DARK_AQUA + "[VIP] " + ChatColor.WHITE + "Usage: /changevipspawnpermissions itemcode amount!");
							return true;
						}
						Integer itemcode = 0;
						Integer itemamount = 0;
						itemcode = Integer.valueOf(arg);
						itemamount = Integer.valueOf(arg1);
						if (SlapHomebrew.vipItems.containsKey(itemcode))
							SlapHomebrew.vipItems.remove(itemcode);
						if (itemcode > 0 && itemcode < 122)
							SlapHomebrew.vipItems.put(itemcode, itemamount);
						player.sendMessage(ChatColor.DARK_AQUA + "[VIP] " + ChatColor.WHITE + "The vip item list has been edited!");
					} else {
						player.sendMessage(ChatColor.DARK_AQUA + "[VIP] " + ChatColor.WHITE + "You don't have permission for that!");
					}
				}
				if (arg.equalsIgnoreCase("removevipspawnpermissions") || arg.equalsIgnoreCase("removegrant")) {
					Player player = (Player) sender;
					if (player.hasPermission("slaphomebrew.managevip")) {
						try {
							arg = args[1];
						} catch (ArrayIndexOutOfBoundsException e) {
							player.sendMessage(ChatColor.DARK_AQUA + "[VIP] " + ChatColor.WHITE + "Usage: /removevipspawnpermissions itemcode!");
							return true;
						}
						Integer itemcode = 0;
						itemcode = Integer.valueOf(arg);
						if (SlapHomebrew.vipItems.containsKey(itemcode))
							SlapHomebrew.vipItems.remove(itemcode);
						player.sendMessage(ChatColor.DARK_AQUA + "[VIP] " + ChatColor.WHITE + "The vip item list has been edited!");
					} else {
						player.sendMessage(ChatColor.DARK_AQUA + "[VIP] " + ChatColor.WHITE + "You don't have permission for that!");
					}
				}
				if (arg.equalsIgnoreCase("gettime")) {
					Player player = (Player) sender;
					if (player.hasPermission("slaphomebrew.managevip")) {
						DateFormat checkDay = new SimpleDateFormat("dd");
						DateFormat checkMonth = new SimpleDateFormat("MM");
						DateFormat checkYear = new SimpleDateFormat("yyyy");
						Date date = new Date();
						int vipDay = Integer.valueOf(checkDay.format(date));
						int vipMonth = Integer.valueOf(checkMonth.format(date));
						int vipYear = Integer.valueOf(checkYear.format(date));
						if (vipDay > plugin.getConfig().getInt("vipdate.day") || vipYear > plugin.getConfig().getInt("vipdate.month") || vipYear > plugin.getConfig().getInt("vipdate.year")) {
							SlapHomebrew.usedGrant.clear();
						}
						plugin.getConfig().set("vipdate.day", vipDay);
						plugin.getConfig().set("vipdate.month", vipMonth);
						plugin.getConfig().set("vipdate.year", vipYear);
						plugin.saveConfig();
						player.sendMessage(ChatColor.RED + "This command is deprecated, do not use it!");
					}
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
						plugin.getVipConfig().getConfigurationSection("vipdays").set(arg2, -1);
						plugin.promoteVip(arg2);
						sender.sendMessage(ChatColor.DARK_AQUA + "[VIP] " + ChatColor.WHITE + "Player " + args[1] + " now has lifetime VIP!");
						return true;
					}

					int daysLeft = 0;
					if (plugin.getVipConfig().getConfigurationSection("vipdays").contains(arg2)) {
						daysLeft = plugin.getVipConfig().getConfigurationSection("vipdays").getInt(arg2);
					}
					plugin.getVipConfig().getConfigurationSection("vipdays").set(arg2, arg3 + daysLeft);
					PermissionUser user = PermissionsEx.getUser(args[1]);
					String[] groupNames = user.getGroupsNames();
					if (!groupNames[0].contains("VIP")) {
						plugin.promoteVip(arg2);
					}
					plugin.saveVipConfig();
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
						plugin.getVipConfig().getConfigurationSection("vipdays").set(arg2, null);
						plugin.demoteVip(arg2);
					} else {
						sender.sendMessage(ChatColor.DARK_AQUA + "[VIP] " + ChatColor.WHITE + "Player " + args[1] + " now has " + arg3 + " days remaining!");
						plugin.getVipConfig().getConfigurationSection("vipdays").set(arg2, arg3);
						PermissionUser user = PermissionsEx.getUser(args[1]);
						String[] groupNames = user.getGroupsNames();
						if (!groupNames[0].contains("VIP")) {
							plugin.promoteVip(arg2);
						}
					}
					plugin.saveVipConfig();
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
					if (plugin.getVipConfig().getConfigurationSection("vipdays").contains(arg2)) {
						daysLeft = plugin.getVipConfig().getConfigurationSection("vipdays").getInt(arg2);
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
						plugin.getVipConfig().getConfigurationSection("vipdays").set(arg2, daysLeft);
						PermissionUser user = PermissionsEx.getUser(args[1]);
						String[] groupNames = user.getGroupsNames();
						if (!groupNames[0].contains("VIP")) {
							plugin.promoteVip(arg2);
						}
					} else {
						sender.sendMessage(ChatColor.DARK_AQUA + "[VIP] " + ChatColor.WHITE + "Removed " + arg3 + " days from player " + args[1] + ", this player is no longer a VIP!");
						plugin.getVipConfig().getConfigurationSection("vipdays").set(arg2, null);
						plugin.demoteVip(arg2);
					}
					plugin.saveVipConfig();
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
					if (plugin.getVipConfig().getConfigurationSection("vipdays").getInt(arg2) == 0) {
						player.sendMessage(ChatColor.DARK_AQUA + "[VIP] " + ChatColor.WHITE + args[1] + " is not a VIP!");
					} else if (plugin.getVipConfig().getConfigurationSection("vipdays").getInt(arg2) == -1) {
						player.sendMessage(ChatColor.DARK_AQUA + "[VIP] " + ChatColor.WHITE + args[1] + " has lifetime VIP!");
					} else {
						player.sendMessage(ChatColor.DARK_AQUA + "[VIP] " + ChatColor.WHITE + args[1] + " has " + plugin.getVipConfig().getConfigurationSection("vipdays").getInt(arg2)
								+ " VIP days remaining!");
					}
					return true;
				}

				if (arg.equalsIgnoreCase("vips")) {
					Player player = (Player) sender;
					if (!player.hasPermission("slaphomebrew.vip.vips")) {
						player.sendMessage(ChatColor.DARK_AQUA + "[VIP] " + ChatColor.WHITE + "You don't have permission for that!");
						return true;
					}
					HashMap<String, Integer> tempMap = new HashMap<String, Integer>();
					for (String key : plugin.getVipConfig().getConfigurationSection("vipdays").getKeys(false)) {
						tempMap.put(key, plugin.getVipConfig().getInt("vipdays." + key));
					}
					player.sendMessage(ChatColor.DARK_AQUA + "[VIP] " + ChatColor.WHITE + "The current vips are: " + tempMap);
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
					plugin.addHomes(playerName);
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
						plugin.getServer()
								.dispatchCommand(
										plugin.getServer().getConsoleSender(),
										"mail send " + playerName + " " + ChatColor.DARK_AQUA + "[VIP] " + ChatColor.WHITE + "Your account had " + amount
												+ " dollar credited (Including 20% extra VIP bonus)!");
					} else {
						plugin.getServer()
								.dispatchCommand(
										plugin.getServer().getConsoleSender(),
										"mail send " + playerName + " " + ChatColor.DARK_AQUA + "[VIP] " + ChatColor.WHITE + "Your account had " + amount
												+ " dollar credited (Including 20% extra VIP bonus)!");
					}
					SlapHomebrew.econ.depositPlayer(playerName, amount);
					sender.sendMessage(ChatColor.DARK_AQUA + "[VIP] " + ChatColor.WHITE + "Gave player " + playerName + " " + amount + " dollars!");
					return true;
				}

				if (arg.equalsIgnoreCase("message")) {
					if (sender instanceof Player) {
						Player player = (Player) sender;
						if (!player.hasPermission("slaphomebrew.vip.message")) {
							player.sendMessage(ChatColor.DARK_AQUA + "[VIP] " + ChatColor.WHITE + "You don't have permission for that!");
							return true;
						}
					}
					List<String> message = new ArrayList<String>();
					for (int i = 1; i < args.length; i++) {
						message.add(args[i]);
					}
					String fullMessage = Joiner.on(" ").join(message);
					plugin.getServer().broadcastMessage(ChatColor.translateAlternateColorCodes('&', fullMessage));
				}

				/*
				 * Vip Forum Promotion Commands
				 */
				VipForumMarkCommands vipForumMarkCommands = plugin.getVipForumMarkCommands();
				if (arg.equalsIgnoreCase("mark")) {
					vipForumMarkCommands.markCommand(sender, args);
					return true;
				}

				if (arg.equalsIgnoreCase("check")) {
					if (sender instanceof Player)
						vipForumMarkCommands.checkCommand((Player) sender, args);
					return true;
				}
				if (arg.equalsIgnoreCase("done")) {
					if (sender instanceof Player)
						vipForumMarkCommands.doneCommand((Player) sender, args);
					return true;
				}
			} else {
				Player player = (Player) sender;
				player.sendMessage(ChatColor.DARK_AQUA + "[VIP] " + ChatColor.WHITE + "That command does not exist!");
			}
		}
		return true;
	}
}
