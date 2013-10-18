package me.naithantu.SlapHomebrew;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import me.naithantu.SlapHomebrew.Commands.CommandHandler;
import me.naithantu.SlapHomebrew.Commands.Basics.BlockfaqCommand;
import me.naithantu.SlapHomebrew.Commands.Basics.SpawnCommand;
import me.naithantu.SlapHomebrew.Controllers.*;
import me.naithantu.SlapHomebrew.Listeners.*;
import me.naithantu.SlapHomebrew.Listeners.Entity.*;
import me.naithantu.SlapHomebrew.Listeners.Player.*;
import me.naithantu.SlapHomebrew.Storage.YamlStorage;
import net.milkbowl.vault.economy.Economy;

import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import com.earth2me.essentials.Essentials;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;

public class SlapHomebrew extends JavaPlugin {

	/**
	 * BackDeath HashMap - Key Playername -> Value DeathLocation
	 */
	private HashMap<String, Location> backDeathMap = new HashMap<String, Location>();
	
	/**
	 * worldGuard HashMap - Key Regionname -> value x
	 */
	private HashMap<String, String> regionMap = new HashMap<String, String>();
	
	/**
	 * Plots HashMap - Key PlotRequestID -> Value Info
	 * UnfinishedPlots List - List of unfinished PlotRequestIDs
	 */
	private HashMap<Integer, String> plots = new HashMap<Integer, String>();
	private List<Integer> unfinishedPlots = new ArrayList<Integer>();
	
	/**
	 * forumVip HashMap - Key VIPForumID -> Value Info
	 * unfinishedForumVip - List of unfinished VIPForumIDs
	 */
	private HashMap<Integer, String> forumVip = new HashMap<Integer, String>();
	private List<Integer> unfinishedForumVip = new ArrayList<Integer>();

	/**
	 * messages HashSet - Contains all messages
	 * tpBlocks HashSet - Contains all tpBlocks
	 */
	private HashSet<String> messages = new HashSet<String>();
	private HashSet<String> tpBlocks = new HashSet<String>();
	
	/**
	 * YamlStorage files
	 * FileConfigs
	 */
	private YamlStorage dataStorage;
	private YamlStorage vipStorage;
	private YamlStorage timeStorage;
	private YamlStorage sonicStorage;
	private YamlStorage vipGrantStorage;
	private YamlStorage applyThreadStorage;
	private FileConfiguration dataConfig;
	private FileConfiguration vipConfig;
	private Configuration config;

	/**
	 * Controllers
	 */
	private AwayFromKeyboard afk;
	private Bump bump;
	private ChangeLog changeLog;
	private DuelArena duelArena;
	private Extras extras;
	private FireworkShow show;
	private Horses horses;
	private Jails jails;
	private Lag lag;
	private Lottery lottery;
	private Mail mail;
	private PlayerLogger playerLogger;
	private Sonic sonic;
	private TabController tabController;
	private Vip vip;
	private WorthList worthList;

	/**
	 * External
	 */
	private Essentials essentials;
	private Economy economy;
	private WorldGuardPlugin worldGuard;
	
	/**
	 * allowCakeTp boolean - 
	 */
	private boolean allowCakeTp;

	/**
	 * The CommandHandler
	 */
	private CommandHandler commandHandler;

	
	/*
	 **************************************
	 * JavaPlugin methods
	 **************************************
	 */	
	
	@Override
	public void onEnable() {
		//Initialize
		initializeExternals();
		initializeYamlStoragesConfigs();
		initializeLoaders();
		initializeControllers();
		initializeListeners();
		
		//Setup ChatBot
		setupChatBot();
		
		//Create Schedulers -> Runnables & Make the commandHandler
		new Schedulers(this);
		commandHandler = new CommandHandler(this);

		//Create configurationsection if it isn't there yet:
		if (vipConfig.getConfigurationSection("vipdays") == null) {
			vipConfig.createSection("vipdays");
		}
		
		//Set resource world
		String rwWorld = config.getString("resourceworld");
		if (rwWorld == null) {
			config.set("resourceworld", "world_resource13");
			rwWorld = "world_resource13";
		}
		SpawnCommand.setResourceWorldName(rwWorld);
		
		saveConfig();
	}

	@Override
	public void onDisable() {
		getServer().getScheduler().cancelTasks(this);
		disableSavers();
		disableControllers();
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
		return commandHandler.handle(sender, cmd, args);
	}
	
	
	/*
	 **************************************
	 * Initializers
	 **************************************
	 */	
	
