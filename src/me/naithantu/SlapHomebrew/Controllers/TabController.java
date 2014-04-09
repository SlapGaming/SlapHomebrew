package me.naithantu.SlapHomebrew.Controllers;

import java.util.ArrayList;
import java.util.List;

import me.naithantu.SlapHomebrew.Storage.YamlStorage;
import me.naithantu.SlapHomebrew.Util.Util;
import nl.stoux.slapbridged.bukkit.SlapBridged;
import nl.stoux.slapbridged.objects.OtherPlayer;
import nl.stoux.slapbridged.objects.OtherServer;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.mcsg.double0negative.tabapi.TabAPI;

import ru.tehkode.permissions.PermissionGroup;
import ru.tehkode.permissions.PermissionUser;
import ru.tehkode.permissions.bukkit.PermissionsEx;

public class TabController extends AbstractController {
	
	private PlayerLogger playerLogger;
	
	private ArrayList<String> ops;
	private ArrayList<String> admins;
	private ArrayList<String> mods;
	private ArrayList<String> specials;
	private ArrayList<String> vips;
	private ArrayList<String> members;
	private ArrayList<String> builders;
	
	private int maxPlayers;
	
	private YamlStorage yaml;
	private FileConfiguration config;
	
	private boolean tabApiSetup;
	
	public TabController(PlayerLogger playerLogger) {
		this.playerLogger = playerLogger;
				
		//Get the max players
		yaml = new YamlStorage(plugin, "tabsettings");
		config = yaml.getConfig();
		maxPlayers = config.getInt("maxplayers");
		
		//Check if TabAPI up & Running
		tabApiSetup = false;
		TabAPI tabApi = (TabAPI) plugin.getServer().getPluginManager().getPlugin("TabAPI");
		if (tabApi != null) {
			tabApiSetup = tabApi.isEnabled();
		}
		
		if (!tabApiSetup) return;
		
		//Create the lists
		ops = new ArrayList<>();
		admins = new ArrayList<>();
		mods = new ArrayList<>();
		specials = new ArrayList<>();
		vips = new ArrayList<>();
		members = new ArrayList<>();
		builders = new ArrayList<>();
		
		//Check for players
		onEnable();
	}
	
