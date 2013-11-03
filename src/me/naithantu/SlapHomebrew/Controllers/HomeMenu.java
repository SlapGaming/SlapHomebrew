package me.naithantu.SlapHomebrew.Controllers;

import java.util.List;

import me.naithantu.SlapHomebrew.SlapHomebrew;
import me.naithantu.SlapHomebrew.Controllers.IconMenu.OptionClickEvent;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import com.earth2me.essentials.User;

public class HomeMenu {

	private SlapHomebrew plugin;
	private User essentialsUser;
	private String playerName;
	private IconMenu mainMenu;
	private IconMenu homeMenu;
	private List<String> homes;

	int oldSurvivalHomes;
	int newSurvivalHomes;
	int creativeHomes;
	int netherHomes;
	int resourceHomes;

	public HomeMenu(User player, SlapHomebrew plugin) {
		this.plugin = plugin;
		essentialsUser = player;
		playerName = player.getName();
		createHomeMainMenu();
	}

	/* ---Creators--- */

	private void createHomeMainMenu() {
		//Destroy if exists
		if (mainMenu instanceof IconMenu) {
			mainMenu.destroy();
			mainMenu = null;
		}
		if (homeMenu instanceof IconMenu) {
			homeMenu.destroy();
			homeMenu = null;
		}

		homes = essentialsUser.getHomes();
		oldSurvivalHomes = newSurvivalHomes = creativeHomes = netherHomes = resourceHomes = 0;

		for (String home : homes) {
			try {
				String worldName = essentialsUser.getHome(home).getWorld().getName();
				switch (worldName.toLowerCase()) {
				case "world":
					oldSurvivalHomes++;
					break;
				case "world_survival2":
					newSurvivalHomes++;
					break;
				case "world_creative":
					creativeHomes++;
					break;
				case "world_nether":
					netherHomes++;
					break;
				case "world_resource10":
					resourceHomes++;
					break;
				}
			} catch (Exception e) {
			}
		}

		mainMenu = new IconMenu("Home Main Menu", 9, new IconMenu.OptionClickEventHandler() {

			@Override
			public void onOptionClick(OptionClickEvent event) {
				handleMainMenuClick(event, event.getCommand().split("-"));
			}

		}, plugin, playerName);

		mainMenu.setOption("world-" + oldSurvivalHomes, 0, new ItemStack(Material.DIRT, 0), "Old Survival World", oldSurvivalHomes + " home(s)");
		mainMenu.setOption("world_survival2-" + newSurvivalHomes, 2, new ItemStack(Material.GRASS, 0), "New Survival World", newSurvivalHomes + " home(s)");
		mainMenu.setOption("world_creative-" + creativeHomes, 4, new ItemStack(Material.DIAMOND_BLOCK, 0), "Creative World", creativeHomes + " home(s)");
		mainMenu.setOption("world_nether-" + netherHomes, 6, new ItemStack(Material.NETHER_BRICK, 0), "The Nether", netherHomes + " home(s)");
		mainMenu.setOption("world_resource10-" + resourceHomes, 8, new ItemStack(Material.COBBLESTONE, 0), "Resource World", resourceHomes + " home(s)");

		mainMenu.open(essentialsUser.getPlayer());

	}

	public void reCreateHomeMainMenu(User player) {
		essentialsUser = plugin.getEssentials().getUserMap().getUser(player.getName());
		playerName = player.getName();
		createHomeMainMenu();
	}

