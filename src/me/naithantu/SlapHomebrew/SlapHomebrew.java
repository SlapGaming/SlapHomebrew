package me.naithantu.SlapHomebrew;

import com.earth2me.essentials.Essentials;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import de.diddiz.LogBlock.LogBlock;
import me.naithantu.SlapHomebrew.Commands.Basics.SpawnCommand;
import me.naithantu.SlapHomebrew.Commands.CommandHandler;
import me.naithantu.SlapHomebrew.Commands.TabHandler;
import me.naithantu.SlapHomebrew.Controllers.*;
import me.naithantu.SlapHomebrew.Controllers.FancyMessage.FancyMessageControl;
import me.naithantu.SlapHomebrew.Listeners.*;
import me.naithantu.SlapHomebrew.Listeners.Entity.*;
import me.naithantu.SlapHomebrew.Listeners.Player.*;
import me.naithantu.SlapHomebrew.Listeners.World.ChunkLoadListener;
import me.naithantu.SlapHomebrew.Listeners.World.ChunkUnloadListener;
import me.naithantu.SlapHomebrew.PlayerExtension.PlayerControl;
import me.naithantu.SlapHomebrew.Storage.HorseSerializables.MutatedHorsesCollection;
import me.naithantu.SlapHomebrew.Storage.HorseSerializables.SavedHorse;
import me.naithantu.SlapHomebrew.Storage.JailSerializables.Jail;
import me.naithantu.SlapHomebrew.Storage.JailSerializables.JailTime;
import me.naithantu.SlapHomebrew.Storage.YamlStorage;
import me.naithantu.SlapHomebrew.Timing.HandlerControl;
import me.naithantu.SlapHomebrew.Util.Helpers.HelpMenu;
import me.naithantu.SlapHomebrew.Util.Log;
import me.naithantu.SlapHomebrew.Util.Util;
import net.milkbowl.vault.economy.Economy;
import nl.stoux.SlapPlayers.SlapPlayers;
import nl.stoux.SlapPlayers.Util.DateUtil;
import nl.stoux.SlapPlayers.Util.SQLPool;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

public class SlapHomebrew extends JavaPlugin {
	
	/**
	 * The SlapHomebrew instance
	 */
	private static SlapHomebrew instance;
			
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
    private MuteController muteController;
    private PlayerControl playerControl;
	private PlayerLogger playerLogger;
    private Profiler profiler;
	private SpartaPads spartaPads;
	private TabController tabController;
	private ToldStatus toldStatus;
	private Vip vip;
	private Whitelist whitelist;
	private WorthList worthList;

	/**
	 * External
	 */
	private Essentials essentials;
	private Economy economy;
	private WorldGuardPlugin worldGuard;
	private LogBlock logBlock;
	
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
    public void onLoad() {
        super.onLoad();
        //Register ConfigSerializations
        ConfigurationSerialization.registerClass(Jail.class);
        ConfigurationSerialization.registerClass(JailTime.class);
        ConfigurationSerialization.registerClass(SavedHorse.class);
        ConfigurationSerialization.registerClass(MutatedHorsesCollection.class);
    }

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
		
