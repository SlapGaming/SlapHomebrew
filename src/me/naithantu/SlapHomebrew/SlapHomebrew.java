package me.naithantu.SlapHomebrew;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import me.naithantu.SlapHomebrew.Commands.CommandHandler;
import me.naithantu.SlapHomebrew.Commands.Basics.SpawnCommand;
import me.naithantu.SlapHomebrew.Controllers.*;
import me.naithantu.SlapHomebrew.Listeners.*;
import me.naithantu.SlapHomebrew.Listeners.Entity.*;
import me.naithantu.SlapHomebrew.Listeners.Player.*;
import me.naithantu.SlapHomebrew.Storage.YamlStorage;
import me.naithantu.SlapHomebrew.Timing.HandlerControl;
import me.naithantu.SlapHomebrew.Util.Log;
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
	 * The SlapHomebrew instance
	 */
	private static SlapHomebrew instance;

	/**
	 * BackDeath HashMap - Key Playername -> Value DeathLocation
	 */
	private HashMap<String, Location> backDeathMap = new HashMap<String, Location>();
			
	/**
	 * forumVip HashMap - Key VIPForumID -> Value Info
	 * unfinishedForumVip - List of unfinished VIPForumIDs
	 */
	private HashMap<Integer, String> forumVip = new HashMap<Integer, String>();
	private List<Integer> unfinishedForumVip = new ArrayList<Integer>();

	/**
	 * tpBlocks HashSet - Contains all tpBlocks
	 */
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
    private YamlStorage messageStorage;
	private FileConfiguration dataConfig;
	private FileConfiguration vipConfig;
	private Configuration config;

	/**
	 * Controllers
	 */
	private ArrayList<AbstractController> controllers;
	private AwayFromKeyboard afk;
	private Bump bump;
	private ChangeLog changeLog;
	private ChatChannels chatChannels;
	private DuelArena duelArena;
	private Extras extras;
	private FireworkShow show;
	private Horses horses;
	private Jails jails;
	private Lag lag;
	private Lottery lottery;
	private Mail mail;
	private Mention mention;
    private Messages messages;
	private PlayerLogger playerLogger;
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
		initializeStatics();
		initializeExternals();
		initializeYamlStoragesConfigs();
		initializeLoaders();
		initializeControllers();
		initializeListeners();
		
		//Create Schedulers -> Runnables & Make the commandHandler
		new Schedulers();
		commandHandler = new CommandHandler();

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
		disableStatics();
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
	
	private void initializeStatics() {
		instance = this;
		Log.intialize(getLogger());
	}
	
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
        messageStorage = new YamlStorage(this, "messages");
		dataConfig = dataStorage.getConfig();
		vipConfig = vipStorage.getConfig();
	}
	
	private void initializeControllers() {
		 controllers = new ArrayList<>();
		 controllers.add(bump = new Bump(dataStorage, dataConfig));
		 controllers.add(changeLog = new ChangeLog());
		 controllers.add(chatChannels = new ChatChannels());
		 controllers.add(duelArena = new DuelArena());
		 controllers.add(extras = new Extras());
		 controllers.add(show = new FireworkShow());
		 controllers.add(horses = new Horses());
		 controllers.add(jails = new Jails());
		 controllers.add(lag = new Lag());
		 controllers.add(lottery = new Lottery());
		 controllers.add(mail = new Mail());
		 controllers.add(mention = new Mention());
		 controllers.add(messages = new Messages());
		 controllers.add(playerLogger = new PlayerLogger());
		 controllers.add(afk = new AwayFromKeyboard());
		 controllers.add(tabController = new TabController(playerLogger));
		 controllers.add(vip = new Vip(vipStorage, tabController));
		 controllers.add(worthList = new WorthList());
		 
		 controllers.add(new ApplyChecker(essentials, tabController));
		 controllers.add(new SlapSecurityAgency());
		 
		 //Temporary EventTimer
		 new HandlerControl();
	}
	
	private void initializeLoaders() {
		tpBlocks = loadHashSet("tpblocks");
		loadUnfinishedForumVip();
		loadForumVip();
	}
	
	/**
	 * Initialize the listeners
	 */
	private void initializeListeners() {
		PluginManager pm = getServer().getPluginManager();
		register(pm, new BlockPlaceListener());
		register(pm, new PlayerChatListener(afk, jails, playerLogger, chatChannels, mention));
		register(pm, new PlayerTabCompleteListener());
		register(pm, new PlayerCommandListener(afk, jails, playerLogger));
		register(pm, new CreatureSpawnListener());
		register(pm, new CreatureDeathListener(horses));
		register(pm, new DuelArenaListener(duelArena));
		register(pm, new EntityDamageByEntityListener(horses));
		register(pm, new EntityDamageListener(jails));
		register(pm, new EntityChangeBlockListener());
		register(pm, new PlayerDeathListener(playerLogger));
		register(pm, new DispenseListener());
		register(pm, new PlayerInteractListener(horses, jails, playerLogger));
		register(pm, new PlayerJoinListener(dataStorage, vipStorage, mail, jails, playerLogger, tabController));
		register(pm, new PlayerMoveListener(extras, afk, playerLogger));
		register(pm, new PlayerPortalListener());
		register(pm, new PotionListener());
		register(pm, new ProjectileHitListener());
		register(pm, new ProjectileLaunchListener());
		register(pm, new PlayerQuitListener(afk, jails, playerLogger, tabController, chatChannels));
		register(pm, new PlayerTeleportListener(jails, afk, playerLogger));
		register(pm, new PlayerToggleFlightListener(extras));
		register(pm, new VehicleListener(horses));
		register(pm, new PlayerInteractEntityListener(horses, playerLogger));
		register(pm, new PlayerRespawnListener());
		register(pm, new PlayerChangedWorldListener(lottery, mail));
		register(pm, new PlayerInventoryEvent(lottery, playerLogger));
		register(pm, new AnimalTameListener(horses));
	}
	
	/**
	 * Register a Abstract EventListener
	 * @param pm The Pluginmanager
	 * @param listener The listener
	 */
	private void register(PluginManager pm, AbstractListener listener) {
		pm.registerEvents(listener, this);
	}
	
	
	/*
	 **************************************
	 * Disablers
	 **************************************
	 */	
	
	private void disableStatics() {
		instance = null;
		Log.shutdown();
	}
	
	private void disableSavers() {
		saveHashSet(tpBlocks, "tpblocks");
		saveForumVip();
		saveUnfinishedForumVip();
	}
	
	private void disableControllers() {
		for (AbstractController controller : controllers) {
			controller.shutdown();
		}
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

	public HashSet<String> getTpBlocks() {
		return tpBlocks;
	}

	public HashMap<Integer, String> getForumVip() {
		return forumVip;
	}
	
	public List<Integer> getUnfinishedForumVip() {
		return unfinishedForumVip;
	}

	/*
	 **************************************
	 * SlapHomebrew getter
	 **************************************
	 */	
	/**
	 * Get the instance of this plugin
	 * @return
	 */
	public static SlapHomebrew getInstance() {
		return instance;
	}
	
	
	/*
	 **************************************
	 * Controller getters
	 **************************************
	 */	

	public Bump getBump() {
		return bump;
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
	
	public ChatChannels getChatChannels() {
		return chatChannels;
	}

	public Mail getMail() {
		return mail;
	}
	
	public Mention getMention() {
		return mention;
	}

    public Messages getMessages() {
        return messages;
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

    public YamlStorage getMessageStorage() {
        return messageStorage;
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
	//TODO Use WGCustomFlags instead of crappy member flags.
}