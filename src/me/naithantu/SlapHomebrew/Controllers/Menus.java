package me.naithantu.SlapHomebrew.Controllers;

import me.naithantu.SlapHomebrew.Commands.Exception.ErrorMsg;
import me.naithantu.SlapHomebrew.Commands.Exception.NotVIPException;
import me.naithantu.SlapHomebrew.Storage.YamlStorage;
import me.naithantu.SlapHomebrew.Util.Util;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Menus extends AbstractController {
	
	private IconMenu vipMenu;

	private IconMenu woodMenu;
	private IconMenu stoneMenu;
	private IconMenu netherMenu;
	private IconMenu miscellaneousMenu;

	private IconMenu creativeMenu;

	private YamlStorage vipStorage;
	private FileConfiguration vipConfig;

	private HashMap<String, IconMenu> bookMenus = new HashMap<String, IconMenu>();

	public Menus() {
		vipStorage = plugin.getVipGrantStorage();
		vipConfig = vipStorage.getConfig();
		vipMenu();
		woodMenu();
		stoneMenu();
		netherMenu();
		miscellaneousMenu();

		creativeMenu();
	}

	public IconMenu getCreativeMenu() {
		return creativeMenu;
	}

	public IconMenu getVipMenu() {
		return vipMenu;
	}

	public void vipMenu() {
		vipMenu = new IconMenu("Vip grant menu", 9, new IconMenu.OptionClickEventHandler() {
			@Override
			public void onOptionClick(IconMenu.OptionClickEvent event) {
				handleVipMenu(event);
			}
		});

		addMenuBar(vipMenu);
	}

	public void woodMenu() {
		int size = vipConfig.getConfigurationSection("vipitems.wood").getKeys(false).size();
		woodMenu = new IconMenu("Vip grant wood menu", (int) Math.ceil(size / 9.0 + 1) * 9, new IconMenu.OptionClickEventHandler() {
			@Override
			public void onOptionClick(IconMenu.OptionClickEvent event) {
				handleVipMenu(event);
			}
		});

		fillIconMenu(woodMenu, "vipitems.wood", true);
	}

	public void stoneMenu() {
		int size = vipConfig.getConfigurationSection("vipitems.stone").getKeys(false).size();
		stoneMenu = new IconMenu("Vip grant stone & sand menu", (int) Math.ceil(size / 9.0 + 1) * 9, new IconMenu.OptionClickEventHandler() {
			@Override
			public void onOptionClick(IconMenu.OptionClickEvent event) {
				handleVipMenu(event);
			}
		});

		fillIconMenu(stoneMenu, "vipitems.stone", true);
	}

	public void netherMenu() {
		int size = vipConfig.getConfigurationSection("vipitems.nether").getKeys(false).size();
		netherMenu = new IconMenu("Vip grant nether menu", (int) Math.ceil(size / 9.0 + 1) * 9, new IconMenu.OptionClickEventHandler() {
			@Override
			public void onOptionClick(IconMenu.OptionClickEvent event) {
				handleVipMenu(event);
			}
		});

		fillIconMenu(netherMenu, "vipitems.nether", true);
	}

	public void miscellaneousMenu() {
		int size = vipConfig.getConfigurationSection("vipitems.miscellaneous").getKeys(false).size();
		miscellaneousMenu = new IconMenu("Vip grant miscellaneous menu", (int) Math.ceil(size / 9.0 + 1) * 9, new IconMenu.OptionClickEventHandler() {
			@Override
			public void onOptionClick(IconMenu.OptionClickEvent event) {
				handleVipMenu(event);
			}
		});

		fillIconMenu(miscellaneousMenu, "vipitems.miscellaneous", true);
	}
	
	public void creativeMenu() {
		if (vipConfig.getConfigurationSection("creative.extraitems") == null) {
			vipConfig.set("creative.extraitems.1", 1);
			vipStorage.saveConfig();
		}
		int size = vipConfig.getConfigurationSection("creative.extraitems").getKeys(false).size();
		creativeMenu = new IconMenu("Extra creative items menu", (int) Math.ceil(size / 9.0) * 9, new IconMenu.OptionClickEventHandler() {
			@Override
			public void onOptionClick(IconMenu.OptionClickEvent event) {
				Player player = event.getPlayer();

				if (player.getInventory().firstEmpty() == -1) {
					player.sendMessage(ChatColor.RED + "Your inventory is full!");
					return;
				}

				player.getInventory().addItem(event.getItemClicked());
				event.setWillClose(false);
			}
		});

		fillIconMenu(creativeMenu, "creative.extraitems", false);
	}

	public void openBookMenu(final Player player) {
		if (bookMenus.containsKey(player.getName())){
			bookMenus.get(player.getName()).destroy();
			bookMenus.remove(player.getName());
		}

		IconMenu bookMenu;
		//Get all books out of inventory.
		List<ItemStack> books = new ArrayList<ItemStack>();
		for (ItemStack item : player.getInventory()) {
			if (item != null && item.getType() == Material.WRITTEN_BOOK) {
				books.add(item.clone());
			}
		}

		int iconMenuSize = books.size();

		if (books.isEmpty())
			iconMenuSize++;

		bookMenu = new IconMenu("Vip grant book menu", (int) Math.ceil(iconMenuSize / 9.0 + 1) * 9, new IconMenu.OptionClickEventHandler() {
			@Override
			public void onOptionClick(IconMenu.OptionClickEvent event) {
				event.setWillClose(false);
				if (event.getPosition() > 8 && event.getItemClicked().getType() != Material.WRITTEN_BOOK)
					return;

				handleVipMenu(event);
			}
		});

		addMenuBar(bookMenu);

		if (books.isEmpty()) {
			bookMenu.setOption(13, new ItemStack(Material.SIGN_POST), "There are no books to copy in your inventory", "Add a written book to your inventory, then you can copy it here.");
		} else {
			int i = 0;
			for (ItemStack itemStack : books) {
				bookMenu.setOption(i + 9, itemStack, itemStack.getItemMeta().getDisplayName(), (String[]) null);
				i++;
			}
		}

		bookMenu.open(player);
		bookMenus.put(player.getName(), bookMenu);
	}

	@SuppressWarnings("deprecation")
	private void fillIconMenu(IconMenu iconMenu, String configKey, boolean addMenuBar) {
		int extraRow = 0;
		if (addMenuBar) {
			addMenuBar(iconMenu);
			extraRow = 9;
		}

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

			if (itemStack.getTypeId() != 0)
				iconMenu.setOption(i + extraRow, itemStack, itemStack.getItemMeta().getDisplayName(), (String[]) null);

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

	private void handleVipMenu(IconMenu.OptionClickEvent event) {
		final Player player = event.getPlayer();
		String UUID = player.getUniqueId().toString();
		try {
			//Get uses left
			Vip vip = plugin.getVip(); //Get VIP
			int usesLeft = vip.getVipGrantUsesLeft(UUID);
			if (usesLeft <= 0) {
				Util.badMsg(player, ErrorMsg.alreadyUsedVipGrant.toString());
				return;
			}
			
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
	
			if (player.getInventory().firstEmpty() == -1) {
				player.sendMessage(ChatColor.RED + "Your inventory is full!");
				return;
			}
	
			//use the grant
			usesLeft = vip.useVipGrant(UUID);
			player.getInventory().addItem(event.getItemClicked());
			
			//Message		
			if (usesLeft <= 0) {
				event.setWillClose(true);
				player.sendMessage(ChatColor.DARK_AQUA + "[VIP] " + ChatColor.WHITE + "You have used all your VIP grants for today!");
			} else {
				player.sendMessage(ChatColor.DARK_AQUA + "[VIP] " + ChatColor.WHITE + "You have " + usesLeft + (usesLeft == 1 ? " use" : " uses") + " left today.");
			}
		} catch (NotVIPException e) { //If not VIP
			Util.badMsg(player, e.getMessage());
		}
	}
	
    @Override
    public void shutdown() {
    	//Not needed
    }
}
