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
import java.util.logging.Logger;

import me.naithantu.SlapHomebrew.Commands.BlockfaqCommand;
import me.naithantu.SlapHomebrew.Commands.CommandHandler;
import me.naithantu.SlapHomebrew.Listeners.*;
import me.naithantu.SlapHomebrew.Storage.YamlStorage;
import net.milkbowl.vault.Vault;
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

	public final Logger logger = Logger.getLogger("Minecraft");

	public static HashMap<String, Location> backDeath = new HashMap<String, Location>();
	public static HashMap<String, String> worldGuard = new HashMap<String, String>();
	HashMap<Integer, String> plots = new HashMap<Integer, String>();
	List<Integer> unfinishedPlots = new ArrayList<Integer>();
	HashMap<Integer, String> forumVip = new HashMap<Integer, String>();
	List<Integer> unfinishedForumVip = new ArrayList<Integer>();

	private YamlStorage dataStorage;
	private YamlStorage vipStorage;
	private YamlStorage timeStorage;
	private YamlStorage sonicStorage;
	private YamlStorage vipGrantStorage;
	private YamlStorage applyThreadStorage;

	private FileConfiguration dataConfig;
	private FileConfiguration vipConfig;

	Vip vip;

	public static HashSet<String> message = new HashSet<String>();
	public static HashSet<String> tpBlocks = new HashSet<String>();

	Bump bump;
	Sonic sonic;
	Extras extras;
	Lottery lottery;
	ApplyChecker applyChecker;
	AwayFromKeyboard afk;
	Horses horses;
	ChangeLog changeLog;
	
	Essentials essentials;
	
	public static boolean allowCakeTp;

	Configuration config;


	PluginManager pm;

	public static Economy econ = null;
	public static Vault vault = null;

	CommandHandler commandHandler;

	@Override
	public void onEnable() {
		config = getConfig();
		dataStorage = new YamlStorage(this, "data");
		vipStorage = new YamlStorage(this, "vip");
		timeStorage = new YamlStorage(this, "time");
		sonicStorage = new YamlStorage(this, "sonic");
		vipGrantStorage = new YamlStorage(this, "vipgrant");
		applyThreadStorage = new YamlStorage(this, "ApplyThreads");
		dataConfig = dataStorage.getConfig();
		vipConfig = vipStorage.getConfig();
		vip = new Vip(vipStorage);
		sonic = new Sonic(this);
		bump = new Bump(this, dataStorage, dataConfig);
		extras = new Extras(this);
		tpBlocks = loadHashSet("tpblocks");
		lottery = new Lottery(this);
		applyChecker = new ApplyChecker(this);
		afk = new AwayFromKeyboard(this);
		horses = new Horses(this);
		changeLog = new ChangeLog();
		BlockfaqCommand.chatBotBlocks = loadHashSet("chatbotblocks");
		setupEconomy();
		setupChatBot();
		loadworldGuard();
		loadPlots();
		loadUnfinishedForumVip();
		loadForumVip();
		loadUnfinishedPlots();
		new Schedulers(this);
		commandHandler = new CommandHandler(this, lottery);
		pm = getServer().getPluginManager();
		pm.registerEvents(new BlockPlaceListener(this), this);
		pm.registerEvents(new PlayerChatListener(this, afk), this);
		pm.registerEvents(new PlayerCommandListener(this, afk), this);
		pm.registerEvents(new CreatureSpawnListener(this), this);
		pm.registerEvents(new CreatureDeathListener(this, horses), this);
		pm.registerEvents(new EntityDamageByEntityListener(this, horses), this);
		pm.registerEvents(new EntityDamageListener(), this);
		pm.registerEvents(new PlayerDeathListener(this), this);
		pm.registerEvents(new DispenseListener(), this);
		pm.registerEvents(new FoodLevelChangeListener(), this);
		pm.registerEvents(new PlayerInteractListener(this, horses), this);
		pm.registerEvents(new PlayerLoginListener(this, timeStorage, dataStorage, vipStorage), this);
		pm.registerEvents(new PlayerMoveListener(this, extras, afk), this);
		pm.registerEvents(new PlayerPortalListener(), this);
		pm.registerEvents(new PotionListener(), this);
		pm.registerEvents(new ProjectileHitListener(), this);
		pm.registerEvents(new PlayerQuitListener(this, timeStorage, afk), this);
		pm.registerEvents(new PlayerTeleportListener(), this);
		pm.registerEvents(new PlayerToggleFlightListener(extras), this);
		pm.registerEvents(new VehicleListener(horses), this);
		pm.registerEvents(new PlayerInteractEntityListener(horses), this);
		pm.registerEvents(new PlayerRespawnListener(), this);
		pm.registerEvents(new PlayerChangedWorldListener(lottery), this);
		pm.registerEvents(new PlayerInventoryEvent(lottery), this);
		pm.registerEvents(new AnimalTameListener(horses), this);

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
		
		essentials = (Essentials) this.getServer().getPluginManager().getPlugin("Essentials");
		saveConfig();
	}

	@Override
	public void onDisable() {
		getServer().getScheduler().cancelTasks(this);
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
	
	public Essentials getEssentials(){
		return essentials;
	}

	//TODO Use WGCustomFlags instead of crappy member flags.
	
/*	private WGCustomFlagsPlugin getWGCustomFlags() {
		Plugin plugin = getServer().getPluginManager().getPlugin("WGCustomFlags");

		if (plugin == null || !(plugin instanceof WGCustomFlagsPlugin)) {
			return null;
		}

		return (WGCustomFlagsPlugin) plugin;
	}*/

	public Vip getVip() {
		return vip;
	}

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
	
	public YamlStorage getApplyThreadStorage(){
		return applyThreadStorage;
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

	public HashMap<Integer, String> getForumVip() {
		return forumVip;
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
	
	public Sonic getSonic(){
		return sonic;
	}
	
	public Extras getExtras(){
		return extras;
	}
	
	public AwayFromKeyboard getAwayFromKeyboard(){
		return afk;
	}
	
	public Horses getHorses(){
		return horses;
	}
	
	public ChangeLog getChangeLog(){
		return changeLog;
	}

	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
		return commandHandler.handle(sender, cmd, args);
	}
}