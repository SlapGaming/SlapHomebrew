package me.naithantu.SlapHomebrew.Util;

import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.reflect.StructureModifier;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import me.naithantu.SlapHomebrew.SlapHomebrew;
import me.naithantu.SlapHomebrew.Commands.Exception.CommandException;
import me.naithantu.SlapHomebrew.Controllers.Flag;
import me.naithantu.SlapHomebrew.PlayerExtension.PlayerControl;
import me.naithantu.SlapHomebrew.PlayerExtension.SlapPlayer;
import me.naithantu.SlapHomebrew.Storage.YamlStorage;
import mkremins.fanciful.FancyMessage;
import net.minecraft.server.v1_8_R1.ChatSerializer;
import net.minecraft.server.v1_8_R1.PacketPlayOutChat;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.craftbukkit.v1_8_R1.entity.CraftPlayer;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.BlockIterator;
import org.bukkit.util.Vector;

import ru.tehkode.permissions.PermissionUser;
import ru.tehkode.permissions.bukkit.PermissionsEx;

import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

public class Util {

    //Pattern for adding time, see {@link Util#parseToTime(String) parseToTime}
    private static Pattern addTimePattern;

    /**
     * Initialize the util
     */
    public static void initialize() {
        addTimePattern = Pattern.compile("[0-9]+[a-z]+", Pattern.CASE_INSENSITIVE);
    }

    /**
     * Destruct the Util
     */
    public static void destruct() {
        addTimePattern = null;
    }


	
	public static String getHeader() {
		return ChatColor.GOLD + "[SLAP] " + ChatColor.WHITE;
	}

	public static void dateIntoTimeConfig(String date, String message, YamlStorage timeStorage) {
		FileConfiguration timeConfig = timeStorage.getConfig();
		int i = 1;
		while (timeConfig.contains(date)) {
			date += " (" + i + ")";
			i++;
		}
		timeConfig.set(date, message);
		timeStorage.saveConfig();
	}

	public static boolean hasFlag(SlapHomebrew plugin, Location location, Flag flag) {
		RegionManager regionManager = plugin.getworldGuard().getRegionManager(location.getWorld());
		ApplicableRegionSet regions = regionManager.getApplicableRegions(location);
		for (ProtectedRegion region : regions) {
			for (String string : region.getMembers().getPlayers()) {
				if (string.startsWith("flag:" + flag.toString().toLowerCase()))
					return true;
			}
		}
		return false;
	}

	public static String getFlag(SlapHomebrew plugin, Location location, Flag flag) {
		RegionManager regionManager = plugin.getworldGuard().getRegionManager(location.getWorld());
		ApplicableRegionSet regions = regionManager.getApplicableRegions(location);
		for (ProtectedRegion region : regions) {
			for (String string : region.getMembers().getPlayers()) {
				if (string.startsWith("flag:" + flag.toString().toLowerCase()))
					return string;
			}
		}
		return null;
	}
	
	public static List<Flag> getFlags(SlapHomebrew plugin, Location location) {
		List<Flag> flags = new ArrayList<Flag>();
		RegionManager regionManager = plugin.getworldGuard().getRegionManager(location.getWorld());
		ApplicableRegionSet regions = regionManager.getApplicableRegions(location);
		for (ProtectedRegion region : regions) {
			for (String string : region.getMembers().getPlayers()) {
				if (string.startsWith("flag:")) {
					String flagWithoutPrefix = string.replaceFirst("flag:", "");
					String flagName = flagWithoutPrefix.split("\\(")[0];
					try {
						Flag flag = Flag.valueOf(flagName.toUpperCase());
						flags.add(flag);
					} catch (IllegalArgumentException e) {
					}
				}
			}
		}
		return flags;
	}


