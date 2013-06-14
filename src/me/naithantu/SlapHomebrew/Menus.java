package me.naithantu.SlapHomebrew;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import me.naithantu.SlapHomebrew.Storage.YamlStorage;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;

public class Menus {
	SlapHomebrew plugin;
	IconMenu vipMenu;

	IconMenu woodMenu;
	IconMenu stoneMenu;
	IconMenu sandMenu;

	YamlStorage vipStorage;
	FileConfiguration vipConfig;

	public Menus(SlapHomebrew plugin) {
		this.plugin = plugin;
		vipStorage = plugin.getVipGrantStorage();
		vipConfig = vipStorage.getConfig();
		vipMenu();
		woodMenu();
		stoneMenu();
		sandMenu();
	}

	public IconMenu getVipMenu() {
		return vipMenu;
	}

	public IconMenu getWoodMenu() {
		return woodMenu;
	}

	public IconMenu StoneMenu() {
		return stoneMenu;
	}

	public IconMenu getSandMenu() {
		return sandMenu;
	}

	public void vipMenu() {
		//Get vip items from config.
		YamlStorage vipStorage = plugin.getVipGrantStorage();
		FileConfiguration vipConfig = vipStorage.getConfig();
		List<String> vipItems = new ArrayList<String>();

		if (vipConfig.getConfigurationSection("vipitems.normal") == null) {
			System.out.println("No vip items found, putting new values into config!");
			generateVipItems();
		}

		Set<String> keys = vipConfig.getConfigurationSection("vipitems.normal").getKeys(false);
		for (String string : keys) 
			vipItems.add(string);

		//Create iconmenu with correct amount of rows.
		vipMenu = new IconMenu("Vip grant menu", (int) (Math.ceil((vipItems.size()) / 9.0) + 1) * 9, new IconMenu.OptionClickEventHandler() {
			@Override
			public void onOptionClick(IconMenu.OptionClickEvent event) {
				event.setWillClose(false);
				final Player player = event.getPlayer();
				if (event.getPosition() == 2) {
					player.closeInventory();
					Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
						public void run() {
							woodMenu.open(player);
						}
					}, 1);
					return;
				}
				if (event.getPosition() == 4) {
					player.closeInventory();
					Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
						public void run() {
							sandMenu.open(player);
						}
					}, 1);
					return;
				}
				if (event.getPosition() == 6) {
					player.closeInventory();
					Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
						public void run() {
							stoneMenu.open(player);
						}
					}, 1);
					return;
				}
				handleSubMenu(event);
			}
		}, plugin);

		//Fill iconmenu with items.
		vipMenu.setOption(2, new ItemStack(Material.LOG, 0), "Wood sub menu.", "Click for more wood and leaves types.");
		vipMenu.setOption(4, new ItemStack(Material.SANDSTONE, 0), "Sandstone sub menu", "Click for more sandstone types.");
		vipMenu.setOption(6, new ItemStack(Material.SMOOTH_BRICK, 0), "Stone sub menu", "Click for more stone types.");
		fillIconMenu(vipMenu, vipItems, "vipitems.normal", 1);

	}
	
	public void woodMenu() {
		List<String> vipItems = new ArrayList<String>();
		Set<String> keys = vipConfig.getConfigurationSection("vipitems.wood").getKeys(false);
		for (String string : keys) {
			vipItems.add(string);
		}
		woodMenu = new IconMenu("Vip grant wood menu", (int) Math.ceil(vipItems.size() / 9.0) * 9, new IconMenu.OptionClickEventHandler() {
			@Override
			public void onOptionClick(IconMenu.OptionClickEvent event) {
				handleSubMenu(event);
			}
		}, plugin);

		fillIconMenu(woodMenu, vipItems, "vipitems.wood", 0);
	}

	public void stoneMenu() {
		List<String> vipItems = new ArrayList<String>();
		Set<String> keys = vipConfig.getConfigurationSection("vipitems.stone").getKeys(false);
		for (String string : keys) {
			vipItems.add(string);
		}
		stoneMenu = new IconMenu("Vip grant stone", (int) Math.ceil(vipItems.size() / 9.0) * 9, new IconMenu.OptionClickEventHandler() {
			@Override
			public void onOptionClick(IconMenu.OptionClickEvent event) {
				handleSubMenu(event);
			}
		}, plugin);

		fillIconMenu(stoneMenu, vipItems, "vipitems.stone",  0);
	}

	public void sandMenu() {
		List<String> vipItems = new ArrayList<String>();
		Set<String> keys = vipConfig.getConfigurationSection("vipitems.sand").getKeys(false);
		for (String string : keys) {
			vipItems.add(string);
		}
		sandMenu = new IconMenu("Vip grant sand menu", (int) Math.ceil(vipItems.size() / 9.0) * 9, new IconMenu.OptionClickEventHandler() {
			@Override
			public void onOptionClick(IconMenu.OptionClickEvent event) {
				handleSubMenu(event);
			}
		}, plugin);

		fillIconMenu(sandMenu, vipItems, "vipitems.sand", 0);
	}

	private void fillIconMenu(IconMenu iconMenu, List<String> vipItems, String configKey, int extraRows) {
		int i = 0;
		for (String item : vipItems) {
			//Create itemStack with data value.
			String[] itemSplit = item.split(":");
			int id = Integer.parseInt(itemSplit[0]);
			MaterialData materialData = new MaterialData(Material.getMaterial(id));
			if (itemSplit.length > 1)
				materialData.setData((byte) Integer.parseInt(itemSplit[1]));
			ItemStack itemStack = materialData.toItemStack(vipConfig.getInt(configKey + "." + item));
			//Change material name to nice readable name.
			String materialName = Material.getMaterial(id).toString();
			String[] materialSplit = materialName.split("_");
			String capitalizedMaterialName = "";
			for (String string : materialSplit) {
				capitalizedMaterialName = capitalizedMaterialName + string.substring(0, 1) + string.substring(1).toLowerCase() + " ";
			}
			capitalizedMaterialName.trim();
			iconMenu.setOption(i + 9 * extraRows, itemStack, ChatColor.RESET + capitalizedMaterialName, (String[]) null);
			i++;
		}
	}

	private void handleSubMenu(IconMenu.OptionClickEvent event) {
		event.setWillClose(false);
		YamlStorage dataStorage = plugin.getDataStorage();
		FileConfiguration dataConfig = dataStorage.getConfig();
		final Player player = event.getPlayer();
		String playerName = player.getName();
		if (player.getInventory().firstEmpty() == -1) {
			player.sendMessage(ChatColor.RED + "Your inventory is full!");
			return;
		}

		if (event.getInIconMenu() == false) {
			if (event.getItemClicked().getType() != Material.WRITTEN_BOOK)
				return;
		}

		int timesUsed = dataConfig.getInt("usedgrant." + playerName);
		timesUsed += 1;
		dataConfig.set("usedgrant." + playerName, timesUsed);
		player.sendMessage(ChatColor.DARK_AQUA + "[VIP] " + ChatColor.WHITE + (3 - timesUsed) + " times left today.");
		player.getInventory().addItem(event.getItemClicked());
		if (timesUsed == 3) {
			event.setWillClose(true);
		}
	}

	private void generateVipItems() {
		YamlStorage vipStorage = plugin.getVipGrantStorage();
		FileConfiguration vipConfig = vipStorage.getConfig();
		vipConfig.set("vipitems.normal.2", 64);
		vipConfig.set("vipitems.normal.3", 64);
		vipConfig.set("vipitems.normal.13", 64);
		vipConfig.set("vipitems.normal.20", 64);
		vipConfig.set("vipitems.normal.35", 64);
		vipConfig.set("vipitems.normal.37", 64);
		vipConfig.set("vipitems.normal.38", 64);
		vipConfig.set("vipitems.normal.65", 64);
		vipConfig.set("vipitems.normal.67", 64);
		vipConfig.set("vipitems.normal.81", 64);
		vipConfig.set("vipitems.normal.86", 64);
		vipConfig.set("vipitems.normal.87", 64);
		vipConfig.set("vipitems.normal.89", 64);
		vipConfig.set("vipitems.normal.91", 64);
		vipConfig.set("vipitems.normal.110", 64);
		vipConfig.set("vipitems.normal.112", 64);
		vipConfig.set("vipitems.normal.337", 64);

		vipConfig.set("vipitems.wood.5:0", 64);
		vipConfig.set("vipitems.wood.5:1", 64);
		vipConfig.set("vipitems.wood.5:2", 64);
		vipConfig.set("vipitems.wood.5:3", 64);
		vipConfig.set("vipitems.wood.17:0", 16);
		vipConfig.set("vipitems.wood.17:1", 16);
		vipConfig.set("vipitems.wood.17:2", 16);
		vipConfig.set("vipitems.wood.17:3", 16);
		vipConfig.set("vipitems.wood.18:0", 64);
		vipConfig.set("vipitems.wood.18:1", 64);
		vipConfig.set("vipitems.wood.18:2", 64);
		vipConfig.set("vipitems.wood.18:3", 64);
		vipConfig.set("vipitems.wood.53", 64);
		vipConfig.set("vipitems.wood.134", 64);
		vipConfig.set("vipitems.wood.135", 64);
		vipConfig.set("vipitems.wood.136", 64);

		vipConfig.set("vipitems.stone.1", 64);
		vipConfig.set("vipitems.stone.4", 64);
		vipConfig.set("vipitems.stone.44:0", 64);
		vipConfig.set("vipitems.stone.44:3", 64);
		vipConfig.set("vipitems.stone.44:5", 64);
		vipConfig.set("vipitems.stone.98:0", 64);
		vipConfig.set("vipitems.stone.98:1", 8);
		vipConfig.set("vipitems.stone.98:2", 8);
		vipConfig.set("vipitems.stone.98:3", 8);
		vipConfig.set("vipitems.stone.67", 64);
		vipConfig.set("vipitems.stone.109", 64);

		vipConfig.set("vipitems.sand.12", 64);
		vipConfig.set("vipitems.sand.24:0", 16);
		vipConfig.set("vipitems.sand.24:1", 16);
		vipConfig.set("vipitems.sand.24:2", 16);
		vipStorage.saveConfig();
	}

}