	private void initializeExternals() {
		Plugin vault = getServer().getPluginManager().getPlugin("Vault");
		if (vault == null) {
			getLogger().warning(String.format("[%s] Vault was _NOT_ found! Disabling plugin.", getDescription().getName()));
			getPluginLoader().disablePlugin(this);
			return;
		}
		essentials = (Essentials) getServer().getPluginManager().getPlugin("Essentials");
		worldGuard = (WorldGuardPlugin) getServer().getPluginManager().getPlugin("WorldGuard");
		RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
		if (rsp != null) {
			economy = rsp.getProvider();
		}
	}

	private void initializeYamlStoragesConfigs() {
		config = getConfig();
		dataStorage = new YamlStorage(this, "data");
		vipStorage = new YamlStorage(this, "vip");
		timeStorage = new YamlStorage(this, "time");
		sonicStorage = new YamlStorage(this, "sonic");
		vipGrantStorage = new YamlStorage(this, "vipgrant");
		applyThreadStorage = new YamlStorage(this, "ApplyThreads");
		dataConfig = dataStorage.getConfig();
		vipConfig = vipStorage.getConfig();
	}
	
	private void initializeControllers() {
		 bump = new Bump(this, dataStorage, dataConfig);
		 changeLog = new ChangeLog(this);
		 duelArena = new DuelArena(this);
		 extras = new Extras(this);
		 show = new FireworkShow(this);
		 horses = new Horses(this);
		 jails = new Jails(this);
		 lag = new Lag(this);
		 lottery = new Lottery(this);
		 mail = new Mail(this);
		 playerLogger = new PlayerLogger(this);
		 sonic = new Sonic(this);
		 afk = new AwayFromKeyboard(this, playerLogger);
		 tabController = new TabController(this, playerLogger);
		 vip = new Vip(this, vipStorage, tabController);
		 worthList = new WorthList(this);
		 
		 new ApplyChecker(this, essentials, tabController);
	}
	
	private void initializeLoaders() {
		tpBlocks = loadHashSet("tpblocks");
		BlockfaqCommand.chatBotBlocks = loadHashSet("chatbotblocks");		
		loadworldGuard();
		loadPlots();
		loadUnfinishedForumVip();
		loadForumVip();
		loadUnfinishedPlots();
	}
	
	private void initializeListeners() {
		PluginManager pm = getServer().getPluginManager();
		pm.registerEvents(new BlockPlaceListener(this), this);
		pm.registerEvents(new PlayerChatListener(this, afk, jails, playerLogger), this);
		pm.registerEvents(new PlayerCommandListener(this, afk, jails, playerLogger), this);
		pm.registerEvents(new CreatureSpawnListener(this), this);
		pm.registerEvents(new CreatureDeathListener(this, horses), this);
		pm.registerEvents(new DuelArenaListener(duelArena), this);
		pm.registerEvents(new EntityDamageByEntityListener(this, horses), this);
		pm.registerEvents(new EntityDamageListener(jails), this);
		pm.registerEvents(new EntityChangeBlockListener(this), this);
		pm.registerEvents(new PlayerDeathListener(this, playerLogger), this);
		pm.registerEvents(new DispenseListener(), this);
		pm.registerEvents(new FoodLevelChangeListener(), this);
		pm.registerEvents(new PlayerInteractListener(this, horses, jails, playerLogger), this);
		pm.registerEvents(new PlayerJoinListener(this, timeStorage, dataStorage, vipStorage, mail, jails, playerLogger, tabController), this);
		pm.registerEvents(new PlayerMoveListener(this, extras, afk, playerLogger), this);
		pm.registerEvents(new PlayerPortalListener(), this);
		pm.registerEvents(new PotionListener(), this);
		pm.registerEvents(new ProjectileHitListener(), this);
		pm.registerEvents(new PlayerQuitListener(timeStorage, afk, jails, playerLogger, tabController), this);
		pm.registerEvents(new PlayerTeleportListener(jails, afk, playerLogger), this);
		pm.registerEvents(new PlayerToggleFlightListener(extras), this);
		pm.registerEvents(new VehicleListener(horses), this);
		pm.registerEvents(new PlayerInteractEntityListener(horses, playerLogger), this);
		pm.registerEvents(new PlayerRespawnListener(), this);
		pm.registerEvents(new PlayerChangedWorldListener(lottery, mail, playerLogger), this);
		pm.registerEvents(new PlayerInventoryEvent(lottery, playerLogger), this);
		pm.registerEvents(new AnimalTameListener(horses), this);
	}
	
	
	/*
	 **************************************
	 * Disablers
	 **************************************
	 */	
	
