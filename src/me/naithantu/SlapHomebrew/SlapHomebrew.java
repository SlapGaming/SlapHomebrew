package me.naithantu.SlapHomebrew;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import me.naithantu.SlapHomebrew.Commands.BlockfaqCommand;
import me.naithantu.SlapHomebrew.Commands.CommandHandler;
import me.naithantu.SlapHomebrew.Listeners.*;
import me.naithantu.SlapHomebrew.Storage.YamlStorage;
import net.milkbowl.vault.Vault;
import net.milkbowl.vault.economy.Economy;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;

public class SlapHomebrew extends JavaPlugin {

	public SlapHomebrew plugin;
	public final Logger logger = Logger.getLogger("Minecraft");

	public static HashMap<String, Integer> usedGrant = new HashMap<String, Integer>();
	public static HashMap<Integer, Integer> vipItems = new HashMap<Integer, Integer>();
	public static HashMap<String, Location> backDeath = new HashMap<String, Location>();
	public static HashMap<String, String> worldGuard = new HashMap<String, String>();
	HashMap<Integer, String> plots = new HashMap<Integer, String>();
	List<Integer> unfinishedPlots = new ArrayList<Integer>();
	HashMap<Integer, String> forumVip = new HashMap<Integer, String>();
	List<Integer> unfinishedForumVip = new ArrayList<Integer>();

	private YamlStorage dataStorage;
	private YamlStorage vipStorage;
	private YamlStorage timeStorage;
	
	private FileConfiguration dataConfig;
	private FileConfiguration vipConfig;
	
	Vip vip;

	public static HashSet<String> message = new HashSet<String>();
	public static HashSet<String> tpBlocks = new HashSet<String>();
	
	Vehicles vehicles = new Vehicles();
	Bump bump = new Bump(this);

	public static boolean allowCakeTp;

	Configuration config;

	ArrayList<String> tempArrayList = new ArrayList<String>();

	boolean reloadChatBot = false;

	public void setReloadChatBot(boolean reloadChatBot) {
		this.reloadChatBot = reloadChatBot;
	}

	public int timerTime = 144000;
	PluginManager pm;
	public Boolean debug = false;

	public static Economy econ = null;

	public static Vault vault = null;

	VipForumMarkCommands vipForumMarkCommands = new VipForumMarkCommands(this);
	CommandHandler commandHandler = new CommandHandler(this);

	@Override
	public void onEnable() {
		config = getConfig();
		dataStorage = new YamlStorage(this, "data");
		vipStorage = new YamlStorage(this, "vip");
		timeStorage = new YamlStorage(this, "time");
		dataConfig = dataStorage.getConfig();
		vipConfig = vipStorage.getConfig();
		vip = new Vip(vipStorage);
		loadItems();
		tpBlocks = loadHashSet("tpblocks");
		BlockfaqCommand.chatBotBlocks = loadHashSet("chatbotblocks");
		setupEconomy();
		loadUses();
		setupChatBot();
		loadworldGuard();
		loadPlots();
		loadUnfinishedForumVip();
		loadForumVip();
		loadUnfinishedPlots();
		bump.bumpTimer();
		Lottery lottery = new Lottery(this);
		lottery.lotteryTimer();
		pm = getServer().getPluginManager();
		pm.registerEvents(new ChatListener(this), this);
		pm.registerEvents(new CommandListener(), this);
		pm.registerEvents(new CreatureSpawnListener(), this);
		pm.registerEvents(new DeathListener(this), this);
		pm.registerEvents(new DispenseListener(), this);
		pm.registerEvents(new InteractListener(this), this);
		pm.registerEvents(new LoginListener(this, timeStorage, dataStorage, vipStorage), this);
		pm.registerEvents(new PotionListener(), this);
		pm.registerEvents(new QuitListener(this, timeStorage), this);
		pm.registerEvents(new TeleportListener(vehicles), this);
		pm.registerEvents(new VehicleListener(vehicles), this);
		pm.registerEvents(new PlayerInteractEntityListener(), this);

		Plugin x = this.getServer().getPluginManager().getPlugin("Vault");
		if (x != null & x instanceof Vault) {
			vault = (Vault) x;
		} else {
			logger.warning(String.format("[%s] Vault was _NOT_ found! Disabling plugin.", getDescription().getName()));
			getPluginLoader().disablePlugin(this);
			return;
		}
		//Create configurationsection if it isn't there yet:
		if (vipConfig.getConfigurationSection("vipdays") == null) {
			vipConfig.createSection("vipdays");
		}
		saveConfig();
		removeInvisibility();
	}

	@Override
	public void onDisable() {
		saveItems();
		saveUses();
		saveworldGuard();
		saveHashSet(tpBlocks, "tpblocks");
		saveHashSet(BlockfaqCommand.chatBotBlocks, "chatbotblocks");
		saveUnfinishedPlots();
		savePlots();
		saveForumVip();
		saveUnfinishedForumVip();
	}

