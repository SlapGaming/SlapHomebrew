package me.naithantu.SlapHomebrew.Commands;

import java.util.HashSet;

import me.naithantu.SlapHomebrew.Bump;
import me.naithantu.SlapHomebrew.Lottery;
import me.naithantu.SlapHomebrew.SlapHomebrew;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Ocelot;
import org.bukkit.entity.Player;
import org.bukkit.entity.Tameable;
import org.bukkit.entity.Wolf;
import org.bukkit.entity.Ocelot.Type;

public class SlapCommand extends AbstractVipCommand {

	String header = ChatColor.DARK_AQUA + "[VIP] " + ChatColor.WHITE;

	Integer used = 0;
	HashSet<String> vipItemsList = new HashSet<String>();
	public static HashSet<String> retroBow = new HashSet<String>();

	SlapHomebrew plugin;
	Lottery lottery;

	public SlapCommand(CommandSender sender, String[] args, SlapHomebrew plugin) {
		super(sender, args);
		this.plugin = plugin;
	}

	public boolean handle() {
		final Player player = (Player) sender;
		String arg;
		try {
			arg = args[0];
		} catch (ArrayIndexOutOfBoundsException e) {
			player.sendMessage(ChatColor.DARK_AQUA + "SlapHomebrew is a plugin which adds extra commands to the SlapGaming Server.");
			player.sendMessage(ChatColor.DARK_AQUA + "For A List Of Commands Under /Slap Type /Slap Help!");
			player.sendMessage(ChatColor.DARK_AQUA + "Current SlapHomebrew Version: " + plugin.getDescription().getVersion());
			return true;
		}
		if (arg.equalsIgnoreCase("help")) {
			if (player.hasPermission("slaphomebrew.fun")) {
				player.sendMessage(ChatColor.DARK_AQUA + "Fun Commands: retrobow, wolf, removewolf, cat, removecat");
			}
			if (player.hasPermission("slaphomebrew.manage")) {
				player.sendMessage(ChatColor.DARK_AQUA + "Manage Commands: minecart, resetchatbot, amsgtimer, debug, reload, backdeath, config, vip");
			}
			player.sendMessage(ChatColor.DARK_AQUA + "Extra Commands: help, info");
		}

		if (arg.equalsIgnoreCase("retrobow")) {
			if (player.hasPermission("slaphomebrew.fun")) {
				if (!retroBow.contains(player.getName())) {
					retroBow.add(player.getName());
					player.sendMessage("Retrobow mode has been turned on!");
				} else {
					retroBow.remove(player.getName());
					player.sendMessage("Retrobow mode has been turned off!");
				}
			} else {
				player.sendMessage(ChatColor.RED + "You do not have access to that command.");
			}
		}

		if (arg.equalsIgnoreCase("wolf")) {
			World world = player.getWorld();
			if (player.hasPermission("slaphomebrew.fun") || player.getName().equals("naithantu") || player.getName().equals("Telluur")) {
				try {
					arg = args[1];
				} catch (ArrayIndexOutOfBoundsException e) {
					// e.printStackTrace();
				}
				Location spawnLocation = player.getEyeLocation();
				int x;
				try {
					Integer.valueOf(arg);
				} catch (NumberFormatException e) {
					world.spawn(spawnLocation, Wolf.class).setOwner(player);
					return true;
				}
				if (Integer.valueOf(arg) < 20) {
					for (x = 0; x < Integer.valueOf(arg); x++) {
						world.spawn(spawnLocation, Wolf.class).setOwner(player);
					}
				} else {
					player.sendMessage(ChatColor.RED + "You are not allowed to spawn more then 20 wolves at the same time!");
				}
			} else {
				player.sendMessage(ChatColor.RED + "You do not have access to that command.");
			}
		}
		if (arg.equalsIgnoreCase("attackwolf")) {
			try {
				arg = args[0];
			} catch (ArrayIndexOutOfBoundsException e) {
				return true;
			}
			Player targetPlayer = plugin.getServer().getPlayer(arg);

			if (player.hasPermission("slaphomebrew.fun")) {
				for (Entity entity : player.getNearbyEntities(10, 10, 10)) {
					if (entity instanceof Wolf) {
						if (((Tameable) entity).getOwner().equals(player)) {
							player.sendMessage("Your wolves are now attacking " + arg);
							player.sendMessage(ChatColor.RED + "This command is under development and currently does not work!");
							((Wolf) entity).setTarget(targetPlayer);
						}
					}
				}
			}
		}
		if (arg.equalsIgnoreCase("cat")) {
			World world = player.getWorld();
			Type type = null;
			if (player.hasPermission("slaphomebrew.fun") || player.getName().equals("naithantu") || player.getName().equals("Telluur")) {
				try {
					arg = args[1];
				} catch (ArrayIndexOutOfBoundsException e) {
					return true;
				}
				if (arg.equalsIgnoreCase("siamesecat")) {
					type = Type.SIAMESE_CAT;
				} else if (arg.equalsIgnoreCase("blackcat")) {
					type = Type.BLACK_CAT;
				} else if (arg.equalsIgnoreCase("redcat")) {
					type = Type.RED_CAT;
				} else if (arg.equalsIgnoreCase("wildocelot")) {
					type = Type.WILD_OCELOT;
				} else {
					player.sendMessage(ChatColor.RED + "Type not recognized. Only siamesecat, blackcat, wildocelot and redcat are allowed!");
				}
				if (type != null) {
					Location spawnLocation = player.getEyeLocation();
					try {
						arg = args[2];
					} catch (ArrayIndexOutOfBoundsException e) {
						world.spawn(spawnLocation, Ocelot.class);
						for (Entity entity : player.getNearbyEntities(1, 1, 1)) {
							if (entity instanceof Ocelot) {
								((Tameable) entity).setOwner(player);
								((Ocelot) entity).setCatType(type);
							}
						}
						return true;
					}
					try {
						Integer.valueOf(arg);
					} catch (NumberFormatException e) {
						world.spawn(spawnLocation, Ocelot.class);
						for (Entity entity : player.getNearbyEntities(1, 1, 1)) {
							if (entity instanceof Ocelot) {
								((Tameable) entity).setOwner(player);
								((Ocelot) entity).setCatType(type);
							}
						}
						return true;
					}
					if (Integer.valueOf(arg) < 21) {
						int x;
						for (x = 0; x < Integer.valueOf(arg); x++) {
							world.spawn(spawnLocation, Ocelot.class);
							for (Entity entity : player.getNearbyEntities(1, 1, 1)) {
								if (entity instanceof Ocelot) {
									((Tameable) entity).setOwner(player);
									((Ocelot) entity).setCatType(type);
								}
							}
						}
					} else {
						player.sendMessage(ChatColor.RED + "You are not allowed to spawn more then 20 cats at the same time!");
					}
				}

			} else {
				player.sendMessage(ChatColor.RED + "You do not have access to that command.");
			}
		}
		if (arg.equalsIgnoreCase("removewolf")) {
			if (player.hasPermission("slaphomebrew.fun") || player.equals("naithantu") || player.getName().equals("telluur")) {
				for (Entity entity : player.getNearbyEntities(50, 50, 50)) {
					if (entity instanceof Wolf) {
						if (((Wolf) entity).getOwner().equals(player)) {
							entity.remove();
						}
					}
				}
			}
		}
		if (arg.equalsIgnoreCase("removecat")) {
			if (player.hasPermission("slaphomebrew.fun") || player.getName().equals("naithantu") || player.getName().equals("telluur")) {
				for (Entity entity : player.getNearbyEntities(50, 50, 50)) {
					if (entity instanceof Ocelot) {
						if (((Ocelot) entity).getOwner().equals(player)) {
							entity.remove();
						}
					}
				}
			}
		}

		if (arg.equalsIgnoreCase("advisor")) {
			if (player.hasPermission("slaphomebrew.advisor")) {
				try {
					arg = args[1];
				} catch (ArrayIndexOutOfBoundsException e) {
					player.sendMessage(ChatColor.RED + "Usage: /slap advisor list/[username]");
				}
				if (arg.equalsIgnoreCase("list")) {
					player.sendMessage(SlapHomebrew.guides.toString());
				} else {
					if (!SlapHomebrew.guides.contains(arg)) {
						SlapHomebrew.guides.add(arg);
						plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), "pex user " + arg + " prefix \"&6[Advisor]&f \"");
						player.sendMessage(ChatColor.DARK_AQUA + "[SLAP] " + ChatColor.WHITE + arg + " now has a [Advisor] Prefix!");
					} else {
						SlapHomebrew.guides.remove(arg);
						plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), "pex user " + arg + " prefix \"\"");
						player.sendMessage(ChatColor.DARK_AQUA + "[SLAP] " + ChatColor.WHITE + arg + " no longer has a [Advisor] Prefix!");
					}
				}
			}
		}

		if (arg.equalsIgnoreCase("lottery")) {
			if (player.hasPermission("slaphomebrew.manage")) {
				player.sendMessage(lottery.getLottery().toString());
			}
		}

		if (arg.equalsIgnoreCase("startlottery")) {
			if (player.hasPermission("slaphomebrew.manage")) {
				lottery.startLottery();
			}
		}

		if (arg.equalsIgnoreCase("worldguard")) {
			if (player.hasPermission("slaphomebrew.manage")) {
				player.sendMessage(SlapHomebrew.worldGuard.toString());
			}
		}

		if (arg.equalsIgnoreCase("minecart")) {
			if (player.hasPermission("slaphomebrew.manage")) {
				player.sendMessage("Minecart HashSet: " + SlapHomebrew.mCarts.toString());
			} else {
				player.sendMessage(ChatColor.RED + "You do not have access to that command.");
				player.sendMessage(ChatColor.RED + "Type /minecart for the minecart command.");
			}
		}

		if (arg.equalsIgnoreCase("resetchatbot")) {
			if (player.hasPermission("slaphomebrew.manage")) {
				plugin.setReloadChatBot(true);
				plugin.setupChatBot();
				player.sendMessage(ChatColor.DARK_AQUA + "[SLAP] " + ChatColor.WHITE + "The chat bot messages have been reset!");
			} else {
				player.sendMessage(ChatColor.RED + "You do not have access to that command.");
			}
		}

		if (arg.equalsIgnoreCase("amsgtimer")) {
			if (player.hasPermission("slaphomebrew.manage")) {
				try {
					arg = args[1];
				} catch (ArrayIndexOutOfBoundsException e) {
					player.sendMessage(ChatColor.RED + "Usage: /slap amsgtimer <restart/change>");
					return true;
				}

				if (arg.equalsIgnoreCase("restart")) {
					Bump bump = plugin.getBump();
					bump.cancelTimer();
					bump.bumpTimer();
					player.sendMessage(ChatColor.DARK_AQUA + "[SLAP] " + ChatColor.WHITE + "The amsg timer has been restarted!");
				} else if (arg.equalsIgnoreCase("change")) {
					int intArg;
					try {
						intArg = Integer.valueOf(arg) * 20;
					} catch (NumberFormatException e) {
						return true;
					}
					plugin.timerTime = intArg;
					player.sendMessage("Changed the time to " + arg + " minutes");
					player.sendMessage(ChatColor.RED + "Don't use this command, this command is deprecated!");
				}
			}
		}
		if (arg.equalsIgnoreCase("debug")) {
			if (player.hasPermission("slaphomebrew.manage")) {
				if (plugin.debug == true) {
					plugin.debug = false;
					player.sendMessage("Debug mode is now off!");
				} else {
					plugin.debug = true;
					player.sendMessage("Debug mode is now on!");
				}
			}
		}
		if (arg.equalsIgnoreCase("info")) {
			player.sendMessage(plugin.getDescription().getVersion());
		}
		if (arg.equalsIgnoreCase("reload")) {
			if (sender instanceof Player) {
				if (player.hasPermission("slaphomebrew.manage")) {
					Bukkit.getPluginManager().disablePlugin(plugin);
					Bukkit.getPluginManager().enablePlugin(plugin);
					player.sendMessage(ChatColor.GRAY + "SlapHomebrew has been reloaded...");
				}
			}
		}

		if (arg.equalsIgnoreCase("backdeath")) {
			if (player.hasPermission("slaphomebrew.manage")) {
				player.sendMessage(SlapHomebrew.backDeath.toString());
			} else {
				player.sendMessage(ChatColor.RED + "You do not have access to that command.");
				player.sendMessage(ChatColor.RED + "For the backdeath command, type /backdeath.");
			}
		}

		if (arg.equalsIgnoreCase("config")) {
			if (player.hasPermission("slaphomebrew.manage")) {
				try {
					arg = args[1];
				} catch (ArrayIndexOutOfBoundsException e) {
					player.sendMessage(ChatColor.RED + "Either use: reload, clearuses, clearvip or:");
					player.sendMessage(ChatColor.RED + "Choose a section to output: vipitems, vipuses, vipdate, chatmessages.[something]");
					return true;
				}
				if (arg.equalsIgnoreCase("reload")) {
					plugin.saveTheConfig();
					player.sendMessage(ChatColor.DARK_AQUA + "[SLAP] " + ChatColor.WHITE + "The config file has been reloaded!");

				} else if (arg.equalsIgnoreCase("clearuses")) {
					plugin.getConfig().set("vipuses", null);
					player.sendMessage(ChatColor.DARK_AQUA + "[SLAP] " + ChatColor.WHITE + "The vip uses have been cleared out of the config!");

				} else if (arg.equalsIgnoreCase("clearvip")) {
					plugin.getConfig().set("vipitems", null);
					player.sendMessage(ChatColor.DARK_AQUA + "[SLAP] " + ChatColor.WHITE + "The vip items have been cleared out of the config!");

				} else if (arg.equalsIgnoreCase("vipitems")) {
					try {
						player.sendMessage(plugin.getConfig().get("vipitems").toString());
					} catch (Exception e) {
						player.sendMessage("Error: " + e.toString());
					}
				} else if (arg.equalsIgnoreCase("vipuses")) {
					try {
						player.sendMessage(plugin.getConfig().get("vipUses").toString());
					} catch (Exception e) {
						player.sendMessage("Error: " + e.toString());
					}
				} else if (arg.equalsIgnoreCase("vipdate")) {
					try {
						player.sendMessage(plugin.getConfig().get("vipdate").toString());
					} catch (Exception e) {
						player.sendMessage("Error: " + e.toString());
					}
				} else if (arg.contains("chatmessages")) {
					try {
						player.sendMessage(plugin.getConfig().get(arg).toString());
					} catch (Exception e) {
						player.sendMessage("Error: " + e.toString());
					}
				}
			}
		}
		if (arg.equalsIgnoreCase("vip")) {
			if (player.hasPermission("slaphomebrew.manage")) {
				try {
					arg = args[1];
				} catch (ArrayIndexOutOfBoundsException e) {
					player.sendMessage(ChatColor.RED + "Usage: slap vip <loaduses/loadvip/saveuses/savevip>");
				}
				if (arg.equalsIgnoreCase("loaduses")) {
					plugin.loadUses();
					player.sendMessage(ChatColor.DARK_AQUA + "[SLAP] " + ChatColor.WHITE + "The vip uses were reloaded!");
				} else if (arg.equalsIgnoreCase("loadvip")) {
					plugin.loadItems();
					player.sendMessage(ChatColor.DARK_AQUA + "[SLAP] " + ChatColor.WHITE + "The vip items were reloaded!");
				} else if (arg.equalsIgnoreCase("saveuses")) {
					plugin.saveUses();
					player.sendMessage(ChatColor.DARK_AQUA + "[SLAP] " + ChatColor.WHITE + "The vip uses were saved into the config!");
				} else if (arg.equalsIgnoreCase("savevip")) {
					plugin.saveItems();
					player.sendMessage(ChatColor.DARK_AQUA + "[SLAP] " + ChatColor.WHITE + "The vip items were saved into the config!");
				}
			}
		}

		if (arg.equals("notify")) {
			if (player.hasPermission("slaphomebrew.notify")) {
				player.playSound(player.getLocation(), Sound.NOTE_PIANO, 1, 1);
				plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
					public void run() {
						player.playSound(player.getLocation(), Sound.NOTE_PIANO, 1, 2);
						plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
							public void run() {
								player.playSound(player.getLocation(), Sound.NOTE_PIANO, 1, 1);
								plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
									public void run() {
										player.playSound(player.getLocation(), Sound.NOTE_PIANO, 2000, 2);
									}
								}, 5);
							}
						}, 5);
					}
				}, 5);

			}
		}

		return true;
	}
}