	private void createTab(){
		int tabSize = 0;
		if (ops.size() > 0) tabSize = tabSize + ops.size() + 1;
		if (admins.size() > 0) tabSize = tabSize + admins.size() + 1;
		if (mods.size() > 0) tabSize = tabSize + mods.size() + 1;
		if (specials.size() > 0) tabSize = tabSize + specials.size() + 1;
		if (vips.size() > 0) tabSize = tabSize + vips.size() + 1;
		if (members.size() > 0) tabSize = tabSize + members.size() + 1;
		if (builders.size() > 0) tabSize = tabSize + builders.size() + 1;
		
		List<OtherServer> otherServers = null;
		int otherPlayers = 0;
		if (plugin.hasSlapBridged()) { //See if API available
			if ((otherPlayers = SlapBridged.getAPI().getTotalPlayersOnline()) > 0) {
				tabSize++;
			}
			
			otherServers = SlapBridged.getAPI().getOtherServers(); //Get other servers
			for (OtherServer server : otherServers) { //Loop thru servers 
				if (server.getNrOfPlayersOnline() > 0) { //If any players online
					tabSize += server.getNrOfPlayersOnline() + 1;
				} else {
					tabSize += 2; //Name + "No players"
				}
			}
		}
		
		if (tabSize == 0) return;
		
		int x = 0;
		int playersOnline = getOnlinePlayers();
		String[] tab = new String[tabSize];
		boolean fOps; boolean fAdmins; boolean fMods; boolean fSpecials; boolean fVips; boolean fMembers; boolean fBuilders;
		fOps = fAdmins = fMods = fSpecials = fVips = fMembers = fBuilders = true;
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
		for (String p : specials) {
			if (fSpecials) { fSpecials = false; tab[x] = ChatColor.DARK_AQUA + "-- Specials --"; x++; }
			tab[x] = ChatColor.DARK_AQUA + p; x++;
			}
		for (String p : vips) { 
			if (fVips) { fVips = false; tab[x] = ChatColor.BLUE + "-- VIPs --"; x++; }
			tab[x] = ChatColor.BLUE + p; x++; 
			}
		for (String p : members) { 
			if (fMembers) { fMembers = false; tab[x] = ChatColor.GREEN + "-- Members --"; x++; }
			tab[x] = ChatColor.GREEN + p; x++; 
			}
		for (String p : builders) { 
			if (fBuilders) { fBuilders = false; tab[x] = ChatColor.DARK_GREEN + "-- Builders --"; x++; }
			tab[x] = ChatColor.DARK_GREEN + p; x++; 
			}
		
		//Check other servers
		if (otherServers != null) {
			if (otherPlayers > 0) {
				tab[x++] = ChatColor.WHITE + "     " + ChatColor.WHITE;
			}
			for (OtherServer otherServer : otherServers) {
				tab[x++] = Util.colorize(otherServer.getTabName()); //Set Server name
				String color = Util.colorize(otherServer.getTabName().substring(0, 2)); //Get color
				if (otherServer.getNrOfPlayersOnline() > 0) { //If players online
					for (OtherPlayer p : otherServer.getPlayers().values()) {
						tab[x++] = color + p.getPlayername();
					}
				} else {
					tab[x++] = color + "No players";
				}
			}
		}
		
		for (Player onlinePlayer : plugin.getServer().getOnlinePlayers()) {
			updateTab(onlinePlayer, tab, playersOnline, (maxPlayers > playersOnline ? maxPlayers : maxPlayers * 2));
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
		if (!tabApiSetup) return; //Return if not setup
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
		if (!tabApiSetup) return; //Return if not setup
		String playerName = p.getName();
		removeFromGroups(playerName);
		plugin.getServer().getScheduler().runTaskLater(plugin, new Runnable() {
			@Override
			public void run() {
				createTab();
			}
		}, 2);
	}
	
	/**
	 * Other server Tab Activity
	 */
	public void otherServerActivity() {
		if (!tabApiSetup) return; //Return if not setup
		
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
				if (groups.length > 0) {
					switch (groups[0].getName()) {
					case "builder":
						addToList(builders, playerName);
						break;
					case "Member": case "Guide": case "Slap":
						addToList(members, playerName);
						break;
					case "VIP": case "VIPGuide":
						addToList(vips, playerName);
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
							case vips: 		addToList(vips, playerName); 		break;
							case specials: 	addToList(specials, playerName);	break;
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
			case 3:	if (vips.remove(playerName)) removed = true; break;
			case 4: if (specials.remove(playerName)) removed = true; break;
			case 5:	if (mods.remove(playerName)) removed = true; break;
			case 6:	if (admins.remove(playerName)) removed = true; break;
			case 7:	if (ops.remove(playerName)) removed = true; break;
			default: return;
			}
			x++;
		}
	}
	
	public void playerSwitchGroup(Player p) {
		if (!tabApiSetup) return; //Return if not setup
		String playerName = p.getName();
		removeFromGroups(playerName);
		playerJoin(p);	
	}
	
	public void reEnable() {
		if (!tabApiSetup) return; //Return if not setup
		builders.clear();
		members.clear();
		vips.clear();
		specials.clear();
		mods.clear();
		admins.clear();
		ops.clear();
		
		onEnable();
	}
	
	public enum TabGroup {
		builders, members, vips, specials, mods, admins, ops;
	}
	
	/**
	 * Set the max number of players in TAB & /List
	 * @param players The number of players
	 */
	public void setMaxPlayers(int players) {
		maxPlayers = players;
		config.set("maxplayers", players);
		yaml.saveConfig();
	}
	
	/**
	 * Get the max amount of players
	 * @return the number of players
	 */
	public int getMaxPlayers() {
		return maxPlayers;
	}
	
	/**
	 * Get the number of online players from all servers
	 * @return online players
	 */
	public int getOnlinePlayers() {
		int players = Util.getOnlinePlayers().length;
		if (plugin.hasSlapBridged()) { //If API available
			players += SlapBridged.getAPI().getTotalPlayersOnline();
		}
		return players;
	}
	
    @Override
    public void shutdown() {
    	//Not needed
    }
	

}
