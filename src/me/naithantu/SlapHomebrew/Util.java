package me.naithantu.SlapHomebrew;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import me.naithantu.SlapHomebrew.Storage.YamlStorage;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

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
		RegionManager regionManager = plugin.getWorldGuard().getRegionManager(location.getWorld());
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
		RegionManager regionManager = plugin.getWorldGuard().getRegionManager(location.getWorld());
		ApplicableRegionSet regions = regionManager.getApplicableRegions(location);
		for (ProtectedRegion region : regions) {
			for (String string : region.getMembers().getPlayers()) {
				if (string.startsWith("flag:" + flag.toString().toLowerCase()))
					return string;
			}
		}
		return null;
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
	
	public static PotionEffect getPotionEffect(String name, int time, int power) {
		name = name.toLowerCase();
		time = time * 20;
		PotionEffect effect = null;
		if (name.equals("nightvision")) {
			effect = new PotionEffect(PotionEffectType.NIGHT_VISION, time, power);
		} else if (name.equals("blindness")) {
			effect = new PotionEffect(PotionEffectType.BLINDNESS, time, power);
		} else if (name.equals("confusion")) {
			effect = new PotionEffect(PotionEffectType.CONFUSION, time, power);
		} else if (name.equals("jump")) {
			effect = new PotionEffect(PotionEffectType.JUMP, time, power);
		} else if (name.equals("slowdig")) {
			effect = new PotionEffect(PotionEffectType.SLOW_DIGGING, time, power);
		} else if (name.equals("damageresist")) {
			effect = new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, time, power);
		} else if (name.equals("fastdig")) {
			effect = new PotionEffect(PotionEffectType.FAST_DIGGING, time, power);
		} else if (name.equals("fireresist")) {
			effect = new PotionEffect(PotionEffectType.FIRE_RESISTANCE, time, power);
		} else if (name.equals("harm")) {
			effect = new PotionEffect(PotionEffectType.HARM, time, power);
		} else if (name.equals("heal")) {
			effect = new PotionEffect(PotionEffectType.HEAL, time, power);
		} else if (name.equals("hunger")) {
			effect = new PotionEffect(PotionEffectType.HUNGER, time, power);
		} else if (name.equals("strength")) {
			effect = new PotionEffect(PotionEffectType.INCREASE_DAMAGE, time, power);
		} else if (name.equals("invisibility")) {
			effect = new PotionEffect(PotionEffectType.INVISIBILITY, time, power);
		} else if (name.equals("poison")) {
			effect = new PotionEffect(PotionEffectType.POISON, time, power);
		} else if (name.equals("regeneration")) {
			effect = new PotionEffect(PotionEffectType.REGENERATION, time, power);
		} else if (name.equals("slow")) {
			effect = new PotionEffect(PotionEffectType.SLOW, time, power);
		} else if (name.equals("speed")) {
			effect = new PotionEffect(PotionEffectType.SPEED, time, power);
		} else if (name.equals("waterbreathing")) {
			effect = new PotionEffect(PotionEffectType.WATER_BREATHING, time, power);
		} else if (name.equals("weakness")) {
			effect = new PotionEffect(PotionEffectType.WEAKNESS, time, power);
		}
		return effect;
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
		if (sender instanceof Player) {
			sender.sendMessage(Util.getHeader() + msg);
		} else {
			sender.sendMessage("[SLAP] " + msg);
		}
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

    public static boolean testPermission(CommandSender sender, String perm) {
		String permission = "slaphomebrew." + perm;
		if (!(sender instanceof Player) || sender.hasPermission(permission))
			return true;
		return false;
	}
}
