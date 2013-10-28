package me.naithantu.SlapHomebrew.Controllers;

import java.util.ArrayList;

import me.naithantu.SlapHomebrew.SlapHomebrew;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.mcsg.double0negative.tabapi.TabAPI;

import ru.tehkode.permissions.PermissionGroup;
import ru.tehkode.permissions.PermissionUser;
import ru.tehkode.permissions.bukkit.PermissionsEx;

public class TabController {
	
	private SlapHomebrew plugin;
	private PlayerLogger playerLogger;
	
	private ArrayList<String> ops;
	private ArrayList<String> admins;
	private ArrayList<String> mods;
	private ArrayList<String> guides;
	private ArrayList<String> specials;
	private ArrayList<String> vips;
	private ArrayList<String> slaps;
	private ArrayList<String> members;
	private ArrayList<String> builders;
	
	public TabController(SlapHomebrew plugin, PlayerLogger playerLogger) {
		this.plugin = plugin;
		this.playerLogger = playerLogger;
		ops = new ArrayList<>();
		admins = new ArrayList<>();
		mods = new ArrayList<>();
		guides = new ArrayList<>();
		specials = new ArrayList<>();
		vips = new ArrayList<>();
		slaps = new ArrayList<>();
		members = new ArrayList<>();
		builders = new ArrayList<>();
		onEnable();
	}
	
	private void createTab(){
		int tabSize = 0;
		if (ops.size() > 0) tabSize = tabSize + ops.size() + 1;
		if (admins.size() > 0) tabSize = tabSize + admins.size() + 1;
		if (mods.size() > 0) tabSize = tabSize + mods.size() + 1;
		if (guides.size() > 0) tabSize = tabSize + guides.size() + 1;
		if (specials.size() > 0) tabSize = tabSize + specials.size() + 1;
		if (vips.size() > 0) tabSize = tabSize + vips.size() + 1;
		if (slaps.size() > 0) tabSize = tabSize + slaps.size() + 1;
		if (members.size() > 0) tabSize = tabSize + members.size() + 1;
		if (builders.size() > 0) tabSize = tabSize + builders.size() + 1;
		if (tabSize == 0) return;
		int x = 0;
		int playersOnline = plugin.getServer().getOnlinePlayers().length;
		int maxPlayers = plugin.getServer().getMaxPlayers();
		String[] tab = new String[tabSize];
		boolean fOps; boolean fAdmins; boolean fMods; boolean fGuides; boolean fSpecials; boolean fVips; boolean fSlaps; boolean fMembers; boolean fBuilders;
		fOps = fAdmins = fMods = fGuides = fSpecials = fVips = fSlaps = fMembers = fBuilders = true;
		for (String p : ops) { 
			if (fOps) { fOps = false; tab[x] = ChatColor.DARK_RED + "-- Owners --"; x++; }
			tab[x] = ChatColor.DARK_RED + p; x++; 
			}
		for (String p : admins) { 
			if (fAdmins) { fAdmins = false; tab[x] = ChatColor.RED + "-- Admins --"; x++; }
			tab[x] = ChatColor.RED + p; x++; 
			}
		for (String p : mods) { 
			if (fMods) { fMods = false; tab[x] = ChatColor.AQUA + "-- Mods --"; x++; }
			tab[x] = ChatColor.AQUA + p; x++; 
			}
		for (String p : guides) { 
			if (fGuides) { fGuides = false; tab[x] = ChatColor.GOLD + "-- Guides --"; x++; }
			tab[x] = ChatColor.GOLD + p; x++; 
			}
		for (String p : specials) {
			if (fSpecials) { fSpecials = false; tab[x] = ChatColor.DARK_AQUA + "-- Specials --"; x++; }
			tab[x] = ChatColor.DARK_AQUA + p; x++;
			}
		for (String p : vips) { 
			if (fVips) { fVips = false; tab[x] = ChatColor.BLUE + "-- VIPs --"; x++; }
			tab[x] = ChatColor.BLUE + p; x++; 
			}
		for (String p : slaps) { 
			if (fSlaps) { fSlaps = false; tab[x] = ChatColor.YELLOW + "-- SLAPs --"; x++; }
			tab[x] = ChatColor.YELLOW + p; x++; 
			}
		for (String p : members) { 
			if (fMembers) { fMembers = false; tab[x] = ChatColor.GREEN + "-- Members --"; x++; }
			tab[x] = ChatColor.GREEN + p; x++; 
			}
		for (String p : builders) { 
			if (fBuilders) { fBuilders = false; tab[x] = ChatColor.DARK_GREEN + "-- Builders --"; x++; }
			tab[x] = ChatColor.DARK_GREEN + p; x++; 
			}
		for (Player onlinePlayer : plugin.getServer().getOnlinePlayers()) {
			updateTab(onlinePlayer, tab, playersOnline, maxPlayers);
		}
		TabAPI.updateAll();
	}
	
