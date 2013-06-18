package me.naithantu.SlapHomebrew;

import java.util.ArrayList;
import java.util.List;

import me.naithantu.SlapHomebrew.Storage.YamlStorage;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

public class Menus {
	SlapHomebrew plugin;
	IconMenu vipMenu;

	IconMenu woodMenu;
	IconMenu stoneMenu;
	IconMenu netherMenu;
	IconMenu miscellaneousMenu;
	IconMenu bookMenu;

	YamlStorage vipStorage;
	FileConfiguration vipConfig;

	public Menus(SlapHomebrew plugin) {
		this.plugin = plugin;
		vipStorage = plugin.getVipGrantStorage();
		vipConfig = vipStorage.getConfig();
		vipMenu();
		woodMenu();
		stoneMenu();
		netherMenu();
		miscellaneousMenu();
		bookMenu();
	}

	public IconMenu getVipMenu() {
		return vipMenu;
	}

	public void vipMenu() {
		vipMenu = new IconMenu("Vip grant menu", 9, new IconMenu.OptionClickEventHandler() {
			@Override
			public void onOptionClick(IconMenu.OptionClickEvent event) {
				handleMenu(event);
			}
		}, plugin);

		addMenuBar(vipMenu);
	}

	public void woodMenu() {
		int size = vipConfig.getConfigurationSection("vipitems.wood").getKeys(false).size();
		woodMenu = new IconMenu("Vip grant wood menu", (int) Math.ceil(size / 9.0 + 1) * 9, new IconMenu.OptionClickEventHandler() {
			@Override
			public void onOptionClick(IconMenu.OptionClickEvent event) {
				handleMenu(event);
			}
		}, plugin);

		fillIconMenu(woodMenu, "vipitems.wood", 1);
	}

	public void stoneMenu() {
		int size = vipConfig.getConfigurationSection("vipitems.stone").getKeys(false).size();
		stoneMenu = new IconMenu("Vip grant stone menu", (int) Math.ceil(size / 9.0 + 1) * 9, new IconMenu.OptionClickEventHandler() {
			@Override
			public void onOptionClick(IconMenu.OptionClickEvent event) {
				handleMenu(event);
			}
		}, plugin);

		fillIconMenu(stoneMenu, "vipitems.stone", 1);
	}

	public void netherMenu() {
		int size = vipConfig.getConfigurationSection("vipitems.nether").getKeys(false).size();
		netherMenu = new IconMenu("Vip grant nether menu", (int) Math.ceil(size / 9.0 + 1) * 9, new IconMenu.OptionClickEventHandler() {
			@Override
			public void onOptionClick(IconMenu.OptionClickEvent event) {
				handleMenu(event);
			}
		}, plugin);

		fillIconMenu(netherMenu, "vipitems.nether", 1);
	}

	public void miscellaneousMenu() {
		int size = vipConfig.getConfigurationSection("vipitems.miscellaneous").getKeys(false).size();
		miscellaneousMenu = new IconMenu("Vip grant miscellaneous menu", (int) Math.ceil(size / 9.0 + 1) * 9, new IconMenu.OptionClickEventHandler() {
			@Override
			public void onOptionClick(IconMenu.OptionClickEvent event) {
				handleMenu(event);
			}
		}, plugin);

		fillIconMenu(miscellaneousMenu, "vipitems.miscellaneous", 1);
	}

	public void bookMenu() {
		bookMenu = new IconMenu("Vip grant book menu", (int) 4 * 9, new IconMenu.OptionClickEventHandler() {
			@Override
			public void onOptionClick(IconMenu.OptionClickEvent event) {
				handleMenu(event);
			}
		}, plugin);
	}

	public void openBookMenu(final Player player) {
		//Get all books out of inventory.
		List<ItemStack> books = new ArrayList<ItemStack>();
		for (ItemStack item : player.getInventory()) {
			if (item != null && item.getType() == Material.WRITTEN_BOOK) {
				books.add(item.clone());
			}
		}

		bookMenu.emptyMenu();
		addMenuBar(bookMenu);

		int i = 0;
		for (ItemStack itemStack : books) {
			BookMeta bookMeta = (BookMeta) itemStack.getItemMeta();
			String title = bookMeta.getTitle();
			bookMenu.setOption(i + 9, itemStack, ChatColor.RESET + title, (String[]) null);
			i++;
		}
		bookMenu.open(player);
	}

	private void fillIconMenu(IconMenu iconMenu, String configKey, int extraRows) {
		addMenuBar(iconMenu);

		int i = 0;
		for (String item : vipConfig.getConfigurationSection(configKey).getKeys(false)) {
			//Create itemStack with data value.
			String[] itemSplit = item.split(":");
			int id = Integer.parseInt(itemSplit[0]);
			ItemStack itemStack;
			if (itemSplit.length > 1)
				itemStack = new ItemStack(Material.getMaterial(id), vipConfig.getInt(configKey + "." + item), (short) Integer.parseInt(itemSplit[1]));
			else
				itemStack = new ItemStack(Material.getMaterial(id), vipConfig.getInt(configKey + "." + item));
			
			//Change material name to nice readable name.
			String materialName = Material.getMaterial(id).toString();
			String[] materialSplit = materialName.split("_");
			String capitalizedMaterialName = "";
			for (String string : materialSplit) {
				capitalizedMaterialName = capitalizedMaterialName + string.substring(0, 1) + string.substring(1).toLowerCase() + " ";
			}
			capitalizedMaterialName.trim();
			if (itemStack.getTypeId() != 0)
				iconMenu.setOption(i + 9 * extraRows, itemStack, ChatColor.RESET + capitalizedMaterialName, (String[]) null);
			i++;
		}
	}

	private void addMenuBar(IconMenu iconMenu) {
		iconMenu.setOption(0, new ItemStack(Material.NETHER_BRICK, 0), "Nether menu", "Click for more nether items.");
		iconMenu.setOption(2, new ItemStack(Material.LOG, 0), "Wood menu.", "Click for more wood and leaves items.");
		iconMenu.setOption(4, new ItemStack(Material.SMOOTH_BRICK, 0), "Stone & sand menu", "Click for more stone & sandstone items.");
		iconMenu.setOption(6, new ItemStack(Material.GLASS), "Miscellaneous menu", "Click for all other items.");
		iconMenu.setOption(8, new ItemStack(Material.WRITTEN_BOOK, 0), "Book menu", "Click to copy books.");
	}

	private void handleMenu(IconMenu.OptionClickEvent event) {
		final Player player = event.getPlayer();
		String playerName = player.getName();
		if (event.getPosition() == 0) {
			player.closeInventory();
			Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
				public void run() {
					netherMenu.open(player);
				}
			}, 2);
			return;
		}
		if (event.getPosition() == 2) {
			player.closeInventory();
			Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
				public void run() {
					woodMenu.open(player);
				}
			}, 2);
			return;
		}
		if (event.getPosition() == 4) {
			player.closeInventory();
			Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
				public void run() {
					stoneMenu.open(player);
				}
			}, 2);
			return;
		}
		if (event.getPosition() == 6) {
			player.closeInventory();
			Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
				public void run() {
					miscellaneousMenu.open(player);
				}
			}, 2);
			return;
		}

		if (event.getPosition() == 8) {
			Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
				public void run() {
					openBookMenu(player);
				}
			}, 2);
			return;
		}
		event.setWillClose(false);
		YamlStorage dataStorage = plugin.getDataStorage();
		FileConfiguration dataConfig = dataStorage.getConfig();

		if (player.getInventory().firstEmpty() == -1) {
			player.sendMessage(ChatColor.RED + "Your inventory is full!");
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
}
