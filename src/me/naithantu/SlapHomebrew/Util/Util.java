package me.naithantu.SlapHomebrew.Util;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import me.naithantu.SlapHomebrew.SlapHomebrew;
import me.naithantu.SlapHomebrew.Commands.Exception.CommandException;
import me.naithantu.SlapHomebrew.Controllers.Flag;
import me.naithantu.SlapHomebrew.Storage.YamlStorage;
import net.minecraft.server.v1_7_R1.ChatSerializer;
import net.minecraft.server.v1_7_R1.PacketPlayOutChat;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.craftbukkit.v1_7_R1.entity.CraftPlayer;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.BlockIterator;

import ru.tehkode.permissions.PermissionUser;
import ru.tehkode.permissions.bukkit.PermissionsEx;

import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

public class Util {
	
	public static String getHeader() {
		return ChatColor.GOLD + "[SLAP] " + ChatColor.WHITE;
	}

	public static void dateIntoTimeConfig(String date, String message, YamlStorage timeStorage) {
		FileConfiguration timeConfig = timeStorage.getConfig();
		int i = 1;
		while (timeConfig.contains(date)) {
			date += " (" + i + ")";
			i++;
		}
		timeConfig.set(date, message);
		timeStorage.saveConfig();
	}

	public static boolean hasFlag(SlapHomebrew plugin, Location location, Flag flag) {
		RegionManager regionManager = plugin.getworldGuard().getRegionManager(location.getWorld());
		ApplicableRegionSet regions = regionManager.getApplicableRegions(location);
		for (ProtectedRegion region : regions) {
			for (String string : region.getMembers().getPlayers()) {
				if (string.startsWith("flag:" + flag.toString().toLowerCase()))
					return true;
			}
		}
		return false;
	}

	public static String getFlag(SlapHomebrew plugin, Location location, Flag flag) {
		RegionManager regionManager = plugin.getworldGuard().getRegionManager(location.getWorld());
		ApplicableRegionSet regions = regionManager.getApplicableRegions(location);
		for (ProtectedRegion region : regions) {
			for (String string : region.getMembers().getPlayers()) {
				if (string.startsWith("flag:" + flag.toString().toLowerCase()))
					return string;
			}
		}
		return null;
	}
	
	public static List<Flag> getFlags(SlapHomebrew plugin, Location location) {
		List<Flag> flags = new ArrayList<Flag>();
		RegionManager regionManager = plugin.getworldGuard().getRegionManager(location.getWorld());
		ApplicableRegionSet regions = regionManager.getApplicableRegions(location);
		for (ProtectedRegion region : regions) {
			for (String string : region.getMembers().getPlayers()) {
				if (string.startsWith("flag:")) {
					String flagWithoutPrefix = string.replaceFirst("flag:", "");
					String flagName = flagWithoutPrefix.split("\\(")[0];
					try {
						Flag flag = Flag.valueOf(flagName.toUpperCase());
						flags.add(flag);
					} catch (IllegalArgumentException e) {
					}
				}
			}
		}
		return flags;
	}


	public static boolean hasEmptyInventory(Player player) {
		Boolean emptyInv = true;
		PlayerInventory inv = player.getInventory();
		for (ItemStack stack : inv.getContents()) {
			//TODO Is try - catch really required here?
			try {
				if (stack.getType() != (Material.AIR)) {
					emptyInv = false;
				}
			} catch (NullPointerException e) {
			}
		}
		for (ItemStack stack : inv.getArmorContents()) {
			try {
				if (stack.getType() != (Material.AIR)) {
					emptyInv = false;
				}
			} catch (NullPointerException e) {
			}
		}
		return emptyInv;
	}

	public static void broadcastToWorld(String worldName, String message) {
		for (Player player : Bukkit.getServer().getOnlinePlayers()) {
			if (player.getWorld().getName().equalsIgnoreCase(worldName)) {
				player.sendMessage(message);
			}
		}
	}

