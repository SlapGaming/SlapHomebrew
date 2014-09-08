package me.naithantu.SlapHomebrew.Commands;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import me.naithantu.SlapHomebrew.Commands.Exception.NoMessageException;
import me.naithantu.SlapHomebrew.PlayerExtension.UUIDControl;
import me.naithantu.SlapHomebrew.SlapHomebrew;
import me.naithantu.SlapHomebrew.Commands.Exception.CommandException;
import me.naithantu.SlapHomebrew.Commands.Exception.ErrorMsg;
import me.naithantu.SlapHomebrew.PlayerExtension.PlayerControl;
import me.naithantu.SlapHomebrew.PlayerExtension.SlapPlayer;
import me.naithantu.SlapHomebrew.Util.DateUtil;
import me.naithantu.SlapHomebrew.Util.Helpers.HelpMenu;
import me.naithantu.SlapHomebrew.Util.Util;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

public abstract class AbstractCommand {

	/**
	 * Handle the Command
	 * @return 
	 * @throws CommandException
	 */
	abstract public boolean handle() throws CommandException;
	
	protected CommandSender sender;
	protected String[] args;
	protected SlapHomebrew plugin;

	public AbstractCommand(CommandSender sender, String[] args) {
		this.sender = sender;
		this.args = args;
		this.plugin = SlapHomebrew.getInstance();
	}
	
	/*
	 ***********
	 * Testers *
	 ***********
	 */
	
	/**
	 * Test if the CommandSender of this command has the specified permission
	 * @param perm The permission starting from slaphomebrew.[perm]
	 * @throws CommandException if no permission
	 */
	protected void testPermission(String perm) throws CommandException {
		if (!Util.testPermission(sender, perm)) {
			throw new CommandException(ErrorMsg.noPermission);
		}
	}
	
	/**
	 * Check if the CommandSender is in the correct world.
	 * This will cast the sender to player and can throw that exception.
	 * @param worldname The name of the world the player should be in
	 * @throws CommandException if not a player or if in the wrong world.
	 */
	protected void testWorld(String worldname) throws CommandException {
		if (!getPlayer().getWorld().getName().equalsIgnoreCase(worldname)) {
			throw new CommandException(ErrorMsg.wrongWorld);
		}
	}
	
	/**
	 * Check if the CommandSender is NOT in a certain world
	 * @param worldnames The name of the world
	 * @throws CommandException if not a player or if in the world
	 */
	protected void testNotWorld(String... worldnames) throws CommandException {
		String playerWorld = getPlayer().getWorld().getName();
		for (String worldname : worldnames) {
			if (playerWorld.equalsIgnoreCase(worldname)) {
				throw new CommandException("You cannot do this in this world!");
			}
		}
	}
	
	/**
	 * Try to parse a string to integer. The integer must be positive.
	 * @param arg The string that needs to be parsed
	 * @return the int
	 * @throws CommandException if the arg is not a valid int or negative/zero
	 */
	protected static int parseIntPositive(String arg) throws CommandException {
		int nr = parseInt(arg);
		if (nr <= 0) throw new CommandException(arg + " is not a valid number. It needs to be positive (1+).");
		return nr;
	}
	
	/**
	 * Try to parse a string to integer
	 * @param arg The string that needs to be parsed
	 * @return The int
	 * @throws CommandException if the arg is not a valid int
	 */
	protected static int parseInt(String arg) throws CommandException {
		try {
			return Integer.parseInt(arg);
		} catch (NumberFormatException e) {
			throw new CommandException(arg + " is not a valid number.");
		}
	}
	
	/**
	 * Try to parse a String to EntityType
	 * @param arg The string
	 * @return The EntityType
	 * @throws CommandException if not a valid EntityType
	 */
	protected EntityType parseEntityType(String arg) throws CommandException {
		try {
			return EntityType.valueOf(arg); //Try to get the MobType
		} catch (IllegalArgumentException e) {
			throw new CommandException(arg + " is not a valid EntityType.");
		}
	}
	
	
	
	/*
	 ***********
	 * Getters *
	 ***********
	 */
	
