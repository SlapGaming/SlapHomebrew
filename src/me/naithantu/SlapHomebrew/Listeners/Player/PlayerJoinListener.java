package me.naithantu.SlapHomebrew.Listeners.Player;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import me.naithantu.SlapHomebrew.Commands.Exception.CommandException;
import me.naithantu.SlapHomebrew.Commands.Lists.ListCommand;
import me.naithantu.SlapHomebrew.Controllers.Book;
import me.naithantu.SlapHomebrew.Controllers.Jails;
import me.naithantu.SlapHomebrew.Controllers.Mail;
import me.naithantu.SlapHomebrew.Controllers.PlayerLogger;
import me.naithantu.SlapHomebrew.Controllers.TabController;
import me.naithantu.SlapHomebrew.Controllers.PlayerLogging.PlotControl;
import me.naithantu.SlapHomebrew.Listeners.AbstractListener;
import me.naithantu.SlapHomebrew.Storage.YamlStorage;
import me.naithantu.SlapHomebrew.Util.Util;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import ru.tehkode.permissions.PermissionUser;
import ru.tehkode.permissions.bukkit.PermissionsEx;

public class PlayerJoinListener extends AbstractListener {
	
	private YamlStorage dataStorage;
	private YamlStorage vipStorage;

	private FileConfiguration dataConfig;
	private FileConfiguration vipConfig;
	
	private Mail mail;
	private Jails jails;
	private PlayerLogger playerLogger;
	private TabController tabController;
	
	private DateFormat checkDay;
	private DateFormat checkMonth;
	private DateFormat checkYear;

	public PlayerJoinListener(YamlStorage dataStorage, YamlStorage vipStorage, Mail mail, Jails jails, PlayerLogger playerLogger, TabController tabController) {
		this.dataStorage = dataStorage;
		this.vipStorage = vipStorage;
		dataConfig = dataStorage.getConfig();
		vipConfig = vipStorage.getConfig();
		this.mail = mail;
		this.jails = jails;
		this.playerLogger = playerLogger;
		this.tabController = tabController;
		
		//Date format
		checkDay = new SimpleDateFormat("dd");
		checkMonth = new SimpleDateFormat("MM");
		checkYear = new SimpleDateFormat("yyyy");
	}