	private void disableSavers() {
		saveworldGuard();
		saveHashSet(tpBlocks, "tpblocks");
		saveHashSet(BlockfaqCommand.chatBotBlocks, "chatbotblocks");
		saveUnfinishedPlots();
		savePlots();
		saveForumVip();
		saveUnfinishedForumVip();
	}
	
	private void disableControllers() {
		jails.shutdown();
		playerLogger.onDisable();
	}

	
	/*
	 **************************************
	 * Save Methods
	 **************************************
	 */	
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void saveHashSet(HashSet hashSet, String configString) {
		List<String> tempList = new ArrayList<String>(hashSet);
		dataConfig.set(configString, null);
		dataConfig.set(configString, tempList);
		dataStorage.saveConfig();
	}
	
	private void savePlots() {
		dataConfig.set("plots", null);
		for (Map.Entry<Integer, String> entry : plots.entrySet()) {
			dataConfig.set("plots." + entry.getKey(), entry.getValue());
		}
		dataStorage.saveConfig();
	}
	
	@SuppressWarnings("rawtypes")
	private void saveworldGuard() {
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
			Set<?> set = regionMap.entrySet();
			Iterator<?> i = set.iterator();
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
	
	private void saveUnfinishedPlots() {
		dataConfig.set("unfinishedplots", unfinishedPlots);
		dataStorage.saveConfig();
	}

	private void saveUnfinishedForumVip() {
		dataConfig.set("unfinishedforumvip", unfinishedForumVip);
		dataStorage.saveConfig();
	}

	private void saveForumVip() {
		dataConfig.set("forumvip", null);
		for (Map.Entry<Integer, String> entry : forumVip.entrySet()) {
			dataConfig.set("forumvip." + entry.getKey(), entry.getValue());
		}
		dataStorage.saveConfig();
	}


	
	/*
	 **************************************
	 * Load Methods
	 **************************************
	 */	
	
	private HashSet<String> loadHashSet(String configString) {
		List<String> tempList = dataConfig.getStringList(configString);
		HashSet<String> hashSet = new HashSet<String>(tempList);
		return hashSet;
	}
	
	private void loadPlots() {
		if (dataConfig.getConfigurationSection("plots") == null)
			return;
		for (String key : dataConfig.getConfigurationSection("plots").getKeys(false)) {
			plots.put(Integer.valueOf(key), dataConfig.getString("plots." + key));
		}
	}

	private void loadworldGuard() {
		FileReader fRead = null;
		try {
			fRead = new FileReader("plugins" + File.separator + "SlapHomebrew" + File.separator + "worldGuard.yml");
			BufferedReader bRead = new BufferedReader(fRead);
			String tempString;
			while ((tempString = bRead.readLine()) != null) {
				String[] tempList = tempString.split(":", 2);
				regionMap.put(tempList[0], tempList[1]);
			}
			bRead.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private void loadUnfinishedPlots() {
		unfinishedPlots = dataConfig.getIntegerList("unfinishedplots");
	}

	private void loadUnfinishedForumVip() {
		unfinishedForumVip = dataConfig.getIntegerList("unfinishedforumvip");
	}

	private void loadForumVip() {
		if (dataConfig.getConfigurationSection("forumvip") == null)
			return;
		for (String key : dataConfig.getConfigurationSection("forumvip").getKeys(false)) {
			forumVip.put(Integer.valueOf(key), dataConfig.getString("forumvip." + key));
		}
	}
	
	
	
	/*
	 **************************************
	 * HashMap & Lists getters
	 **************************************
	 */	
	
	public HashMap<String, Location> getBackDeathMap() {
		return backDeathMap;
	}
	
	public HashMap<String, String> getRegionMap() {
		return regionMap;
	}

	public HashSet<String> getMessages() {
		return messages;
	}
	
	public HashSet<String> getTpBlocks() {
		return tpBlocks;
	}

	public HashMap<Integer, String> getPlots() {
		return plots;
	}
	
	public List<Integer> getUnfinishedPlots() {
		return unfinishedPlots;
	}

	public HashMap<Integer, String> getForumVip() {
		return forumVip;
	}
	
	public List<Integer> getUnfinishedForumVip() {
		return unfinishedForumVip;
	}


	
	/*
	 **************************************
	 * Controller getters
	 **************************************
	 */	

	public Bump getBump() {
		return bump;
	}

	public Sonic getSonic() {
		return sonic;
	}

	public Extras getExtras() {
		return extras;
	}

	public AwayFromKeyboard getAwayFromKeyboard() {
		return afk;
	}
	
	public Lag getLag() {
		return lag;
	}
	
	public Lottery getLottery() {
		return lottery;
	}

	public Horses getHorses() {
		return horses;
	}

	public ChangeLog getChangeLog() {
		return changeLog;
	}

	public Mail getMail() {
		return mail;
	}
	
	public Jails getJails() {
		return jails;
	}
	
	public FireworkShow getFireworkShow() {
		return show;
	}
	
	public PlayerLogger getPlayerLogger() {
		return playerLogger;
	}
	
	public TabController getTabController() {
		return tabController;
	}
	
	public Vip getVip() {
		return vip;
	}
	
	public WorthList getWorthList() {
		return worthList;
	}
	
	
	/*
	 **************************************
	 * YamlStorage getters
	 **************************************
	 */	
	
	public YamlStorage getTimeStorage() {
		return timeStorage;
	}

	public YamlStorage getVipStorage() {
		return vipStorage;
	}

	public YamlStorage getDataStorage() {
		return dataStorage;
	}

	public YamlStorage getSonicStorage() {
		return sonicStorage;
	}

	public YamlStorage getVipGrantStorage() {
		return vipGrantStorage;
	}

	public YamlStorage getApplyThreadStorage() {
		return applyThreadStorage;
	}
	
	
	/*
	 **************************************
	 * External getters
	 **************************************
	 */	
	
	public Economy getEconomy() {
		return economy;
	}
	
	public Essentials getEssentials() {
		return essentials;
	}
	
	public WorldGuardPlugin getworldGuard() {
		return worldGuard;
	}
	
	
	/*
	 **************************************
	 * Others
	 **************************************
	 */	

	public boolean isAllowCakeTp() {
		return allowCakeTp;
	}

	public void setAllowCakeTp(boolean allowCakeTp) {
		this.allowCakeTp = allowCakeTp;
	}

	private void setupChatBot() {
		if (!config.contains("chatmessages.member")) {
			config.set("chatmessages.member", "&c[FAQ] &3Go to &bwww.slap-gaming.com/apply &3to apply for member!");
		}
		if (!config.contains("chatmessages.vip")) {
			config.set("chatmessages.vip", "&c[FAQ] &3Go to www.slap-gaming.com/vip for more information about VIP!");
		}
		if (!config.contains("chatmessages.build")) {
			config.set("chatmessages.build", "&c[FAQ] &3Find an empty plot, then ask a mod/admin for a worldguard! Take the teleport pad at spawn or use the online dynamap (slap-gaming.com/map) to go to empty plots!");
		}
		if (!config.contains("chatmessages.worldguard")) {
			config.set("chatmessages.worldguard", "&c[FAQ] &3Ask a mod/admin for a worldguard! This also protects chests!");
		}
		if (!config.contains("chatmessages.lockette")) {
			config.set("chatmessages.lockette", "&c[FAQ] &3We don't use lockette, worldguard also protects your chests!");
		}
		if (!config.contains("chatmessages.shop")) {
			config.set("chatmessages.shop", "&c[FAQ] &3You need to be a member to use the shop! Right click for an item, shift + right click for a stack of items!");
		}
		if (!config.contains("chatmessages.money")) {
			config.set("chatmessages.money", "&c[FAQ] &3Type /sell hand with an item you want to sell in your hand, or go to www.slap-gaming.com/money to get money!");
		}
		if (!config.contains("chatmessages.checkwg")) {
			config.set("chatmessages.checkwg", "&c[FAQ] &3Right click the ground with string to see zones!");
		}
		if (!config.contains("chatmessages.pay")) {
			config.set("chatmessages.pay", "&c[FAQ] &3Type /money pay [name] [amount]!");
		}
		this.saveConfig();
	}
	
	//TODO Use WGCustomFlags instead of crappy member flags.
}