	/**
	 * Cast the CommandSender to player
	 * @return the player
	 * @throws CommandException if the CommandSender is not a player
	 */
	protected Player getPlayer() throws CommandException {
		if (!(sender instanceof Player)) {
			throw new CommandException(ErrorMsg.notAPlayer);
		}
		return (Player) sender;
	}

    /**
     * Get the UUIDProfile of the command sender
     * @return The profile
     */
    protected UUIDControl.UUIDProfile getUUIDProfile() {
        String UUID = (sender instanceof Player ? ((Player) sender).getUniqueId().toString() : "CONSOLE");
        return UUIDControl.getInstance().getUUIDProfile(UUID);
    }
	
	/**
	 * Cast the CommandSender to Player, and get the SlapPlayer instance of that player
	 * @return the SlapPlayer
	 * @throws CommandException if the CommandSender is not a player
	 */
	protected SlapPlayer getSlapPlayer() throws CommandException {
		return PlayerControl.getPlayer(getPlayer());
	}
	
	/**
	 * Get an online player
	 * @param playername The player's name
	 * @param exact The name has to be an exact match
	 * @return The found player
	 * @throws CommandException if player is not online/found
	 */
	protected Player getOnlinePlayer(String playername, boolean exact) throws CommandException {
		Player foundPlayer;
		if (exact) foundPlayer = plugin.getServer().getPlayerExact(playername); //Use exact method
		else foundPlayer = plugin.getServer().getPlayer(playername);
		if (foundPlayer == null) throw new CommandException("There is no player with the name '" + playername + "' online!"); //If no player found throw error
		return foundPlayer;
	}
	
	/**
	 * Get an offline player
     * This will default to only players. CONSOLE is not allowed.
	 * @param playername The player's name
	 * @return The UUIDProfile
	 * @throws CommandException if offline player has never played on this server before
	 */
    protected UUIDControl.UUIDProfile getOfflinePlayer(String playername) throws CommandException {
        return getOfflinePlayer(playername, false);
    }

    /**
     * Get an offline player or Console
     * @param playername The player's name
     * @param allowConsole Allow console to be returned
     * @return The profile
     * @throws CommandException if targeting console while not allowed, or no players with that name. NoMessageException is also possible in case of multiple users.
     */
	protected UUIDControl.UUIDProfile getOfflinePlayer(String playername, boolean allowConsole) throws CommandException {
        //Check if the targeted player is console
        if (!allowConsole) {
            //=> Check if targeting CONSOLE
            if (playername.equalsIgnoreCase("CONSOLE")) {
                throw new CommandException("'CONSOLE' is not a player.");
            }
        }

        //Get UUIDControl
        UUIDControl control = UUIDControl.getInstance();

        //Get UserIDs with this playername
        Collection<Integer> ids = control.getUserIDs(playername);
        if (ids.isEmpty()) {
            //No players with this name
            throw new CommandException("There is no player that has ever used this name.");
        } else {
            //Multiple players with this name. Check if one is currently using it.
            UUIDControl.UUIDProfile profile = null;
            for (int id : ids) {
                UUIDControl.UUIDProfile foundProfile = control.getUUIDProfile(id);
                if (foundProfile.getCurrentName().equalsIgnoreCase(playername)) { //If currently being used profile
                    return foundProfile;
                }
            }
            //No user is currently using the name
            hMsg("No user is currently using that name! Did you mean:");
            for (int id : ids) {
                UUIDControl.UUIDProfile foundProfile = control.getUUIDProfile(id);
                UUIDControl.NameProfile nameProfile = foundProfile.getNames().get(0);
                msg(ChatColor.GOLD + "   ┗▶ " + ChatColor.WHITE + nameProfile.getPlayername() + ChatColor.GRAY + " (since " + DateUtil.format("dd/MM/yyyy", nameProfile.getKnownSince()) + ")");
            }
            //Message is already send, throw NoMessageException
            throw new NoMessageException();
        }
	}


	
	/*
	 ***********
	 * Senders *
	 ***********
	 */
	
	/**
	 * Message the CommandSender of this command
	 * @param msg The message
	 */
	protected void msg(String msg) {
		sender.sendMessage(msg);
	}
	