	private void showHomeWorldMenu(String World, int page) {
		//Destroy if exists
		if (homeMenu instanceof IconMenu) {
			homeMenu.destroy();
		}

		final String world = World;

		String menuName;
		Material menuMaterial;
		int nrOfHomes;

		switch (world) {
		case "world":
			menuName = "Old Survival World Menu";
			menuMaterial = Material.DIRT;
			nrOfHomes = oldSurvivalHomes;
			break;
		case "world_survival2":
			menuName = "New Survival World Menu";
			menuMaterial = Material.GRASS;
			nrOfHomes = newSurvivalHomes;
			break;
		case "world_creative":
			menuName = "Creative World Menu";
			menuMaterial = Material.DIAMOND_BLOCK;
			nrOfHomes = creativeHomes;
			break;
		case "world_nether":
			menuName = "The Nether Menu";
			menuMaterial = Material.NETHER_BRICK;
			nrOfHomes = netherHomes;
			break;
		case "world_resource10":
			menuName = "Resource World Menu";
			menuMaterial = Material.COBBLESTONE;
			nrOfHomes = resourceHomes;
			break;
		default:
			menuName = "Unkown world";
			menuMaterial = Material.CAKE;
			nrOfHomes = 0;
			for (String home : homes) {
				try {
					if (essentialsUser.getHome(home).getWorld().getName().equals(world)) {
						nrOfHomes++;
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}

		//Determine iconMenu size
		int inventorySize;
		int lastHome = 0;
		int firstHome = 0;
		boolean nextPage = false;
		if (page == 1) {
			inventorySize = (((int) Math.ceil(nrOfHomes / 9.0) + 1) * 9);
			if (inventorySize > 54) {
				inventorySize = 54;
				nextPage = true;
			}
		} else {
			lastHome = (page * 45) + 1;
			if (nrOfHomes < (lastHome + 1)) {
				firstHome = (page - 1) * 45;
				inventorySize = ((int) Math.ceil((double) (nrOfHomes - firstHome) / 9) + 1) * 9;
			} else {
				inventorySize = 54;
				nextPage = true;
			}
		}

		//Create IconMenu
		homeMenu = new IconMenu(menuName, inventorySize, new IconMenu.OptionClickEventHandler() {

			@Override
			public void onOptionClick(OptionClickEvent event) {
				handleHomeMenuClick(event, event.getCommand(), world);
			}

		}, plugin, playerName);

		//Set menuBar
		homeMenu.setOption("back_to_home_menu", inventorySize - 5, new ItemStack(Material.BOOK, 0), "Back to the main menu");
		if (page > 1) {
			homeMenu.setOption("to_page-" + (page - 1), (inventorySize - 9), new ItemStack(Material.GLOWSTONE_DUST, 0), "To the previous page");
		}
		if (nextPage) {
			homeMenu.setOption("to_page-" + (page + 1), (inventorySize - 1), new ItemStack(Material.REDSTONE), "To the next page");
		}

		//Fill with homes
		int xCount = 0;
		if (page == 1) {
			for (String home : homes) {
				String worldName = getWorldName(home);
				if (world.equals(worldName)) {
					if (xCount < 45) {
						homeMenu.setOption(home, xCount, new ItemStack(menuMaterial, 0), home, "Teleport to " + home);
						xCount++;
					}
				}
			}
		} else {
			int currentHome = 0;
			for (String home : homes) {
				String worldName = getWorldName(home);
				if (world.equals(worldName)) {
					currentHome++;
					if (firstHome < currentHome && currentHome < (lastHome + 1)) {
						homeMenu.setOption(home, xCount, new ItemStack(menuMaterial, 0), home, "Teleport to " + home);
						xCount++;
					}
				}
			}
		}

		homeMenu.open(essentialsUser.getPlayer());

	}

	/* ---Show--- */
	private void showMainMenu() {
		plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {

			@Override
			public void run() {
				mainMenu.open(essentialsUser.getPlayer());
			}

		}, 2);
	}

	private void showNextHomeMenu(String World, int Page) {
		final String world = World;
		final int page = Page;
		plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				showHomeWorldMenu(world, page);
			}

		}, 2);
	}

	/* ---Handlers--- */
	private void handleMainMenuClick(OptionClickEvent event, String[] worldInfo) {
		int numberOfHomes = Integer.parseInt(worldInfo[1]);
		if (numberOfHomes == 0) {
			//No homes in that world
			essentialsUser.sendMessage(ChatColor.RED + "You don't have any homes in this world.");
			event.setWillClose(false);
		} else {
			showNextHomeMenu(worldInfo[0], 1);
		}

	}

	private void handleHomeMenuClick(OptionClickEvent event, String EventName, String world) {
		final String eventName = EventName;
		if (eventName.equals("back_to_home_menu")) {
			showMainMenu();
		} else if (eventName.contains("to_page-")) {
			int toPage = Integer.parseInt(eventName.split("-")[1]);
			showNextHomeMenu(world, toPage);
		} else {
			teleportPlayer(eventName);
		}
		event.setWillDestroy(true);
	}

	/* ---Others--- */
	private void teleportPlayer(String home) {
		try {
			essentialsUser.teleport(essentialsUser.getHome(home));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private String getWorldName(String home) {
		try {
			return essentialsUser.getHome(home).getWorld().getName();
		} catch (Exception e) {
			return null;
		}
	}

}