	@EventHandler
	public void onPlayerLogin(PlayerJoinEvent event) {
		final Player player = event.getPlayer();

		//TODO To be removed?
		plugin.getExtras().getGhostTeam().addPlayer(player);

		//Double jump
		if (player.getWorld().getName().equals("world_start"))
			player.setAllowFlight(true);

		//VIP Message
		if (player.hasPermission("slaphomebrew.vip.check") && plugin.getUnfinishedForumVip().size() > 0) {
			plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
				public void run() {
					player.sendMessage(ChatColor.GREEN + "There are " + plugin.getUnfinishedForumVip().size() + " waiting forum promotions/demotions! Type /vip check to see them!");
				}
			}, 10);
		}

		//Vip grant reset
		Date date = new Date();
		int vipDay = Integer.valueOf(checkDay.format(date));
		int vipMonth = Integer.valueOf(checkMonth.format(date));
		int vipYear = Integer.valueOf(checkYear.format(date));
		if (vipDay > dataConfig.getInt("vipdate.day") || vipMonth > dataConfig.getInt("vipdate.month") || vipYear > dataConfig.getInt("vipdate.year")) {
			YamlStorage dataStorage = plugin.getDataStorage();
			dataStorage.getConfig().set("usedgrant", null);
			dataStorage.saveConfig();
			updateVipDays();
		}
		dataConfig.set("vipdate.day", vipDay);
		dataConfig.set("vipdate.month", vipMonth);
		dataConfig.set("vipdate.year", vipYear);
		dataStorage.saveConfig();

		//Check homes.
		if (vipConfig.getConfigurationSection("homes") != null) {
			if (vipConfig.getConfigurationSection("homes").contains(player.getName())) {
				if (!player.hasPermission("essentials.sethome.multiple." + Integer.toString(plugin.getVip().getHomes(player.getName())))) {
					PermissionUser user = PermissionsEx.getUser(player.getName());
					String permission = "essentials.sethome.multiple." + Integer.toString(plugin.getVip().getHomes(player.getName()));
					user.addPermission(permission);
				}
			}
		}

		//Check vip book
		if (vipConfig.getStringList("book") != null) {
			List<String> playerList = vipConfig.getStringList("book");
			if (playerList.contains(player.getName())) {
				System.out.println("Giving book!");
				plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
					public void run() {
						player.getInventory().addItem(Book.getBook(new YamlStorage(plugin, "bookStorage")));
					}
				}, 1);
				playerList.remove(player.getName());
				vipConfig.set("book", playerList);
				vipStorage.saveConfig();
			}
		}	
				
		//Add to Tab
		tabController.playerJoin(player);
		
		//First time join?
		final boolean firstTime = !player.hasPlayedBefore();
		
		Util.runLater(plugin, new Runnable() {
			
			@Override
			public void run() {
				//First time broadcast
				if (firstTime) {
					plugin.getServer().broadcastMessage(Util.getHeader() + "Welcome " + ChatColor.GREEN + player.getName() + ChatColor.WHITE + " to the SlapGaming Minecraft Server. If you need help please contact a " + ChatColor.GOLD + "Guide" + ChatColor.WHITE + ", " +
							ChatColor.AQUA + "Mod" + ChatColor.WHITE + " or " + ChatColor.RED + "Admin" + ChatColor.WHITE +
							" by typing " + ChatColor.RED + "/modreq [message]" + ChatColor.WHITE + "!");
				}
				
				//Abort if the player went offline already
				if (!player.isOnline()) return;
				
				if (firstTime) {
					//Starter kit
					PlayerInventory pi = player.getInventory();
					pi.setItem(0, new ItemStack(Material.STONE_SWORD));
					pi.setItem(1, new ItemStack(Material.STONE_PICKAXE));
					pi.setItem(2, new ItemStack(Material.STONE_AXE));
					pi.setItem(3, new ItemStack(Material.STONE_SPADE));
					pi.setItem(7, new ItemStack(Material.FEATHER));
					pi.setItem(8, new ItemStack(Material.COOKIE, 5));
						//TODO Add a new Starter Book
				}
				
				//Minechat prevention
				playerLogger.joinedMinechatChecker(player);
				
				try { //Execute /list
					new ListCommand(player, new String[]{}).handle();
				} catch (CommandException e) {
					Util.badMsg(player, e.getMessage());
				}
				
				//Throw in jail
				if (jails.isInJail(player.getName())) {
					jails.switchToOnlineJail(player);
				}
				
				//Check mails
				mail.hasNewMail(player);
				
				//If admin send pending plot checks
				if (Util.testPermission(player, "plot.admin")) {
					PlotControl.sendUnfinishedPlotMarks(player);
				}
				
			}
		}, 10);
		
	}

	private void updateVipDays() {
		HashMap<String, Integer> tempMap = new HashMap<String, Integer>();
		if (vipConfig.getConfigurationSection("vipdays") == null)
			return;
		for (String key : vipConfig.getConfigurationSection("vipdays").getKeys(false)) {
			tempMap.put(key, vipConfig.getInt("vipdays." + key));
		}
		vipConfig.set("vipdays", null);
		//Create configurationsection if it isn't there yet:
		if (vipConfig.getConfigurationSection("vipdays") == null) {
			vipConfig.createSection("vipdays");
		}
		for (Map.Entry<String, Integer> entry : tempMap.entrySet()) {
			PermissionUser user = PermissionsEx.getUser(entry.getKey());
			if (entry.getValue() != -1 && !user.has("slaphomebrew.vip.freeze")) {
				entry.setValue(entry.getValue() - 1);
			}
			if (entry.getValue() == 0) {
				plugin.getVip().demoteVip(entry.getKey());
			} else {
				vipConfig.getConfigurationSection("vipdays").set(entry.getKey(), entry.getValue());
			}
		}
		vipStorage.saveConfig();
	}
	
}