	private void updateTab(Player p, String[] tab, int playersOnline, int maxPlayers) {
		TabAPI.clearTab(p);
		TabAPI.setPriority(plugin, p, 0);
		TabAPI.setTabString(plugin, p, 0, 0, ChatColor.GOLD + "Welcome to");
		TabAPI.setTabString(plugin, p, 0, 1, ChatColor.GOLD + "SLAPGaming");
		TabAPI.setTabString(plugin, p, 0, 2, ChatColor.GOLD + "Online " + playersOnline + "/" + maxPlayers);
		
		int row = 2; int colom = 0;
		for (String player : tab) {
			TabAPI.setTabString(plugin, p, row, colom, player);
			row++;
			if (row == 20) {
				row = 2;
				colom++;
				if (colom == 3) {
					return;
				}
			}
		}
	}
	
	public void playerJoin(Player p) {
		String playerName = p.getName();
		addToGroup(playerName);
		plugin.getServer().getScheduler().runTaskLater(plugin, new Runnable() {
			@Override
			public void run() {
				createTab();
			}
		}, 2);
	}
	
	public void playerQuit(Player p) {
		String playerName = p.getName();
		removeFromGroups(playerName);
		plugin.getServer().getScheduler().runTaskLater(plugin, new Runnable() {
			@Override
			public void run() {
				createTab();
			}
		}, 2);
	}
	
	private void onEnable(){
		int x = 0;
		for (Player p : plugin.getServer().getOnlinePlayers()) {
			addToGroup(p.getName());
			x++;
		}
		if (x > 0) {
			createTab();
		}
	}
	
	private void addToGroup(String playerName) {
		PermissionUser user = PermissionsEx.getUser(playerName);
		if (user != null) {
			PermissionGroup[] groups = user.getGroups();
			if (groups != null) {
				String prefix;
				boolean added = false;
				if (groups.length > 0) {
					switch (groups[0].getName()) {
					case "builder":
						addToList(builders, playerName);
						break;
					case "Member":
						addToList(members, playerName);
						break;
					case "Slap":
						addToList(slaps, playerName);
						break;
					case "VIP":
						prefix = user.getPrefix();
						if (prefix != null) {
							if (prefix.toLowerCase().contains("slap")) {
								addToList(slaps, playerName);
								added = true;
							}
						}
						if (!added) addToList(vips, playerName);
						break;
					case "Guide": case "VIPGuide":
						addToList(guides, playerName);
						break;
					case "Mod":
						addToList(mods, playerName);
						break;
					case "Admin":
						addToList(admins, playerName);
						break;
					case "SuperAdmin":
						TabGroup group = playerLogger.getSuperAdminGroup(playerName);
						if (group == null) {
							addToList(ops, playerName);
						} else {
							switch(group) {
							case builders:	addToList(builders, playerName);	break;
							case members: 	addToList(members, playerName); 	break;
							case slaps: 	addToList(slaps, playerName); 		break;
							case vips: 		addToList(vips, playerName); 		break;
							case specials: 	addToList(specials, playerName);	break;
							case guides: 	addToList(guides, playerName);		break;
							case mods: 		addToList(mods, playerName);		break;
							case admins:	addToList(admins, playerName);		break;
							default:		addToList(ops, playerName);
							}
						}
						break;
					}
				}
			}
		}
	}
	
	private void addToList(ArrayList<String> list, String player) {
		if (!list.contains(player)) {
			list.add(player);
		}
	}
	
	private void removeFromGroups(String playerName) {
		boolean removed = false; int x = 1;
		while (removed == false) {
			switch(x) {
			case 1:	if (builders.remove(playerName)) removed = true; break;
			case 2:	if (members.remove(playerName)) removed = true; break;
			case 3:	if (slaps.remove(playerName)) removed = true; break;
			case 4:	if (vips.remove(playerName)) removed = true; break;
			case 5: if (specials.remove(playerName)) removed = true; break;
			case 6:	if (guides.remove(playerName)) removed = true; break;
			case 7:	if (mods.remove(playerName)) removed = true; break;
			case 8:	if (admins.remove(playerName)) removed = true; break;
			case 9:	if (ops.remove(playerName)) removed = true; break;
			default: return;
			}
			x++;
		}
	}
	
	public void playerSwitchGroup(Player p) {
		String playerName = p.getName();
		removeFromGroups(playerName);
		playerJoin(p);	
	}
	
	public void reEnable() {
		builders.clear();
		members.clear();
		slaps.clear();
		vips.clear();
		specials.clear();
		guides.clear();
		mods.clear();
		admins.clear();
		ops.clear();
		
		onEnable();
	}
	
	public enum TabGroup {
		builders, members, slaps, vips, specials, guides, mods, admins, ops;
	}

}
