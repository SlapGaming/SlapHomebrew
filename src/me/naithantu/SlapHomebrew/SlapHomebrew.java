package me.naithantu.SlapHomebrew;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import me.naithantu.SlapHomebrew.Commands.CommandHandler;
import me.naithantu.SlapHomebrew.Commands.Basics.SpawnCommand;
import me.naithantu.SlapHomebrew.Controllers.*;
import me.naithantu.SlapHomebrew.Controllers.FancyMessage.FancyMessageControl;
import me.naithantu.SlapHomebrew.Listeners.*;
import me.naithantu.SlapHomebrew.Listeners.Entity.*;
import me.naithantu.SlapHomebrew.Listeners.Player.*;
import me.naithantu.SlapHomebrew.Storage.YamlStorage;
import me.naithantu.SlapHomebrew.Timing.HandlerControl;
import me.naithantu.SlapHomebrew.Util.Log;
import me.naithantu.SlapHomebrew.Util.SQLPool;
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
	 * tpBlocks HashSet - Contains all tpBlocks
	 */
	private HashSet<String> tpBlocks = new HashSet<String>();
	
	/**
	 * YamlStorage files
	 * FileConfigs
	 */
	private YamlStorage dataStorage;
	private YamlStorage vipGrantStorage;
	private YamlStorage applyThreadStorage;
    private YamlStorage messageStorage;
	private FileConfiguration dataConfig;
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
	private FancyMessageControl fancyMessage;
	private FireworkShow show;
	private Homes homes;
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
	private Whitelist whitelist;
	private WorthList worthList;

	/**
	 * External
	 */
	private Essentials essentials;
	private Economy economy;
	private WorldGuardPlugin worldGuard;
	
	/**
	 * SQL Pool
	 */
	private SQLPool pool;
	
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
		pool.shutdown();
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
		pool = new SQLPool(); //Create SQL Pool
	}

	private void initializeYamlStoragesConfigs() {
		config = getConfig();
		dataStorage = new YamlStorage(this, "data");
		vipGrantStorage = new YamlStorage(this, "vipgrant");
		applyThreadStorage = new YamlStorage(this, "ApplyThreads");
        messageStorage = new YamlStorage(this, "messages");
		dataConfig = dataStorage.getConfig();
	}
	
	private void initializeControllers() {
		 controllers = new ArrayList<>();
		 controllers.add(bump = new Bump(dataStorage, dataConfig));
		 controllers.add(changeLog = new ChangeLog());
		 controllers.add(chatChannels = new ChatChannels());
		 controllers.add(duelArena = new DuelArena());
		 controllers.add(extras = new Extras());
		 controllers.add(fancyMessage = new FancyMessageControl());
		 controllers.add(show = new FireworkShow());
		 controllers.add(homes = new Homes());
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
		 controllers.add(vip = new Vip(tabController));
		 controllers.add(whitelist = new Whitelist());
		 controllers.add(worthList = new WorthList());
		 
		 controllers.add(new ApplyChecker(essentials, tabController));
		 controllers.add(new SlapSecurityAgency());
		 
		 //Temporary EventTimer
		 new HandlerControl();
	}
	
	private void initializeLoaders() {
		tpBlocks = loadHashSet("tpblocks");
	}
	
	/**
	 * Initialize the listeners
	 */
	private void initializeListeners() {
		register(				
				//Listeners
				new AnimalTameListener(horses),
				new BlockPlaceListener(),
				new CreatureDeathListener(horses),
				new CreatureSpawnListener(),
				new DispenseListener(),
				new DuelArenaListener(duelArena),
				new EntityChangeBlockListener(),
				new EntityDamageByEntityListener(horses),
				new EntityDamageListener(jails),
				new PlayerChangedWorldListener(lottery, mail),
				new PlayerChatListener(afk, jails, playerLogger, chatChannels, mention),
				new PlayerCommandListener(afk, jails, playerLogger),
				new PlayerDeathListener(playerLogger),
				new PlayerInteractEntityListener(horses, playerLogger),
				new PlayerInteractListener(horses, jails, playerLogger),
				new PlayerInventoryEvent(lottery, playerLogger),
				new PlayerJoinListener(mail, jails, playerLogger, tabController, homes),
				new PlayerLoginListener(whitelist),
				new PlayerMoveListener(extras, afk, playerLogger),
				new PlayerPortalListener(),
				new PlayerQuitListener(afk, jails, playerLogger, tabController, chatChannels, homes),
				new PlayerRespawnListener(),
				new PlayerTabCompleteListener(),
				new PlayerTeleportListener(jails, afk, playerLogger),
				new PlayerToggleFlightListener(extras),
				new PotionListener(),
				new ProjectileHitListener(),
				new ProjectileLaunchListener(),
				new VehicleListener(horses)
		);
	}
	
	/**
	 * Register a Abstract EventListener
	 * @param listeners The listeners
	 */
	private void register(AbstractListener... listeners) {
		PluginManager pm = getServer().getPluginManager();
		for (AbstractListener listener : listeners) {
			pm.registerEvents(listener, this);
		}
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
	
	public FancyMessageControl getFancyMessage() {
		return fancyMessage;
	}
	
	public Homes getHomes() {
		return homes;
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
	
	public Whitelist getWhitelist() {
		return whitelist;
	}
	
	public WorthList getWorthList() {
		return worthList;
	}
	
	
	/*
	 **************************************
	 * YamlStorage getters
	 **************************************
	 */	

	public YamlStorage getDataStorage() {
		return dataStorage;
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