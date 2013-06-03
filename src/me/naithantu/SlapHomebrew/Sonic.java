package me.naithantu.SlapHomebrew;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
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
		player.teleport(new Location(world, 1355.5, 68, -416.5, 180, 0));
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

	public boolean addHighscore(String playerName, long time) {
		String configKey = "players." + playerName;
		if (!sonicConfig.contains(configKey) || sonicConfig.getLong(configKey) > time) {
			sonicConfig.set(configKey, time);
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
				Long playerTime = sonicConfig.getLong("players." + name);
				if (leaderboard.isEmpty()) {
					leaderboard.add(name);
				} else {
					for (int i = 0; i < leaderboard.size(); i++) {
						String sortedName = leaderboard.get(i);
						if (sonicConfig.getLong("players." + sortedName) > playerTime) {
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
		System.out.println(leaderboard);
	}
}