	public static String changeTimeFormat(long time, String format) {
		final Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(time);
		final String timeString = new SimpleDateFormat(format).format(cal.getTime());
		return timeString;
	}
	
	/**
	 * Create a potion effect
	 * 
	 * @param name The name of the potion
	 * @param time The time it lasts in seconds
	 * @param power The power it has
	 * @return The potioneffect or null if invalid name
	 */
	public static PotionEffect getPotionEffect(String name, int time, int power) {
		time = time * 20;
		switch (name.toLowerCase()) {
		case "nightvision": case "night-vision":
			return new PotionEffect(PotionEffectType.NIGHT_VISION, time, power);
		case "blindness": case "blind":
			return new PotionEffect(PotionEffectType.BLINDNESS, time, power);
		case "confusion": case "nausea": case "confus":
			return new PotionEffect(PotionEffectType.CONFUSION, time, power);
		case "jump":
			return new PotionEffect(PotionEffectType.JUMP, time, power);
		case "slowdig": case "slow-digging": case "slowdigging":
			return new PotionEffect(PotionEffectType.SLOW_DIGGING, time, power);
		case "damageresist": case "damage-resist": case "damageresistance": case "damage-resistance":
			return new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, time, power);
		case "fastdig": case "fast-digging": case "fastdigging":
			return new PotionEffect(PotionEffectType.FAST_DIGGING, time, power);
		case "fireresist": case "fire-resist": case "fireresistance":
			return new PotionEffect(PotionEffectType.FIRE_RESISTANCE, time, power);
		case "harm":
			return new PotionEffect(PotionEffectType.HARM, time, power);
		case "heal":
			return new PotionEffect(PotionEffectType.HEAL, time, power);
		case "hunger":
			return new PotionEffect(PotionEffectType.HUNGER, time, power);
		case "strength": case "power":
			return new PotionEffect(PotionEffectType.INCREASE_DAMAGE, time, power);
		case "invisibility": case "invis":
			return new PotionEffect(PotionEffectType.INVISIBILITY, time, power);
		case "poison":
			return new PotionEffect(PotionEffectType.POISON, time, power);
		case "regeneration": case "regen":
			return new PotionEffect(PotionEffectType.REGENERATION, time, power);
		case "slow": case "slowness":
			return new PotionEffect(PotionEffectType.SLOW, time, power);
		case "speed":
			return new PotionEffect(PotionEffectType.SPEED, time, power);
		case "waterbreathing":
			return new PotionEffect(PotionEffectType.WATER_BREATHING, time, power);
		case "weakness": case "weak":
			return new PotionEffect(PotionEffectType.WEAKNESS, time, power);
		default:
			return null;
		}
	}
	
    public static String colorize(String s){
    	if(s == null) return null;
    	return ChatColor.translateAlternateColorCodes('&', s);
    }
    
    public static String decolorize(String s){
    	if(s == null) return null;
    	return s.replaceAll("&([0-9a-f])", "");
    }
    
    public static void msg(CommandSender sender, String msg) {
    	sender.sendMessage(Util.getHeader() + msg);
	}

    public static void badMsg(CommandSender sender, String msg) {
		if (sender instanceof Player) {
			sender.sendMessage(ChatColor.RED + msg);
		} else {
			sender.sendMessage(msg);
		}
	}

    public static void noPermission(CommandSender sender) {
		sender.sendMessage(ChatColor.RED + "You do not have access to that command.");
	}

    /**
     * Test if a CommandSender has a certain permission. Prepend slaphomebrew.[perm]
     * @param sender The sender
     * @param perm The permission prepended with slaphomebrew.
     * @return has permission
     */
    public static boolean testPermission(CommandSender sender, String perm) {
		if (!(sender instanceof Player) || sender.hasPermission("slaphomebrew." + perm)) return true;
		return false;
	}
    
    /**
     * Test if an offline player has a certain permission
     * @param offPlayer The offlineplayer
     * @param perm The permission perpended with slaphomebrew.
     * @return has permission
     */
    public static boolean checkPermission(OfflinePlayer offPlayer, String perm) {
    	PermissionUser user = PermissionsEx.getUser(offPlayer.getName());
    	if (user == null) return false;
    	return (user.has("slaphomebrew." + perm));
    }
    
    /**
     * Get the time played string
     * Format: X Days, X Hours, X Minutes and X seconds
     * @param l The time played
     * @return The string or 'Unkown'
     */
    public static String getTimePlayedString(long l) {
    	String returnString = "";
    	int t = 0; 
    	l = l / 1000;
    	if (l < 60) {
    		t = 1; //Seconds
    	} else if (l < 3600) {
    		t = 2; //Minutes
    	} else if (l < 86400) {
    		t = 3; //Hours
    	} else {
    		t = 4; //Days
    	}
    	switch (t) {
    	case 4:
    		int days = (int)Math.floor(l / 86400.00);
    		l = l - (days * 86400);
    		returnString = days + (days == 1 ? " day, " : " days, ");
    	case 3:
    		int hours = (int)Math.floor(l / 3600.00);
    		l = l - (hours * 3600);
    		returnString += hours + (hours == 1 ? " hour, " : " hours, ");
    	case 2:
    		int minutes = (int)Math.floor(l / 60.00);
    		l = l - (minutes * 60);
    		returnString += minutes + (minutes == 1 ? " minute" : " minutes") + " and ";
    	case 1:
    		returnString += l + (l == 1 ? " second." : " seconds");
    		break;
    	default:
    		returnString = "Unkown";
    	}
    	return returnString;
    }
    
	/**
	 * Build a string from a string array.
	 * @param split The String array
	 * @param splitChar The string that should be added between each String from the array
	 * @param begin The index it should start on
	 * @return The combined string
	 */
	public static String buildString(String[] split, String splitChar, int begin) {
		return buildString(split, splitChar, begin, 9001);
	}
    
	/**
	 * Build a string from a string array.
	 * @param split The String array
	 * @param splitChar The string that will be added between each String from the array
	 * @param begin The index it should start on
	 * @param end The index it should end with
	 * @return The combined string
	 */
	public static String buildString(String[] split, String splitChar, int begin, int end) {
		String combined = "";
		while (begin <= end && begin < split.length) {
			if (!combined.isEmpty()) {
				combined += splitChar;
			}
			combined += split[begin];
			begin++;
		}
		return combined;
	}
	
	/**
	 * Build a string from a string collection
	 * @param strings The collection of strings
	 * @param splitChar The string that will be added between each string from the list
	 * @return The combined string
	 */
	public static String buildString(Collection<String> strings, String splitChar) {
		String combined = "";
		for (String s : strings) {
			if (!combined.isEmpty()) {
				combined += splitChar;
			}
			combined += s;
		}
		return combined;
	}
    
    /**
     * Get the target block that is NOT air in the line of sight of a player
     * @param entity The entity
     * @param maxDistance Max distance to block
     * @return The block or null
     * @throws CommandException if no block found
     */
    public static Block getTargetBlock(LivingEntity entity, int maxDistance) throws CommandException {
    	BlockIterator iterator = new BlockIterator(entity, maxDistance);
    	while (iterator.hasNext()) {
    		Block foundBlock = iterator.next();
    		if (foundBlock.getType() != Material.AIR) {
    			return foundBlock;
    		}
    	}
    	throw new CommandException("You aren't looking at a block (or out of range)");
    }
    
    /**
     * Broadcast a message to all players. Prepend [SLAP] header.
     * @param message The message
     */
    public static void broadcast(String message) {
    	Bukkit.broadcastMessage(getHeader() + message);
    }
    
    /**
     * Get all blocks in the line of sight of an entity
     * @param entity The entity
     * @param maxDistance The max distance line
     * @return The list with all the blocks
     */
    public static ArrayList<Block> getBlocksInLineOfSight(LivingEntity entity, int maxDistance) {
    	BlockIterator iterator = new BlockIterator(entity, maxDistance);
    	ArrayList<Block> blocks = new ArrayList<>();
    	while (iterator.hasNext()) {
    		blocks.add(iterator.next());
    	}
    	return blocks;
    }
    
    /**
     * Remove all PotionEffects from a player
     * @param p the player
     */
    public static void wipeAllPotionEffects(Player p) {
    	HashSet<PotionEffect> effects = new HashSet<>(p.getActivePotionEffects());
    	for (PotionEffect effect : effects) {
    		p.removePotionEffect(effect.getType());
    	}
    }
    
    /**
     * Send a message to all players with the specified permission
     * @param permission The permission
     * @param message The message
     */
    public static void messagePermissionHolders(String permission, String message) {
    	for (Player p : Bukkit.getOnlinePlayers()) {
    		if (testPermission(p, permission)) {
    			p.sendMessage(message);
    		}
    	}
    }
    
    /**
     * Send a JSON Formatted message to a player
     * @param p The player
     * @param json The JSON message
     */
    public static void sendJsonMessage(Player p, String json) {
    	((CraftPlayer) p)
    		.getHandle()
    		.playerConnection
    		.sendPacket(
    			new PacketPlayOutChat(
    				ChatSerializer.a(json)
    			)
    		);
    }
    
    /**
     * Send a JSON Formatted message to all players
     * @param json The JSON message
     */
    public static void broadcastJsonMessage(String json) {
    	for (Player p : getOnlinePlayers()) {
    		sendJsonMessage(p, json);
    	}
    }
        
    /**
     * Get all the online players
     * @return player array
     */
    public static Player[] getOnlinePlayers() {
    	return Bukkit.getServer().getOnlinePlayers();
    }
    
    /**
     * Get the OfflinePlayer based on their name
     * @param playername The player's name
     * @return the player
     */
    public static OfflinePlayer getOfflinePlayer(String playername) {
    	return Bukkit.getOfflinePlayer(playername);
    }
    
    public static BukkitTask runASync(SlapHomebrew plugin, Runnable runnable) {
    	if (!plugin.isEnabled()) {
    		runnable.run();
    		return null;
    	}
    	return getScheduler(plugin).runTaskAsynchronously(plugin, runnable);
    }
    
    public static BukkitTask runASyncLater(SlapHomebrew plugin, Runnable runnable, int delay) {
    	if (!plugin.isEnabled()) {
    		runnable.run();
    		return null;
    	}
    	return getScheduler(plugin).runTaskLaterAsynchronously(plugin, runnable, delay);
    }
    
    public static BukkitTask runASyncTimer(SlapHomebrew plugin, Runnable runnable, int delay, int period) {
    	if (!plugin.isEnabled()) {
    		runnable.run();
    		return null;
    	}
    	return getScheduler(plugin).runTaskTimerAsynchronously(plugin, runnable, delay, period);
    }
    
    public static BukkitTask run(SlapHomebrew plugin, Runnable runnable) {
    	if (!plugin.isEnabled()) {
    		runnable.run();
    		return null;
    	}
    	return getScheduler(plugin).runTask(plugin, runnable);
    }
    
    public static BukkitTask runLater(SlapHomebrew plugin, Runnable runnable, int delay) {
    	if (!plugin.isEnabled()) {
    		runnable.run();
    		return null;
    	}
    	return getScheduler(plugin).runTaskLater(plugin, runnable, delay);
    }
    
    public static BukkitTask runTimer(SlapHomebrew plugin, Runnable runnable, int delay, int period) {
    	if (!plugin.isEnabled()) {
    		runnable.run();
    		return null;
    	}
    	return getScheduler(plugin).runTaskTimer(plugin, runnable, delay, period);
    }
    
    public static BukkitScheduler getScheduler(SlapHomebrew plugin) {
    	return plugin.getServer().getScheduler();
    }
    
}
