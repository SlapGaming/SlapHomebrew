package me.naithantu.SlapHomebrew;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import me.naithantu.SlapHomebrew.Storage.YamlStorage;

public class Sonic {
	SlapHomebrew plugin;
	YamlStorage sonicStorage;
	FileConfiguration sonicConfig;
	HashMap<String, SonicPlayer> players = new HashMap<String, SonicPlayer>();
	List<String> leaderboard = new ArrayList<String>();

	public Sonic(SlapHomebrew plugin) {
		this.plugin = plugin;
		sonicStorage = plugin.getSonicStorage();
		sonicConfig = sonicStorage.getConfig();
		generateLeaderboard();
	}

	public void teleportSonic(String playerName) {
		Player player = Bukkit.getServer().getPlayer(playerName);
		final World world = Bukkit.getServer().getWorld("world_sonic");
		player.teleport(new Location(world, 1399, 68, -424.5, 180, 0));
		player.getInventory().clear();
		player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 999999, 1));
		player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, 999999, 1));
		player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 999999, 999999));
		player.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 999999, 3));
		ItemStack boots = new ItemStack(Material.LEATHER_BOOTS);
		boots.addUnsafeEnchantment(Enchantment.PROTECTION_FALL, 10);
		boots.addUnsafeEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 10);
		boots.addUnsafeEnchantment(Enchantment.PROTECTION_EXPLOSIONS, 10);
		boots.addUnsafeEnchantment(Enchantment.DURABILITY, 10);
		LeatherArmorMeta itemMeta = (LeatherArmorMeta) boots.getItemMeta();
		itemMeta.setColor(Color.BLUE);
		itemMeta.setDisplayName(ChatColor.AQUA + "" + ChatColor.ITALIC + "Sonic Shoes");
		boots.setItemMeta(itemMeta);
		player.getInventory().setBoots(boots);
		player.setGameMode(GameMode.SURVIVAL);
		players.put(playerName, new SonicPlayer(this, playerName));
	}

	public void addCheckpoint(String playerName, int checkpoint) {
		//If player is racing, add checkpoint.
		if (players.containsKey(playerName)) {
			players.get(playerName).addCheckpoint(checkpoint);
			return;
		}

		//Otherwise, teleport them to the start.
		teleportSonic(playerName);
		Bukkit.getServer().getPlayer(playerName).sendMessage(ChatColor.GOLD + "[SLAP] " + ChatColor.WHITE + " You weren't racing, you've been teleported to the start!");
	}
	
	public void addJump(String playerName, int jump) {
		//If player is racing, add jump.
		if (!players.containsKey(playerName)) {
			Bukkit.getServer().getPlayer(playerName).sendMessage(ChatColor.GOLD + "[SLAP] " + ChatColor.WHITE + " Do not get in the way of players!");
			teleportSonic(playerName);
			return;
		}
		
		for(String name: players.keySet()){
			SonicPlayer sonicPlayer = players.get(name);
			if(!sonicPlayer.getPlayerName().equals(playerName)){
				int lastJump = sonicPlayer.getLastJump();
				if(jump == lastJump){
					long lastJumpTime = sonicPlayer.getLastJumpTime();
					long currentJumpTime = new Date().getTime();
					if(currentJumpTime - lastJumpTime < 2000){
						teleportSonic(playerName);
						Bukkit.getServer().getPlayer(playerName).sendMessage(ChatColor.GOLD + "[SLAP] " + ChatColor.WHITE + "You may not jump that quick after a previous player, you have been teleported to the start!");
						return;
					}
				}
			}	
		}
		
		players.get(playerName).addJump(jump);

	}

	public boolean addHighscore(String playerName, long[] checkpointTimes) {
		String configKey = "players." + playerName.toLowerCase();
		String configTimes = sonicConfig.getString(configKey);
		if (!sonicConfig.contains(configKey) || Long.parseLong(configTimes.split(":")[5]) > checkpointTimes[5]) {
			String configString = "";
			for(long checkpointTime: checkpointTimes){
				configString = configString + checkpointTime + ":";
			}
			sonicConfig.set(configKey, configString);
			sonicStorage.saveConfig();
			generateLeaderboard();
			return true;
		}
		return false;
	}

	public List<String> getLeaderboard() {
		return leaderboard;
	}

	private void generateLeaderboard() {
		leaderboard.clear();
		if (sonicConfig.getConfigurationSection("players") != null) {
			ConfigurationSection players = sonicConfig.getConfigurationSection("players");
			Set<String> names = players.getKeys(false);
			for (String name : names) {
				Long playerTime = getTotalTime(name);
				if (leaderboard.isEmpty()) {
					leaderboard.add(name);
				} else {
					for (int i = 0; i < leaderboard.size(); i++) {
						String sortedName = leaderboard.get(i);
						if (getTotalTime(sortedName) > playerTime) {
							leaderboard.add(i, name);
							break;
						} else if (i == leaderboard.size() - 1) {
							leaderboard.add(name);
							break;
						}
					}
				}
			}
		}
	}
	
	public long getTotalTime(String playerName){
		String playerTimes = sonicConfig.getString("players." + playerName.toLowerCase());
		return Long.parseLong(playerTimes.split(":")[5]);
	}
	
	public static String changeTimeFormat(long time) {
		final Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(time);
		final String timeString = new SimpleDateFormat("mm:ss:SS").format(cal.getTime());
		return timeString;
	}
}