	public static boolean hasEmptyInventory(Player player) {
		Boolean emptyInv = true;
		PlayerInventory inv = player.getInventory();
		for (ItemStack stack : inv.getContents()) {
			//TODO Is try - catch really required here?
			try {
				if (stack.getType() != (Material.AIR)) {
					emptyInv = false;
				}
			} catch (NullPointerException e) {
			}
		}
		for (ItemStack stack : inv.getArmorContents()) {
			try {
				if (stack.getType() != (Material.AIR)) {
					emptyInv = false;
				}
			} catch (NullPointerException e) {
			}
		}
		return emptyInv;
	}

	public static void broadcastToWorld(String worldName, String message) {
		for (Player player : Bukkit.getServer().getOnlinePlayers()) {
			if (player.getWorld().getName().equalsIgnoreCase(worldName)) {
				player.sendMessage(message);
			}
		}
	}
	
	/**
	 * Create a potion effect
	 * 
	 * @param name The name of the potion
	 * @param time The time it lasts in seconds
	 * @param power The power it has
	 * @return The potioneffect or null if invalid name
	 */
	public static PotionEffect getPotionEffect(String name, int time, int power) {
		time = time * 20;
		switch (name.toLowerCase()) {
		case "nightvision": case "night-vision":
			return new PotionEffect(PotionEffectType.NIGHT_VISION, time, power);
		case "blindness": case "blind":
			return new PotionEffect(PotionEffectType.BLINDNESS, time, power);
		case "confusion": case "nausea": case "confus":
			return new PotionEffect(PotionEffectType.CONFUSION, time, power);
		case "jump":
			return new PotionEffect(PotionEffectType.JUMP, time, power);
		case "slowdig": case "slow-digging": case "slowdigging":
			return new PotionEffect(PotionEffectType.SLOW_DIGGING, time, power);
		case "damageresist": case "damage-resist": case "damageresistance": case "damage-resistance":
			return new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, time, power);
		case "fastdig": case "fast-digging": case "fastdigging":
			return new PotionEffect(PotionEffectType.FAST_DIGGING, time, power);
		case "fireresist": case "fire-resist": case "fireresistance":
			return new PotionEffect(PotionEffectType.FIRE_RESISTANCE, time, power);
		case "harm":
			return new PotionEffect(PotionEffectType.HARM, time, power);
		case "heal":
			return new PotionEffect(PotionEffectType.HEAL, time, power);
		case "hunger":
			return new PotionEffect(PotionEffectType.HUNGER, time, power);
		case "strength": case "power":
			return new PotionEffect(PotionEffectType.INCREASE_DAMAGE, time, power);
		case "invisibility": case "invis":
			return new PotionEffect(PotionEffectType.INVISIBILITY, time, power);
		case "poison":
			return new PotionEffect(PotionEffectType.POISON, time, power);
		case "regeneration": case "regen":
			return new PotionEffect(PotionEffectType.REGENERATION, time, power);
		case "slow": case "slowness":
			return new PotionEffect(PotionEffectType.SLOW, time, power);
		case "speed":
			return new PotionEffect(PotionEffectType.SPEED, time, power);
		case "waterbreathing":
			return new PotionEffect(PotionEffectType.WATER_BREATHING, time, power);
		case "weakness": case "weak":
			return new PotionEffect(PotionEffectType.WEAKNESS, time, power);
		default:
			return null;
		}
	}
	
    public static String colorize(String s){
    	if(s == null) return null;
    	return ChatColor.translateAlternateColorCodes('&', s);
    }
    
    public static String decolorize(String s){
    	if(s == null) return null;
    	return s.replaceAll("&([0-9a-f])", "");
    }
    
    public static void msg(CommandSender sender, String msg) {
    	sender.sendMessage(Util.getHeader() + msg);
	}

    public static void badMsg(CommandSender sender, String msg) {
		if (sender instanceof Player) {
			sender.sendMessage(ChatColor.RED + msg);
		} else {
			sender.sendMessage(msg);
		}
	}

    public static void noPermission(CommandSender sender) {
		sender.sendMessage(ChatColor.RED + "You do not have access to that command.");
	}

    /**
     * Test if a CommandSender has a certain permission. Prepend slaphomebrew.[perm]
     * @param sender The sender
     * @param perm The permission prepended with slaphomebrew.
     * @return has permission
     */
    public static boolean testPermission(CommandSender sender, String perm) {
		if (!(sender instanceof Player) || sender.hasPermission("slaphomebrew." + perm)) return true;
		return false;
	}
    
    /**
     * Test if an (offline) player has a certain permission
     * @param UUID The player's UUID
     * @param perm The permission perpended with slaphomebrew.
     * @return has permission
     */
    public static boolean checkPermission(String UUID, String perm) {
    	PermissionUser user = PermissionsEx.getPermissionManager().getUser(UUID);
    	if (user == null) return false;
    	return (user.has("slaphomebrew." + perm));
    }
    
    /**
     * Get the time played string
     * Format: X Days, X Hours, X Minutes and X seconds
     * @param l The time played
     * @return The string or 'Unkown'
     */
    public static String getTimePlayedString(long l) {
    	String returnString = "";
    	int t;
    	l = l / 1000;
    	if (l < 60) {
    		t = 1; //Seconds
    	} else if (l < 3600) {
    		t = 2; //Minutes
    	} else if (l < 86400) {
    		t = 3; //Hours
    	} else {
    		t = 4; //Days
    	}
    	switch (t) {
    	case 4:
    		int days = (int)Math.floor(l / 86400.00);
    		l = l - (days * 86400);
    		returnString = days + (days == 1 ? " day, " : " days, ");
    	case 3:
    		int hours = (int)Math.floor(l / 3600.00);
    		l = l - (hours * 3600);
    		returnString += hours + (hours == 1 ? " hour, " : " hours, ");
    	case 2:
    		int minutes = (int)Math.floor(l / 60.00);
    		l = l - (minutes * 60);
    		returnString += minutes + (minutes == 1 ? " min" : " mins") + " and ";
    	case 1:
    		returnString += l + (l == 1 ? " sec." : " secs");
    		break;
    	default:
    		returnString = "Unknown";
    	}
    	return returnString;
    }
    
	/**
	 * Build a string from a string array.
	 * @param split The String array
	 * @param splitChar The string that should be added between each String from the array
	 * @param begin The index it should start on
	 * @return The combined string
	 */
	public static String buildString(String[] split, String splitChar, int begin) {
		return buildString(split, splitChar, begin, 9001);
	}
    
	/**
	 * Build a string from a string array.
	 * @param split The String array
	 * @param splitChar The string that will be added between each String from the array
	 * @param begin The index it should start on
	 * @param end The index it should end with
	 * @return The combined string
	 */
	public static String buildString(String[] split, String splitChar, int begin, int end) {
		String combined = "";
		while (begin <= end && begin < split.length) {
			if (!combined.isEmpty()) {
				combined += splitChar;
			}
			combined += split[begin];
			begin++;
		}
		return combined;
	}
	
	/**
	 * Build a string from a string collection
	 * @param strings The collection of strings
	 * @param splitChar The string that will be added between each string from the list
	 * @return The combined string
	 */
	public static String buildString(Collection<String> strings, String splitChar) {
		String combined = "";
		for (String s : strings) {
			if (!combined.isEmpty()) {
				combined += splitChar;
			}
			combined += s;
		}
		return combined;
	}

    /**
     * Build a string from a List of strings
     * This one can be used if there last splitChar should be different.
     *
     * Example: buildString(list, ", ", " and ");
     * Will return: "One, Two, Three and Four"
     *
     * @param strings The list of strings
     * @param splitChar The string that splits the strings
     * @param finalSplitChar The string that splits the last strings
     * @return The combined string
     */
    public static String buildString(List<String> strings, String splitChar, String finalSplitChar) {
        String combined = "";
        //Length of the list of strings
        int nrOfStrings = strings.size();
        //Loop through strings
        for (int i = 0; i < nrOfStrings; i++) {
            if (i == (nrOfStrings - 1) && i != 0) { //Last one, add the finalSplitChar
                combined += finalSplitChar;
            } else if (i > 0) { //If not the first nor the last, add the splitChar
                combined += splitChar;
            }
            combined += strings.get(i); //Add the String
        }
        return combined;
    }
    
    /**
     * Get the target block that is NOT air in the line of sight of a player
     * @param entity The entity
     * @param maxDistance Max distance to block
     * @return The block or null
     * @throws CommandException if no block found
     */
    public static Block getTargetBlock(LivingEntity entity, int maxDistance) throws CommandException {
    	BlockIterator iterator = new BlockIterator(entity, maxDistance);
    	while (iterator.hasNext()) {
    		Block foundBlock = iterator.next();
    		if (foundBlock.getType() != Material.AIR) {
    			return foundBlock;
    		}
    	}
    	throw new CommandException("You aren't looking at a block (or out of range)");
    }
    
    /**
     * Broadcast a message to all players. Prepend [SLAP] header.
     * @param message The message
     */
    public static void broadcastHeader(String message) {
    	Bukkit.broadcastMessage(getHeader() + message);
    }
    
    /**
     * Broadcast a message to all players.
     * @param message The message
     */
    public static void broadcast(String message) {
    	Bukkit.broadcastMessage(message);
    }
    
    
    /**
     * Get all blocks in the line of sight of an entity
     * @param entity The entity
     * @param maxDistance The max distance line
     * @return The list with all the blocks
     */
    public static ArrayList<Block> getBlocksInLineOfSight(LivingEntity entity, int maxDistance) {
    	BlockIterator iterator = new BlockIterator(entity, maxDistance);
    	ArrayList<Block> blocks = new ArrayList<>();
    	while (iterator.hasNext()) {
    		blocks.add(iterator.next());
    	}
    	return blocks;
    }
    
    /**
     * Remove all PotionEffects from a player
     * @param p the player
     */
    public static void wipeAllPotionEffects(Player p) {
    	HashSet<PotionEffect> effects = new HashSet<>(p.getActivePotionEffects());
    	for (PotionEffect effect : effects) {
    		p.removePotionEffect(effect.getType());
    	}
    }
    
    /**
     * Send a message to all players with the specified permission
     * @param permission The permission
     * @param message The message
     */
    public static void messagePermissionHolders(String permission, String message) {
    	for (Player p : Bukkit.getOnlinePlayers()) {
    		if (testPermission(p, permission)) {
    			p.sendMessage(message);
    		}
    	}
    }
    
    /**
     * Send a JSON Formatted message to a player
     * @param p The player
     * @param json The JSON message
     */
    public static void sendJsonMessage(Player p, String json) {
    	((CraftPlayer) p)
    		.getHandle()
    		.playerConnection
    		.sendPacket(
    			new PacketPlayOutChat(
    				ChatSerializer.a(json)
    			)
    		);
    }

    /**
     * Send a FancyMessage to a commandsender
     * @param sender The player/commandsender
     * @param fancyMessage The fancymessage
     */
    public static void sendFancyMessage(CommandSender sender, FancyMessage fancyMessage) {
        if (sender instanceof  Player) {
            Player player = (Player) sender;
            sendJsonMessage(player, fancyMessage.toJSONString());
        } else {
            sender.sendMessage(fancyMessage.toOldMessageFormat());
        }
    }
    
    /**
     * Send a JSON Formatted message to all players
     * @param json The JSON message
     */
    public static void broadcastJsonMessage(String json) {
    	for (Player p : getOnlinePlayers()) {
    		sendJsonMessage(p, json);
    	}
    }
        
    /**
     * Get all the online players
     * @return player array
     */
    public static Collection<? extends Player> getOnlinePlayers() {
    	return Bukkit.getServer().getOnlinePlayers();
    }
    
    /**
     * Safely teleport to another player
     * @param toBeTeleported The player who is going to teleport
     * @param toTeleport The locaction to be teleported to
     * @param isFlying if the location is in the (use true if in doubt).
     * @param registerBackLocation will register the location the player is leaving as /back location
     * @param ignoreCooldown Will ignore the cooldown
     * @return has teleported under the target player
     * @throws CommandException if teleport into lava or if target is above the void
     */
    public static boolean safeTeleport(Player toBeTeleported, Location toTeleport, boolean isFlying, boolean registerBackLocation, boolean... ignoreCooldown) throws CommandException {
    	//Get SlapPlayer
    	SlapPlayer sp = PlayerControl.getPlayer(toBeTeleported);
    	
    	if ((ignoreCooldown.length > 0 && !ignoreCooldown[0]) || ignoreCooldown.length == 0) {
	    	if (!sp.getTeleporter().canTeleport()) { //Check if able to teleport if cooldown
	    		if (!Util.testPermission(toBeTeleported, "tp.cooldownoverride")) {
	    			throw new CommandException("You'll need to wait a bit before teleporting agian!");
	    		}
			}
    	}
    	
    	
    	Location teleportTo = null;
		boolean tpUnder = false;
		
		Location fromLocation = toBeTeleported.getLocation();
		
		if (isFlying && !toBeTeleported.isFlying()) { //Target is flying while the player is not flying -> Find first block under target
			tpUnder = true;
			boolean creative = (toBeTeleported.getGameMode() == GameMode.CREATIVE); //Check if in creative
			for (Location loc = toTeleport; loc.getBlockY() > 0; loc.add(0, -1, 0)) { //Loop thru all blocks under target's location
				Material m = loc.getBlock().getType();
				if (m == Material.AIR) continue; //Looking for first solid
				if (m == Material.LAVA && !creative) { //If teleporting into lava && not in creative
					throw new CommandException("You would be teleported into Lava!");
				}
				teleportTo = loc.add(0, 1, 0); //Set loc + 1 block above
				break;
			}
		} else { //Not flying
			teleportTo = toTeleport;
		}
		
		if (teleportTo == null) { //Check if location found
			throw new CommandException("Cannot teleport! Player above void!");
		}
		
		toBeTeleported.teleport(teleportTo); //Teleport
		toBeTeleported.setVelocity(new Vector(0, 0, 0)); //Reset velocity
				
		if (registerBackLocation) { //If registering back location
			sp.getTeleporter().setBackLocation(fromLocation); //Set back location
		}
		
		//Register teleport
		sp.getTeleporter().teleported();
		
		return tpUnder;
    }
    
    /**
     * Set the back location for the player on their current position
     * @param p The player
     */
    public static void setBackLocation(Player p) {
    	SlapPlayer sp = PlayerControl.getPlayer(p);
    	sp.getTeleporter().setBackLocation(p.getLocation());
    }
    
    /**
     * Set the back location for the player on their current position
     * @param p The slapplayer
     */
    public static void setBackLocation(SlapPlayer p) {
    	p.getTeleporter().setBackLocation(p.p().getLocation());
    }
    
    /**
     * Get the OfflinePlayer based on their name
     * @param playername The player's name
     * @return the player
     */
    public static OfflinePlayer getOfflinePlayer(String playername) {
    	return Bukkit.getOfflinePlayer(playername);
    }

    /**
     * Turn a Location into a YML Map
     * @param location The Location
     * @return the YML Map
     */
    public static Map<String, Object> locationToYmlMap(Location location) {
        Map<String, Object> locMap = new HashMap<>();
        locMap.put("world", location.getWorld().getName());
        locMap.put("loc_x", location.getX());
        locMap.put("loc_y", location.getY());
        locMap.put("loc_z", location.getZ());
        locMap.put("pitch", location.getPitch());
        locMap.put("yaw", location.getYaw());
        return locMap;
    }

    /**
     * Create a location from a YML Map.
     * It will return the alt option incase the Location could not be created (invalid world).
     * @param locMap The YML Map
     * @param altLocation The alternative Location
     * @return The Location (or the Alt if failed)
     */
    public static Location ymlMapToLocation(Map<String, Object> locMap, Location altLocation) {
        //Check world
        String worldname = (String) locMap.get("world");
        World world = Bukkit.getWorld(worldname);
        if (world == null) return altLocation;

        //Create location
        double x = (double) locMap.get("loc_x");
        double y = (double) locMap.get("loc_y");
        double z = (double) locMap.get("loc_z");
        float pitch = loadFloatValueFromYmlMap(locMap.get("pitch"));
        float yaw = loadFloatValueFromYmlMap(locMap.get("yaw"));

        //Return location
        return new Location(world, x, y, z, yaw, pitch);
    }

    /**
     * Load a Long value from a YML Map
     * This is necessary as the YML parser tends to read the Long as an Integer.
     * @param longObject The long object from the YML Map
     * @return a long value
     */
    public static long loadLongValueFromYmlMap(Object longObject) {
        long returnLong;
        if (longObject instanceof Integer) {
            returnLong = (Integer) longObject;
        } else {
            returnLong = (Long) longObject;
        }
        return returnLong;
    }

    /**
     * Load a Float vlaue from a YML Map
     * This is necessary as the YML parser tends to read the Float as a Double
     * @param floatObject The float object from the YML Map
     * @return a float value
     */
    public static float loadFloatValueFromYmlMap(Object floatObject) {
        double doubleValue = (double) floatObject;
        float floatValue = (float) doubleValue;
        return floatValue;
    }

    /**
     * Parse a string into time in milliseconds (long).
     *
     * Supported values:
     *  - 'Permanent' or any shortened version (will return -1)
     *  - '[Number][Unit]' multiple times.
     *      This will take the current time and add the given values
     *      Eg: 3d5h
     *
     * @param arg The given argument
     * @return the time or -1 if permanent
     * @throws CommandException if not able to parse the argument
     */
    public static long parseToTime(String arg) throws CommandException {
        switch (arg.toLowerCase()) {
            //All cases for Permanent
            case "infinite":case "inf":
            case "perm":case "permanent":case "permanently":case "perma":
                return -1;

            //Try to parse arg
            default:
                if (!arg.matches("([0-9]+[a-z]+)+")) { //Check if matches
                    throw new CommandException("Invalid format. Should be: [Amount][Unit]");
                }

                //Time in seconds, that needs to be added
                long addTime = 0;

                //=> Match
                Matcher matcher = addTimePattern.matcher(arg);
                while (matcher.find()) {
                    //Get substring (1x pattern)
                    String amountUnit = arg.substring(matcher.start(), matcher.end());

                    int till = 0;
                    char[] chars =  amountUnit.toCharArray();
                    for(int i = 0; i < chars.length; i++) {
                        char f = chars[i];
                        if (!Character.isDigit(f)) {
                            till = i;
                            break;
                        }
                    }

                    //Parse into values
                    long amount = Long.parseLong(amountUnit.substring(0, till));
                    String digits = amountUnit.substring(till, amountUnit.length());
                    switch(digits.toLowerCase()) {
                        case "d":case "day":case "days":
                            amount = amount * 24;
                        case "h":case "hour":case "hours":
                            amount = amount * 60;
                        case "m":case "minute":case"minutes":case "mins":
                            amount = amount * 60;
                        case "s":case "second":case "seconds":case "secs":
                            amount = amount;
                            break;
                        default:
                            throw new CommandException("Unknown unit: " + digits + " (Possible: days/hours/minutes/seconds)");
                    }

                    //=> Add to total
                    addTime += (amount * 1000); //Add amount in millis
                }

                //Return current time + calculated time
                return addTime;
        }
    }

    /**
     * Send a predefined TabHeader package to all players
     */
    public static void sendTabHeader() {
        String header = "{text:\"--- Welcome to SlapGaming ---\",color:gold}";
        String footer = "[{text:\"--- Players \",color:blue},{text:\"X\",color:white},{text:\"/\",color:blue},{text:\"50\",color:white},{text:\" ---\",color:blue}]".replace("X", Util.getOnlinePlayers().size() + "");
        sendTabHeaderPackage(header, footer);
    }

    /**
     * Send all players the TabHeader package with a title and a subtitle
     * @param title The title as JSON
     * @param subtitle The subtitle as JSON
     */
    public static void sendTabHeaderPackage(String title, String subtitle) {
        ProtocolManager manager = ProtocolLibrary.getProtocolManager();
        //Create the packet
        PacketContainer packet = manager.createPacket(PacketType.Play.Server.PLAYER_LIST_HEADER_FOOTER);
        StructureModifier<WrappedChatComponent> comps = packet.getChatComponents();

        //Modify the title & subtitle
        comps.write(0, WrappedChatComponent.fromJson(title));
        comps.write(1, WrappedChatComponent.fromJson(subtitle));

        //Send to players
        for (Player player : getOnlinePlayers()) {
            try {
                manager.sendServerPacket(player, packet);
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        }
    }
    
    public static BukkitTask runASync(Runnable runnable) {
    	SlapHomebrew plugin = SlapHomebrew.getInstance();
    	if (!plugin.isEnabled()) {
    		runnable.run();
    		return null;
    	}
    	return getScheduler(plugin).runTaskAsynchronously(plugin, runnable);
    }
    
    public static BukkitTask runASyncLater(Runnable runnable, int delay) {
    	SlapHomebrew plugin = SlapHomebrew.getInstance();
    	if (!plugin.isEnabled()) {
    		runnable.run();
    		return null;
    	}
    	return getScheduler(plugin).runTaskLaterAsynchronously(plugin, runnable, delay);
    }
    
    public static BukkitTask runASyncTimer(Runnable runnable, int delay, int period) {
    	SlapHomebrew plugin = SlapHomebrew.getInstance();
    	if (!plugin.isEnabled()) {
    		runnable.run();
    		return null;
    	}
    	return getScheduler(plugin).runTaskTimerAsynchronously(plugin, runnable, delay, period);
    }
    
    public static BukkitTask run(Runnable runnable) {
    	SlapHomebrew plugin = SlapHomebrew.getInstance();
    	if (!plugin.isEnabled()) {
    		runnable.run();
    		return null;
    	}
    	return getScheduler(plugin).runTask(plugin, runnable);
    }
    
    public static BukkitTask runLater(Runnable runnable, int delay) {
    	SlapHomebrew plugin = SlapHomebrew.getInstance();
    	if (!plugin.isEnabled()) {
    		runnable.run();
    		return null;
    	}
    	return getScheduler(plugin).runTaskLater(plugin, runnable, delay);
    }
    
    public static BukkitTask runTimer(Runnable runnable, int delay, int period) {
    	SlapHomebrew plugin = SlapHomebrew.getInstance();
    	if (!plugin.isEnabled()) {
    		runnable.run();
    		return null;
    	}
    	return getScheduler(plugin).runTaskTimer(plugin, runnable, delay, period);
    }
    
    public static BukkitScheduler getScheduler(SlapHomebrew plugin) {
    	return plugin.getServer().getScheduler();
    }
    
    public static BukkitScheduler getScheduler() {
    	return SlapHomebrew.getInstance().getServer().getScheduler();
    }
    
    /**
     * Sanitize a string so it is safe to put in a YML file. 
     * Regex: [^a-z0-9]
     * Anything that isn't a-z or 0-9 will be replaced with _
     * The string will also be transformed to lowercase.
     * 
     * @param string The string
     * @return The sanitized string
     */
    public static String sanitizeYamlString(String string) {
    	return string.toLowerCase().replaceAll("[^a-z0-9]", "_");
    }
    
}
