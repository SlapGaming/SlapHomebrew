package me.naithantu.SlapHomebrew.Commands;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import me.naithantu.SlapHomebrew.IconMenu;
import me.naithantu.SlapHomebrew.SlapHomebrew;
import me.naithantu.SlapHomebrew.Vip;
import me.naithantu.SlapHomebrew.VipForumMarkCommands;
import me.naithantu.SlapHomebrew.Storage.YamlStorage;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import ru.tehkode.permissions.PermissionUser;
import ru.tehkode.permissions.bukkit.PermissionsEx;

public class VipCommand extends AbstractVipCommand {

	String header = ChatColor.DARK_AQUA + "[VIP] " + ChatColor.WHITE;

	Integer used = 0;
	HashSet<String> vipItemsList = new HashSet<String>();
	YamlStorage vipStorage;
	FileConfiguration vipConfig;
	Vip vipUtil;

	List<String> vipCommands = Arrays.asList("grant", "copybook", "homes", "list", "grantlist", "resetgrant", "add", "set", "remove", "days", "addhomes", "givemoney", "mark", "check", "done");

	public VipCommand(CommandSender sender, String[] args, SlapHomebrew plugin, YamlStorage vipStorage, Vip vipUtil) {
		super(sender, args, plugin);
		this.vipStorage = vipStorage;
		vipConfig = vipStorage.getConfig();
		this.vipUtil = vipUtil;
	}

	public boolean handle() {
		String arg = null;
		try {
			arg = args[0];
		} catch (ArrayIndexOutOfBoundsException e) {
			Player player = (Player) sender;
			if (vipConfig.getConfigurationSection("vipdays").getInt(player.getName().toLowerCase()) == 0) {
				player.sendMessage(ChatColor.DARK_AQUA + "[VIP] " + ChatColor.WHITE + "You are not a VIP! Go to www.slap-gaming.com/vip for more info about VIP!");
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
			player.sendMessage(ChatColor.DARK_AQUA + "[VIP] " + ChatColor.WHITE + "Not a VIP? Go to slap-gaming.com/vip!");
		}

		if (vipCommands.contains(arg)) {
			if (arg.equalsIgnoreCase("grant")) {
				if (!(sender instanceof Player)) {
					this.badMsg(sender, "You need to be in-game to do that.");
					return true;
				}
				int type;
				Player player = (Player) sender;
				if (player.hasPermission("slaphomebrew.grant")) {
					if (player.getInventory().firstEmpty() == -1) {
						player.sendMessage(ChatColor.DARK_AQUA + "[VIP] " + ChatColor.WHITE + "Your inventory is already full!");
						return true;
					}

					//setupIconMenu(player);

					//TODO Add iconmenu to choose items!
					try {
						arg = args[1];
					} catch (ArrayIndexOutOfBoundsException e) {
						player.sendMessage(ChatColor.DARK_AQUA + "[VIP] " + ChatColor.WHITE + "You haven't chosen an item to be given!");
						return true;
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

					//TODO Make grant system less messy.
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

			if (arg.equalsIgnoreCase("copybook")) {
				if (!(sender instanceof Player)) {
					this.badMsg(sender, "You need to be in-game to do that.");
					return true;
				}
				Player player = (Player) sender;
				if (player.hasPermission("slaphomebrew.grant")) {
					ItemStack bookToCopy = player.getItemInHand();
					if (!(bookToCopy.getType() == Material.WRITTEN_BOOK)) {
						this.badMsg(sender, "You're not holding a book!");
						return true;
					}

					String playerName = player.getName();
					if (SlapHomebrew.usedGrant.containsKey(playerName)) {
						try {
							used = ((Integer) SlapHomebrew.usedGrant.get(playerName).intValue());
						} catch (NullPointerException e) {
						}
						if (used < 3) {
							used += 1;
							player.getInventory().addItem(bookToCopy);
							player.sendMessage(ChatColor.DARK_AQUA + "[VIP] " + ChatColor.WHITE + "Book copied.");
							int timesleft = 3 - used;
							player.sendMessage(ChatColor.DARK_AQUA + "[VIP] " + ChatColor.WHITE + timesleft + " times left today.");
							SlapHomebrew.usedGrant.put(playerName, used);
						} else {
							player.sendMessage(ChatColor.DARK_AQUA + "[VIP] " + ChatColor.WHITE + "You have already used this 3 times today!");
						}
					} else {
						used = 1;
						SlapHomebrew.usedGrant.put(playerName, used);
						int timesleft = 3 - used;
						player.getInventory().addItem(bookToCopy);
						player.sendMessage(ChatColor.DARK_AQUA + "[VIP] " + ChatColor.WHITE + "Book copied.");
						player.sendMessage(ChatColor.DARK_AQUA + "[VIP] " + ChatColor.WHITE + timesleft + " times left today.");
					}
				} else {
					player.sendMessage(ChatColor.DARK_AQUA + "[VIP] " + ChatColor.WHITE + "You don't have permission for that! Go to slap-gaming.com/vip!");
				}
			}

			if (arg.equalsIgnoreCase("homes")) {
				sender.sendMessage(header + "You are allowed to set " + vipUtil.getHomes(sender.getName()) + " homes!");
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
						Map.Entry me = (Map.Entry) i.next(); //TODO
						Material vipItemsMaterial = Material.getMaterial((Integer) me.getKey());
						vipItemsList.add(vipItemsMaterial.toString().toLowerCase());
					}
					player.sendMessage(ChatColor.DARK_AQUA + "[VIP] " + ChatColor.WHITE + "VIPs can spawn the items: " + vipItemsList.toString());
				} else {
					player.sendMessage(ChatColor.DARK_AQUA + "[VIP] " + ChatColor.WHITE + "You don't have permission for that! Go to slap-gaming.com/vip!");
				}
			}

			if (arg.equalsIgnoreCase("resetgrant")) {
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
							+ amount + " dollar credited (Including 20% extra VIP bonus)!");
				}
				SlapHomebrew.econ.depositPlayer(playerName, amount);
				sender.sendMessage(ChatColor.DARK_AQUA + "[VIP] " + ChatColor.WHITE + "Gave player " + playerName + " " + amount + " dollars!");
				return true;
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

		return true;
	}

	public void setupIconMenu(Player player) {
		//TODO Remove iconMenu stuff here, just testing.
		IconMenu iconMenu = new IconMenu("vipMenu", 9, new IconMenu.OptionClickEventHandler() {
			@Override
			public void onOptionClick(IconMenu.OptionClickEvent event) {
				event.getPlayer().sendMessage("You have chosen " + event.getName());
				event.setWillClose(true);
				event.setWillDestroy(true);
			}
		}, plugin).setOption(3, new ItemStack(Material.APPLE, 1), "Food", "The food is delicious").setOption(4, new ItemStack(Material.IRON_SWORD, 1), "Weapon", "Weapons are for awesome people")
				.setOption(5, new ItemStack(Material.EMERALD, 1), "Money", "Money brings happiness");
		iconMenu.open(player);
	}
}