	/**
	 * Message the CommandSender of this command. Prepend the [SLAP] header.
	 * @param msg The message
	 */
	protected void hMsg(String msg) {
		Util.msg(sender, msg);
	}
	
	
	/*
	 *******************
	 * Command Control *
	 *******************
	 */
	/**
	 * Check if a player is doing a command
	 * @throws CommandException if already doing a command
	 */
	protected void checkDoingCommand() throws CommandException {
		if (sender instanceof Player) {
			if (PlayerControl.getPlayer(sender.getName()).isDoingCommand()) {
				throw new CommandException("You're already executing a command!");
			}
		}
	}
	
	/**
	 * Add a player to the doing command set
	 */
	protected void addDoingCommand() {
		if (sender instanceof Player) {
			PlayerControl.getPlayer(sender.getName()).setDoingCommand(true);
		}
	}
	
	/**
	 * Remove a player from the doing command set
	 */
	protected void removeDoingCommand() {
		if (sender instanceof Player) {
			PlayerControl.getPlayer(sender.getName()).setDoingCommand(false);
		}
	}
	
	/**
	 * Remove a player from the doing command set
	 * @param sender The player
	 */
	public static void removeDoingCommand(CommandSender sender) {
		if (sender instanceof Player) {
			PlayerControl.getPlayer(sender.getName()).setDoingCommand(false);
		}
	}

    /*
	 *************
	 * HelpMenus *
	 *************
	 */
    /**
     * Get this class's HelpMenu.
     * This can only be used if #createHelpMenu has been overriden. Otherwise the result will always be null.
     * @return The HelpMenu or null
     */
    protected HelpMenu getHelpMenu() {
        //Get the HelpMenu
        HelpMenu menu = HelpMenu.getHelpMenu(this.getClass());
        //Check if null
        if (menu == null) {
            //=> Menu is null, try to create it
            menu = createHelpMenu();
            if (menu != null) {
                //=> Create was implemented, store it
                HelpMenu.addHelpMenu(this.getClass(), menu);
            }
        }

        //Return the menu
        return menu;
    }

    /**
     * Create a HelpMenu.
     * This must be overriden by Classes that want to make use of this function
     * @return The created HelpMenu
     */
    protected HelpMenu createHelpMenu() {
        return null;
    }

	
	/*
	 ****************
	 * Tab Complete *
	 **************** 
	 */
	/**
	 * Creates a list of all players that can be auto tab completed
	 * @param exclude Possible players that should be excluded
	 * @return the list of names
	 */
	public static List<String> listAllPlayers(String... exclude) {
		List<String> list = new ArrayList<>();
		for (Player player : Util.getOnlinePlayers()) {
			String p = player.getName();
			boolean skip = false;
			for (String ex : exclude) {
				if (p.equalsIgnoreCase(ex)) {
					skip = true;
					break;
				}
			}
			if (!skip) list.add(p);
		}
		return list;
	}
	
	/**
	 * Create a new empty List<String>
	 * @return empty string list
	 */
	public static List<String> createEmptyList() {
		return new ArrayList<String>();
	}
	
	/**
	 * Create a new list with the given options
	 * @param options optional options
	 * @return list with options
	 */
	public static List<String> createNewList(String... options) {
		List<String> list = createEmptyList();
		for (String option : options) {
			list.add(option);
		}
		return list;
	}
	
	/**
	 * Filter all strings that start with the given string 
	 * @param list The list
	 * @param startWith start with
	 * @return the same list (filtered)
	 */
	public static List<String> filterResults(List<String> list, String startWith) {
		if (startWith.equals("")) return list;
		
		startWith = startWith.toLowerCase(); //To lowercase
		
		for (int x = list.size() - 1; x >= 0; x--) { //Loop thru results, start at last entry
			String s = list.get(x);
			if (!(s.toLowerCase().startsWith(startWith))) { //If not start with given string
				list.remove(x); //Remove from list
			}
		}
		return list;
	}
	
	/**
	 * Add strings to a string list
	 * @param list The list
	 * @param toBeAdded Strings that need to be added
	 */
	public static void addToList(List<String> list, String...toBeAdded) {
		for (String s : toBeAdded) {
			list.add(s);
		}
	}
	
	
}
