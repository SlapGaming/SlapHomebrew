package me.naithantu.SlapHomebrew;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.EventHandler;
import ru.tehkode.permissions.PermissionUser;
import ru.tehkode.permissions.bukkit.PermissionsEx;

public class PlayerListener implements Listener {

	SlapHomebrew plugin;

	PlayerListener(SlapHomebrew instance) {
		plugin = instance;
	}

	String serverMessage = "";
	Player player;

	public void ChatColors() {
		serverMessage = serverMessage.replaceAll("&a", ChatColor.GREEN + "");
		serverMessage = serverMessage.replaceAll("&b", ChatColor.AQUA + "");
		serverMessage = serverMessage.replaceAll("&c", ChatColor.RED + "");
		serverMessage = serverMessage.replaceAll("&d", ChatColor.LIGHT_PURPLE + "");
		serverMessage = serverMessage.replaceAll("&e", ChatColor.YELLOW + "");
		serverMessage = serverMessage.replaceAll("&f", ChatColor.WHITE + "");
		serverMessage = serverMessage.replaceAll("&0", ChatColor.BLACK + "");
		serverMessage = serverMessage.replaceAll("&1", ChatColor.DARK_BLUE + "");
		serverMessage = serverMessage.replaceAll("&2", ChatColor.DARK_GREEN + "");
		serverMessage = serverMessage.replaceAll("&3", ChatColor.DARK_AQUA + "");
		serverMessage = serverMessage.replaceAll("&4", ChatColor.DARK_RED + "");
		serverMessage = serverMessage.replaceAll("&5", ChatColor.DARK_PURPLE + "");
		serverMessage = serverMessage.replaceAll("&6", ChatColor.GOLD + "");
		serverMessage = serverMessage.replaceAll("&7", ChatColor.GRAY + "");
		serverMessage = serverMessage.replaceAll("&8", ChatColor.DARK_GRAY + "");
		serverMessage = serverMessage.replaceAll("&9", ChatColor.BLUE + "");
	}

	private void updateVipDays() {
		HashMap<String, Integer> tempMap = new HashMap<String, Integer>();
		if (plugin.getVipConfig().getConfigurationSection("vipdays") == null)
			return;
		for (String key : plugin.getVipConfig().getConfigurationSection("vipdays").getKeys(false)) {
			tempMap.put(key, plugin.getVipConfig().getInt("vipdays." + key));
		}
		plugin.getVipConfig().set("vipdays", null);
		//Create configurationsection if it isn't there yet:
		if (plugin.getVipConfig().getConfigurationSection("vipdays") == null) {
			plugin.getVipConfig().createSection("vipdays");
		}
		for (Map.Entry<String, Integer> entry : tempMap.entrySet()) {
			PermissionUser user = PermissionsEx.getUser(entry.getKey());
			if (entry.getValue() != -1 && !user.has("slaphomebrew.vip.freeze")) {
				entry.setValue(entry.getValue() - 1);
			}
			if (entry.getValue() == 0) {
				plugin.demoteVip(entry.getKey());
			} else {
				plugin.getVipConfig().getConfigurationSection("vipdays").set(entry.getKey(), entry.getValue());
			}
		}
		plugin.saveVipConfig();
	}

