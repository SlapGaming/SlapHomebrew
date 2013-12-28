package me.naithantu.SlapHomebrew.Commands;

import me.naithantu.SlapHomebrew.SlapHomebrew;
import me.naithantu.SlapHomebrew.Commands.Exception.CommandException;
import me.naithantu.SlapHomebrew.Commands.Exception.ErrorMsg;
import me.naithantu.SlapHomebrew.Util.Util;

import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

public abstract class AbstractCommand {

	abstract public boolean handle() throws CommandException;

	protected CommandSender sender;
	protected String[] args;
	protected SlapHomebrew plugin;

	public AbstractCommand(CommandSender sender, String[] args, SlapHomebrew plugin) {
		this.sender = sender;
		this.args = args;
		this.plugin = plugin;
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
	 * Try to parse a string to integer. The integer must be positive.
	 * @param arg The string that needs to be parsed
	 * @return the int
	 * @throws CommandException if the arg is not a valid int or negative/zero
	 */
	protected int parseIntPositive(String arg) throws CommandException {
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
	protected int parseInt(String arg) throws CommandException {
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
		if (foundPlayer == null) throw new CommandException(ErrorMsg.playerNotOnline); //If no player found throw error
		return foundPlayer;
	}
	
	/**
	 * Get an offline player
	 * @param playername The player's name
	 * @return The offlineplayer
	 * @throws CommandException if offline player has never played on this server before
	 */
	protected OfflinePlayer getOfflinePlayer(String playername) throws CommandException {
		OfflinePlayer offPlayer = plugin.getServer().getOfflinePlayer(playername);
		if (!offPlayer.hasPlayedBefore()) throw new CommandException(ErrorMsg.playerNotFound);
		return offPlayer;
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
	
}