	public WorldGuardPlugin getWorldGuard() {
		Plugin plugin = getServer().getPluginManager().getPlugin("WorldGuard");

		// WorldGuard may not be loaded
		if (plugin == null || !(plugin instanceof WorldGuardPlugin)) {
			return null;
		}
		return (WorldGuardPlugin) plugin;
	}
	
	public Vehicles getVehicles(){
		return vehicles;
	}
	
	public Vip getVip(){
		return vip;
	}

	public YamlStorage getTimeStorage() {
		return timeStorage;
	}
	
	public YamlStorage getVipStorage(){
		return vipStorage;
	}
	
	public YamlStorage getDataStorage(){
		return dataStorage;
	}

	public List<Integer> getUnfinishedPlots() {
		return unfinishedPlots;
	}

	public HashMap<Integer, String> getPlots() {
		return plots;
	}

	public List<Integer> getUnfinishedForumVip() {
		return unfinishedForumVip;
	}

	HashMap<Integer, String> getForumVip() {
		return forumVip;
	}

	public VipForumMarkCommands getVipForumMarkCommands() {
		return vipForumMarkCommands;
	}

	private boolean setupEconomy() {
		if (getServer().getPluginManager().getPlugin("Vault") == null) {
			return false;
		}
		RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
		if (rsp == null) {
			return false;
		}
		econ = rsp.getProvider();
		return econ != null;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void saveHashSet(HashSet hashSet, String configString) {
		List<String> tempList = new ArrayList<String>(hashSet);
		dataConfig.set(configString, null);
		dataConfig.set(configString, tempList);
		dataStorage.saveConfig();
	}

	public HashSet<String> loadHashSet(String configString) {
		List<String> tempList = dataConfig.getStringList(configString);
		HashSet<String> hashSet = new HashSet<String>(tempList);
		return hashSet;
	}

	public void saveTheConfig() {
		reloadConfig();
		saveConfig();
	}

	public void setupChatBot() {
		if (!config.contains("chatmessages.member") || reloadChatBot == true) {
			config.set("chatmessages.member", "&c[FAQ] &3Go to &bwww.slap-gaming.com/apply &3to apply for member!");
		}
		if (!config.contains("chatmessages.vip") || reloadChatBot == true) {
			config.set("chatmessages.vip", "&c[FAQ] &3Go to www.slap-gaming.com/vip for more information about VIP!");
		}
		if (!config.contains("chatmessages.build") || reloadChatBot == true) {
			config.set("chatmessages.build", "&c[FAQ] &3Find an empty plot, then ask a mod/admin for a worldguard! Take the teleport pad at spawn or use the online dynamap (slap-gaming.com/map) to go to empty plots!");
		}
		if (!config.contains("chatmessages.worldguard") || reloadChatBot == true) {
			config.set("chatmessages.worldguard", "&c[FAQ] &3Ask a mod/admin for a worldguard! This also protects chests!");
		}
		if (!config.contains("chatmessages.lockette") || reloadChatBot == true) {
			config.set("chatmessages.lockette", "&c[FAQ] &3We don't use lockette, worldguard also protects your chests!");
		}
		if (!config.contains("chatmessages.shop") || reloadChatBot == true) {
			config.set("chatmessages.shop", "&c[FAQ] &3You need to be a member to use the shop! Right click for an item, shift + right click for a stack of items!");
		}
		if (!config.contains("chatmessages.money") || reloadChatBot == true) {
			config.set("chatmessages.money", "&c[FAQ] &3Type /sell hand with an item you want to sell in your hand, or go to www.slap-gaming.com/money to get money!");
		}
		if (!config.contains("chatmessages.checkwg") || reloadChatBot == true) {
			config.set("chatmessages.checkwg", "&c[FAQ] &3Right click the ground with string to see zones!");
		}
		if (!config.contains("chatmessages.pay") || reloadChatBot == true) {
			config.set("chatmessages.pay", "&c[FAQ] &3Type /money pay [name] [amount]!");
		}

		reloadChatBot = false;
		this.saveConfig();
	}

	public void saveUses() {
		dataConfig.set("vipuses", null);
		for (Map.Entry<String, Integer> entry : usedGrant.entrySet()) {
			dataConfig.set("vipuses." + entry.getKey(), entry.getValue());
		}
		dataStorage.saveConfig();
	}

	public void loadUses() {
		if (dataConfig.getConfigurationSection("vipuses") == null)
			return;
		for (String key : dataConfig.getConfigurationSection("vipuses").getKeys(false)) {
			usedGrant.put(key, dataConfig.getInt("vipuses." + key));
		}
	}

	public void saveItems() {
		dataConfig.set("vipitems", null);
		for (Map.Entry<Integer, Integer> entry : vipItems.entrySet()) {
			dataConfig.set("vipitems." + Integer.toString(entry.getKey()), entry.getValue());
		}
		dataStorage.saveConfig();
	}

	public void loadItems() {
		if (dataConfig.getConfigurationSection("vipitems") == null)
			return;
		for (String key : dataConfig.getConfigurationSection("vipitems").getKeys(false)) {
			vipItems.put(Integer.valueOf(key), dataConfig.getInt("vipitems." + key));
		}
	}

	public void savePlots() {
		dataConfig.set("plots", null);
		for (Map.Entry<Integer, String> entry : plots.entrySet()) {
			dataConfig.set("plots." + entry.getKey(), entry.getValue());
		}
		dataStorage.saveConfig();
	}

	public void loadPlots() {
		if (dataConfig.getConfigurationSection("plots") == null)
			return;
		for (String key : dataConfig.getConfigurationSection("plots").getKeys(false)) {
			plots.put(Integer.valueOf(key), dataConfig.getString("plots." + key));
		}
	}

	@SuppressWarnings("rawtypes")
	public void saveworldGuard() {
		File worldguard = new File("plugins" + File.separator + "SlapHomebrew" + File.separator + "worldGuard.yml");
		if (!worldguard.exists()) {
			try {
				new File("plugins" + File.separator + "SlapHomebrew").mkdir();
				worldguard.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			worldguard.delete();
			try {
				new File("plugins" + File.separator + "SlapHomebrew").mkdir();
				worldguard.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		try {
			FileWriter fIn = new FileWriter("plugins" + File.separator + "SlapHomebrew" + File.separator + "worldGuard.yml");
			BufferedWriter oIn = new BufferedWriter(fIn);
			Set<?> set = worldGuard.entrySet();
			Iterator<?> i = set.iterator();
			tempArrayList.clear();
			while (i.hasNext()) {
				Map.Entry me = (Map.Entry) i.next();
				oIn.write(me.getKey().toString() + ":" + me.getValue().toString());
				oIn.newLine();
			}
			oIn.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void loadworldGuard() {
		FileReader fRead = null;
		try {
			fRead = new FileReader("plugins" + File.separator + "SlapHomebrew" + File.separator + "worldGuard.yml");
			BufferedReader bRead = new BufferedReader(fRead);
			String tempString;
			while ((tempString = bRead.readLine()) != null) {
				String[] tempList = tempString.split(":", 2);
				worldGuard.put(tempList[0], tempList[1]);
			}
			bRead.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public void saveUnfinishedPlots() {
		dataConfig.set("unfinishedplots", unfinishedPlots);
		dataStorage.saveConfig();
	}

	public void loadUnfinishedPlots() {
		unfinishedPlots = dataConfig.getIntegerList("unfinishedplots");
	}

	public void removeInvisibility() {
		getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
			public void run() {
				for (Player player : getServer().getOnlinePlayers()) {
					Collection<PotionEffect> potionEffects = player.getActivePotionEffects();
					for (PotionEffect effect : potionEffects) {
						if (effect.getType().equals(PotionEffectType.INVISIBILITY)) {
							player.sendMessage(ChatColor.GOLD + "[SLAP]" + ChatColor.WHITE + " Invisibility potions are not allowed, potion effect removed!");
							player.removePotionEffect(PotionEffectType.INVISIBILITY);
						}
					}
				}
			}
		}, 0, 20);
	}

	public void addBumpDone(String name) {
		String date = new SimpleDateFormat("MMM.d HH:mm z").format(new Date());
		date = date.substring(0, 1).toUpperCase() + date.substring(1);
		dataConfig.set("bumps." + date, name);
		dataStorage.saveConfig();
	}

	public void saveUnfinishedForumVip() {
		dataConfig.set("unfinishedforumvip", unfinishedForumVip);
		dataStorage.saveConfig();
	}

	public void loadUnfinishedForumVip() {
		unfinishedForumVip = dataConfig.getIntegerList("unfinishedforumvip");
	}

	public void saveForumVip() {
		dataConfig.set("forumvip", null);
		for (Map.Entry<Integer, String> entry : forumVip.entrySet()) {
			dataConfig.set("forumvip." + entry.getKey(), entry.getValue());
		}
		dataStorage.saveConfig();
	}

	public void loadForumVip() {
		if (dataConfig.getConfigurationSection("forumvip") == null)
			return;
		for (String key : dataConfig.getConfigurationSection("forumvip").getKeys(false)) {
			forumVip.put(Integer.valueOf(key), dataConfig.getString("forumvip." + key));
		}
	}

	public Bump getBump() {
		return bump;
	}
	
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
		return commandHandler.handle(sender, cmd, args);
	}
}