	@EventHandler
	public void onPlayerLogin(PlayerLoginEvent event) {
		final Player player = event.getPlayer();
		//Plot message
		if (player.hasPermission("slaphomebrew.plot.admin") && plugin.getUnfinishedPlots().size() > 0) {
			plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
				public void run() {
					player.sendMessage(ChatColor.GREEN + "There are " + plugin.getUnfinishedPlots().size() + " unfinished plot requests! Type /plot check to see them!");
				}
			}, 10);
		}
		if (player.hasPermission("slaphomebrew.vip.check") && plugin.getUnfinishedForumVip().size() > 0) {
			plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
				public void run() {
					player.sendMessage(ChatColor.GREEN + "There are " + plugin.getUnfinishedForumVip().size() + " waiting forum promotions/demotions! Type /vip check to see them!");
				}
			}, 10);
		}

		//Vip grant reset
		DateFormat checkDay = new SimpleDateFormat("dd");
		DateFormat checkMonth = new SimpleDateFormat("MM");
		DateFormat checkYear = new SimpleDateFormat("yyyy");
		Date date = new Date();
		int vipDay = Integer.valueOf(checkDay.format(date));
		int vipMonth = Integer.valueOf(checkMonth.format(date));
		int vipYear = Integer.valueOf(checkYear.format(date));
		if (vipDay > plugin.getDataConfig().getInt("vipdate.day") || vipMonth > plugin.getDataConfig().getInt("vipdate.month") || vipYear > plugin.getDataConfig().getInt("vipdate.year")) {
			SlapHomebrew.usedGrant.clear();
			updateVipDays();
		}
		plugin.getDataConfig().set("vipdate.day", vipDay);
		plugin.getDataConfig().set("vipdate.month", vipMonth);
		plugin.getDataConfig().set("vipdate.year", vipYear);
		plugin.saveDataConfig();

		//Check homes.
		if (plugin.getVipConfig().getConfigurationSection("homes") != null) {
			if (plugin.getVipConfig().getConfigurationSection("homes").contains(player.getName())) {
				if (!player.hasPermission("essentials.sethome.multiple." + Integer.toString(plugin.getHomes(player.getName())))) {
					PermissionUser user = PermissionsEx.getUser(player.getName());
					String permission = "essentials.sethome.multiple." + Integer.toString(plugin.getHomes(player.getName()));
					user.addPermission(permission);
				}
			}
		}
	}

	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent event) {
		if (event.getEntity() instanceof Player) {
			player = event.getEntity();
			if (player.hasPermission("slaphomebrew.backdeath")) {
				if (!player.getWorld().getName().equalsIgnoreCase("world_pvp") && !player.getWorld().getName().equalsIgnoreCase("world_the_end")) {
					SlapHomebrew.backDeath.put(player.getName(), player.getLocation());
					player.sendMessage(ChatColor.GRAY + "Use the /backdeath command to return to your death point.");
				}
			}
			System.out.println(player.getName() + " died at (" + player.getLocation().getBlockX() + ", " + player.getLocation().getBlockY() + ", " + player.getLocation().getBlockZ() + ").");
		}
	}

	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event) {
		player = event.getPlayer();
		if (SlapCommands.retroBow.contains(player.getName())) {
			player.launchProjectile(Arrow.class);
		}
	}

	@EventHandler
	public void onPlayerTeleport(PlayerTeleportEvent event) {
		Player player = event.getPlayer();
		if (player.isInsideVehicle()) {
			if (player.getVehicle() instanceof Minecart) {
				if (SlapHomebrew.mCarts.contains(player.getVehicle().getUniqueId())) {
					event.getPlayer().leaveVehicle();

				}
			}
		}
	}

	@EventHandler
	public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
		player = event.getPlayer();
		String message = event.getMessage().toLowerCase().trim();
		String[] commandMessage = message.split(" ");
		if (commandMessage.length < 3)
			return;
		if (commandMessage[0].equals("/rg") || commandMessage[0].equals("/region")) {
			DateFormat cmdDate = new SimpleDateFormat("MM-dd HH:mm:ss");
			Date date = new Date();
			if (commandMessage[1].equals("define")) {
				if (!SlapHomebrew.worldGuard.containsKey(commandMessage[2])) {
					if (commandMessage[0].equals("/rg"))
						SlapHomebrew.worldGuard.put(commandMessage[2], cmdDate.format(date) + " " + player.getName() + " made region " + message.replace("/rg define ", ""));
					else
						SlapHomebrew.worldGuard.put(commandMessage[2], cmdDate.format(date) + " " + player.getName() + " made region " + message.replace("/region define ", ""));
				} else {
					if (commandMessage[0].equals("/rg"))
						SlapHomebrew.worldGuard.put(commandMessage[2], SlapHomebrew.worldGuard.get(commandMessage[2]) + "<==>" + cmdDate.format(date) + " " + player.getName() + " made region "
								+ message.replace("/rg define ", ""));
					else
						SlapHomebrew.worldGuard.put(commandMessage[2], SlapHomebrew.worldGuard.get(commandMessage[2]) + "<==>" + cmdDate.format(date) + " " + player.getName() + " made region "
								+ message.replace("/region define ", ""));
				}
			} else if (commandMessage[1].equals("remove")) {
				if (SlapHomebrew.worldGuard.containsKey(commandMessage[2])) {
					if (commandMessage[0].equals("/rg"))
						SlapHomebrew.worldGuard.put(commandMessage[2], SlapHomebrew.worldGuard.get(commandMessage[2]) + "<==>" + cmdDate.format(date) + " " + player.getName() + " removed region "
								+ message.replace("/rg remove ", ""));
					else
						SlapHomebrew.worldGuard.put(commandMessage[2], SlapHomebrew.worldGuard.get(commandMessage[2]) + "<==>" + cmdDate.format(date) + " " + player.getName() + " removed region "
								+ message.replace("/region remove ", ""));
				}
			} else if (commandMessage[1].equals("addowner")) {
				if (commandMessage[0].equals("/rg"))
					SlapHomebrew.worldGuard.put(commandMessage[2], SlapHomebrew.worldGuard.get(commandMessage[2]) + "<==>" + cmdDate.format(date) + " " + player.getName() + " added owner(s)"
							+ message.replace("/rg addowner " + commandMessage[2], ""));
				else
					SlapHomebrew.worldGuard.put(commandMessage[2], SlapHomebrew.worldGuard.get(commandMessage[2]) + "<==>" + cmdDate.format(date) + " " + player.getName() + " added owner(s)"
							+ message.replace("/region addowner " + commandMessage[2], ""));
			} else if (commandMessage[1].equals("removeowner")) {
				if (commandMessage[0].equals("/rg"))
					SlapHomebrew.worldGuard.put(commandMessage[2], SlapHomebrew.worldGuard.get(commandMessage[2]) + "<==>" + cmdDate.format(date) + " " + player.getName() + " removed owner(s)"
							+ message.replace("/rg removeowner " + commandMessage[2], ""));
				else
					SlapHomebrew.worldGuard.put(commandMessage[2], SlapHomebrew.worldGuard.get(commandMessage[2]) + "<==>" + cmdDate.format(date) + " " + player.getName() + " removed owner(s)"
							+ message.replace("/region removeowner " + commandMessage[2], ""));

			} else if (commandMessage[1].equals("addmember")) {
				if (commandMessage[0].equals("/rg"))
					SlapHomebrew.worldGuard.put(commandMessage[2], SlapHomebrew.worldGuard.get(commandMessage[2]) + "<==>" + cmdDate.format(date) + " " + player.getName() + " added member(s)"
							+ message.replace("/rg addmember " + commandMessage[2], ""));
				else
					SlapHomebrew.worldGuard.put(commandMessage[2], SlapHomebrew.worldGuard.get(commandMessage[2]) + "<==>" + cmdDate.format(date) + " " + player.getName() + " added member(s)"
							+ message.replace("/region addmember " + commandMessage[2], ""));

			} else if (commandMessage[1].equals("removemember")) {
				if (commandMessage[0].equals("/rg"))
					SlapHomebrew.worldGuard.put(commandMessage[2], SlapHomebrew.worldGuard.get(commandMessage[2]) + "<==>" + cmdDate.format(date) + " " + player.getName() + " removed member(s)"
							+ message.replace("/rg removemember " + commandMessage[2], ""));
				else
					SlapHomebrew.worldGuard.put(commandMessage[2], SlapHomebrew.worldGuard.get(commandMessage[2]) + "<==>" + cmdDate.format(date) + " " + player.getName() + " removed member(s)"
							+ message.replace("/region removemember " + commandMessage[2], ""));

			} else if (commandMessage[1].equals("flag")) {
				if (commandMessage[0].equals("/rg"))
					SlapHomebrew.worldGuard.put(commandMessage[2], SlapHomebrew.worldGuard.get(commandMessage[2]) + "<==>" + cmdDate.format(date) + " " + player.getName() + " flagged region"
							+ message.replace("/rg flag " + commandMessage[2], ""));
				else
					SlapHomebrew.worldGuard.put(commandMessage[2], SlapHomebrew.worldGuard.get(commandMessage[2]) + "<==>" + cmdDate.format(date) + " " + player.getName() + " flagged region"
							+ message.replace("/region flag " + commandMessage[2], ""));

			} else if (commandMessage[1].equals("setpriority")) {
				if (commandMessage[0].equals("/rg"))
					SlapHomebrew.worldGuard.put(commandMessage[2], SlapHomebrew.worldGuard.get(commandMessage[2]) + "<==>" + cmdDate.format(date) + " " + player.getName() + " set the priority to"
							+ message.replace("/rg setpriority " + commandMessage[2], ""));
				else
					SlapHomebrew.worldGuard.put(commandMessage[2], SlapHomebrew.worldGuard.get(commandMessage[2]) + "<==>" + cmdDate.format(date) + " " + player.getName() + " set the priority to"
							+ message.replace("/region setpriority " + commandMessage[2], ""));
			} else if (commandMessage[1].equals("redefine")) {
				if (commandMessage[0].equals("/rg"))
					SlapHomebrew.worldGuard.put(commandMessage[2], SlapHomebrew.worldGuard.get(commandMessage[2]) + "<==>" + cmdDate.format(date) + " " + player.getName() + " redefined region"
							+ message.replace("/rg setpriority " + commandMessage[2], ""));
				else
					SlapHomebrew.worldGuard.put(commandMessage[2], SlapHomebrew.worldGuard.get(commandMessage[2]) + "<==>" + cmdDate.format(date) + " " + player.getName() + " redefined region"
							+ message.replace("/region setpriority " + commandMessage[2], ""));
			}
		} else if (commandMessage[0].equalsIgnoreCase("tjail")||commandMessage[0].equalsIgnoreCase("jail")||commandMessage[0].equalsIgnoreCase("togglejail")){
			String time = "";
			int i = 0;
			for(String string: commandMessage){
				if(i > 2)
					time+=string + " ";
				i++;
			}
			Jail jail = new Jail();
			System.out.println("Time: " + time);
			System.out.println("Allowed: " + jail.testJail(time));
			//TODO
		}
	}

	@EventHandler
	public void onPlayerChat(AsyncPlayerChatEvent event) {
		player = event.getPlayer();
		if (!Commands.chatBotBlocks.contains(player.getName())) {
			String message = event.getMessage().toLowerCase();
			if (message.contains("i") && message.contains("can") && message.contains("member") || message.contains("how") && message.contains("get") && message.contains("member")
					|| message.contains("how") && message.contains("become") && message.contains("member")) {
				serverMessage = plugin.getConfig().get("chatmessages.member").toString();
				ChatColors();
				player.sendMessage(serverMessage);
				event.setCancelled(true);
			}
			if (message.contains("how") && message.contains("can") && message.contains("vip") || message.contains("how") && message.contains("become") && message.contains("vip")
					|| message.contains("how") && message.contains("get") && message.contains("vip") || message.contains("can") && message.contains("use") && message.contains("tpa")
					|| message.contains("can") && message.contains("get") && message.contains("tp") || message.contains("do") && message.contains("have") && message.contains("tpa")) {
				serverMessage = plugin.getConfig().get("chatmessages.vip").toString();
				ChatColors();
				player.sendMessage(serverMessage);
				event.setCancelled(true);
			}
			if (message.contains("where") && message.contains("can") && message.contains("build") || message.contains("where") && message.contains("to") && message.contains("build")) {
				serverMessage = plugin.getConfig().get("chatmessages.build").toString();
				ChatColors();
				player.sendMessage(serverMessage);
				event.setCancelled(true);
			}
			if (message.contains("how") && message.contains("protect") && message.contains("house") || message.contains("how") && message.contains("worldguard") && message.contains("house")
					|| message.contains("how") && message.contains("protect") && message.contains("chest") || message.contains("how") && message.contains("lock") && message.contains("chest")
					|| message.contains("how") && message.contains("lock") && message.contains("chests") || message.contains("how") && message.contains("claim") && message.contains("plot")) {
				serverMessage = plugin.getConfig().get("chatmessages.worldguard").toString();
				ChatColors();
				player.sendMessage(serverMessage);
			}
			if (message.contains("do") && message.contains("you") && message.contains("lockette") || message.contains("does") && message.contains("server") && message.contains("lockette")) {
				serverMessage = plugin.getConfig().get("chatmessages.lockette").toString();
				ChatColors();
				player.sendMessage(serverMessage);
				event.setCancelled(true);
			}
			if (message.contains("how") && message.contains("use") && message.contains("shop") || message.contains("cant") && message.contains("use") && message.contains("shop")
					|| message.contains("how") && message.contains("buy") && message.contains("shop") || message.contains("why") && message.contains("can't") && message.contains("buy")
					|| message.contains("why") && message.contains("cant") && message.contains("buy")) {
				serverMessage = plugin.getConfig().get("chatmessages.shop").toString();
				ChatColors();
				player.sendMessage(serverMessage);
				event.setCancelled(true);
			}
			if (message.contains("how") && message.contains("make") && message.contains("money") && message.contains("i") || message.contains("how") && message.contains("do")
					&& message.contains("money") && message.contains("i") || message.contains("how") && message.contains("can") && message.contains("sell") || message.contains("how")
					&& message.contains("do") && message.contains("sell")) {
				serverMessage = plugin.getConfig().get("chatmessages.money").toString();
				ChatColors();
				player.sendMessage(serverMessage);
				event.setCancelled(true);
			}
			if (message.contains("how") && message.contains("check") && message.contains("worldguard") || message.contains("how") && message.contains("check") && message.contains("wg")
					|| message.contains("how") && message.contains("check") && message.contains("zones") || message.contains("how") && message.contains("check") && message.contains("zone")) {
				serverMessage = plugin.getConfig().get("chatmessages.checkwg").toString();
				ChatColors();
				player.sendMessage(serverMessage);
				event.setCancelled(true);
			}
			if (message.contains("how") && message.contains("i") && message.contains("pay") || message.contains("how") && message.contains("do") && message.contains("pay")) {
				serverMessage = plugin.getConfig().get("chatmessages.pay").toString();
				ChatColors();
				player.sendMessage(serverMessage);
				event.setCancelled(true);
			}
		}
		if (SlapHomebrew.message.contains(player.getName())) {
			String message = event.getMessage();
			player.sendMessage(ChatColor.GOLD + "[SLAP] " + ChatColor.WHITE + "The new message has " + Commands.messageName + " as name and " + message + " as message.");
			SlapHomebrew.message.remove(player.getName());
			plugin.getConfig().set("messages." + Commands.messageName, message);
			event.setCancelled(true);
			plugin.saveConfig();
		}
	}

	@EventHandler
	public void onBlockDispenseEvent(BlockDispenseEvent event) {
		if (event.getItem().getType() == Material.LAVA_BUCKET || event.getItem().getType() == Material.FIREBALL) {
			event.setCancelled(true);
		}
	}
}