		//Essentials reflection code is a bit broken. TabComplete handling of some essential commands still go to Essentials
		//Instead of SlapHomebrew. Migrates the handler from Essentials to Slaphomebrew
		Util.runLater(new Runnable() {
			@Override
			public void run() {
				TabHandler.migrateEssentialTabCommands(instance);
			}
		}, 10);
		
	}

	@Override
	public void onDisable() {
		getServer().getScheduler().cancelTasks(this);
		disableSavers();
		disableListeners();
		disableControllers();
		disableStatics();
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
		long s = System.currentTimeMillis(); //Start timing
		boolean handled = commandHandler.handle(sender, cmd, args); //Handle command
		long took = System.currentTimeMillis() - s; //End timing
		if (took > 50) { //If took longer than 1 tick, warn
			Log.warn("Tick disrupted! Command took: " + took + "ms. Cmd: " + cmd.getName() + " | Args: " + Arrays.toString(args));
		}
		return handled;
	}
	
	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
		long s = System.currentTimeMillis(); //Start timing
		List<String> handled = TabHandler.handle(sender, command, args); //Handle TabComplete
		long took = System.currentTimeMillis() - s; //End timing
		if (took > 50) { //If took longer than 1 tick, warn
			Log.warn("Tick disrupted! CommandTabComplete took: " + took + "ms. Cmd: " + command.getName() + " | Args: " + Arrays.toString(args));
		}
		if (handled == null) {
			return super.onTabComplete(sender, command, alias, args);
		} else {
			return handled;
		}
	}
	
	
	/*
	 **************************************
	 * Initializers
	 **************************************
	 */	
	
	private void initializeStatics() {
		instance = this;
		Log.intialize(getLogger());
        Util.initialize();
		DateUtil.initialize();
        HelpMenu.initialize();
	}
	
	private void initializeExternals() {
		PluginManager pm = getServer().getPluginManager();
		
		//Get vault
		Plugin vault = pm.getPlugin("Vault");
		if (vault == null) {
			getLogger().warning(String.format("[%s] Vault was _NOT_ found! Disabling plugin.", getDescription().getName()));
			getPluginLoader().disablePlugin(this);
			return;
		}
		
		//Get essentials
		essentials = (Essentials) getServer().getPluginManager().getPlugin("Essentials");
		
		//Get worldguard
		worldGuard = (WorldGuardPlugin) getServer().getPluginManager().getPlugin("WorldGuard");
		
		//Get EconomyProvider
		RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
		if (rsp != null) {
			economy = rsp.getProvider();
		}
		
		//Get logblock
		Plugin logBlockPlugin = getServer().getPluginManager().getPlugin("LogBlock");
		if (logBlockPlugin != null && logBlockPlugin.isEnabled() && logBlockPlugin instanceof LogBlock) {
			logBlock = (LogBlock) logBlockPlugin;
		}

        //Get SlapPlayers
        Plugin slapPlayersPlugin = getServer().getPluginManager().getPlugin("SlapPlayers");
        if (slapPlayersPlugin == null || !slapPlayersPlugin.isEnabled() && !(slapPlayersPlugin instanceof SlapPlayers)) {
            throw new RuntimeException("Failed to enable: Missing SlapPlayers.");
        }

		//Create SQL Pool
        pool = SlapPlayers.getSQLPool();
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
		playerControl = new PlayerControl(); //Initialize PlayerControl
		
		//Initialize other controllers
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
         controllers.add(muteController = new MuteController());
		 controllers.add(playerLogger = new PlayerLogger());
         controllers.add(profiler = new Profiler());
		 controllers.add(afk = new AwayFromKeyboard());
		 controllers.add(spartaPads = new SpartaPads());
		 controllers.add(tabController = new TabController());
		 controllers.add(toldStatus = new ToldStatus());
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
                new ChunkLoadListener(horses),
                new ChunkUnloadListener(horses),
				new DispenseListener(),
				new DuelArenaListener(duelArena),
				new EntityChangeBlockListener(),
				new EntityDamageByEntityListener(horses),
				new EntityDamageListener(jails),
				new PlayerChangedWorldListener(lottery, mail),
				new PlayerChatListener(afk, jails, chatChannels, mention, muteController),
				new PlayerCommandListener(afk, jails, playerLogger, muteController),
				new PlayerDeathListener(playerLogger),
				new PlayerInteractEntityListener(horses),
				new PlayerInteractListener(jails, spartaPads),
				new PlayerInventoryEvent(lottery),
				new PlayerJoinListener(mail, jails, tabController, homes),
				new PlayerLoginListener(whitelist),
				new PlayerMoveListener(extras, afk),
				new PlayerPortalListener(),
				new PlayerQuitListener(afk, jails, tabController, chatChannels, homes),
				new PlayerRespawnListener(),
				new PlayerTabCompleteListener(),
				new PlayerTeleportListener(jails, afk),
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
		DateUtil.destruct();
        Util.destruct();
        Log.shutdown();
        HelpMenu.shutdown();
	}
	
	private void disableSavers() {
		saveHashSet(tpBlocks, "tpblocks");
	}
	
	private void disableListeners() {
		//Disable the chat listeners | Currently the only one.
		//TODO change this system to automaticly disable all listeners. No matter if they have anything to disable (default behaviour)
		PlayerChatListener.getInstance().disable();
	}
	
	private void disableControllers() {
		//Shutdown main controllers
		for (AbstractController controller : controllers) {
			controller.shutdown();
		}
		
		//Shutdown player control
		playerControl.shutdown(); 
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

    public MuteController getMuteController() {
        return muteController;
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

    public Profiler getProfiler() {
        return profiler;
    }

	public SpartaPads getSpartaPads() {
		return spartaPads;
	}
	
	public TabController getTabController() {
		return tabController;
	}
	
	public ToldStatus getToldStatus() {
		return toldStatus;
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
	
	public LogBlock getLogBlock() {
		return logBlock;
	}

    public SQLPool getSQLPool() {
        return pool;
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