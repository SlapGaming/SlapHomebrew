package me.naithantu.SlapHomebrew.Commands;

import java.util.HashSet;

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
		String arg;
		try {
			arg = args[0];
		} catch (ArrayIndexOutOfBoundsException e) {
			this.msg(sender, "SlapHomebrew is a plugin which adds extra commands to the SlapGaming Server.");
			this.msg(sender, "Current SlapHomebrew Version: " + plugin.getDescription().getVersion());
			return true;
		}

		if (arg.equalsIgnoreCase("reload")) {
			if (this.testPermission(sender, "manage")) {
				Bukkit.getPluginManager().disablePlugin(plugin);
				Bukkit.getPluginManager().enablePlugin(plugin);
				sender.sendMessage(ChatColor.GRAY + "SlapHomebrew has been reloaded...");
			}
		}

		if (!(sender instanceof Player)) {
			this.badMsg(sender, "You need to be in-game to do that.");
			return true;
		}

		final Player player = (Player) sender;

		if (arg.equalsIgnoreCase("retrobow")) {
			if (!testPermission(player, "fun")) {
				this.noPermission(sender);
				return true;
			}
			if (!retroBow.contains(player.getName())) {
				retroBow.add(player.getName());
				this.msg(sender, "Retrobow mode has been turned on!");
			} else {
				retroBow.remove(player.getName());
				this.msg(sender, "Retrobow mode has been turned off!");
			}
		}

		if (arg.equalsIgnoreCase("wolf")) {
			World world = player.getWorld();
			if (!testPermission(player, "fun")) {
				this.noPermission(sender);
				return true;
			}
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
			if (Integer.valueOf(arg) <= 20) {
				for (x = 0; x < Integer.valueOf(arg); x++) {
					world.spawn(spawnLocation, Wolf.class).setOwner(player);
				}
			} else {
				player.sendMessage(ChatColor.RED + "You are not allowed to spawn more then 20 wolves at the same time!");
			}
		}

		if (arg.equalsIgnoreCase("cat")) {
			World world = player.getWorld();
			Type type = null;
			if (!testPermission(player, "fun")) {
				this.noPermission(sender);
				return true;
			}
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
				if (Integer.valueOf(arg) <= 20) {
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
		}
		if (arg.equalsIgnoreCase("removewolf")) {
			if (!testPermission(player, "fun")) {
				this.noPermission(sender);
				return true;
			}
			for (Entity entity : player.getNearbyEntities(50, 50, 50)) {
				if (entity instanceof Wolf) {
					if (((Wolf) entity).getOwner().equals(player)) {
						entity.remove();
					}
				}
			}
		}
		if (arg.equalsIgnoreCase("removecat")) {
			if (!testPermission(player, "fun")) {
				this.noPermission(sender);
				return true;
			}
			for (Entity entity : player.getNearbyEntities(50, 50, 50)) {
				if (entity instanceof Ocelot) {
					if (((Ocelot) entity).getOwner().equals(player)) {
						entity.remove();
					}
				}
			}
		}

		if (arg.equals("notify")) {
			if (!testPermission(player, "notify")) {
				this.noPermission(sender);
				return true;
			}
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
		return true;
	}
}
