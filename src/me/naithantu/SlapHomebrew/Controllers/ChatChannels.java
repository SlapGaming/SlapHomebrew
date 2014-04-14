package me.naithantu.SlapHomebrew.Controllers;

import java.util.ArrayList;
import java.util.HashMap;

import me.naithantu.SlapHomebrew.Commands.Exception.CommandException;
import me.naithantu.SlapHomebrew.Commands.Exception.ErrorMsg;
import me.naithantu.SlapHomebrew.Commands.Exception.UsageException;
import me.naithantu.SlapHomebrew.Util.Util;
import nl.stoux.slapbridged.bukkit.SlapBridged;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ChatChannels extends AbstractController {
	
	private HashMap<String, Channel> channels; //Perm -> Channel
	private HashMap<String, Channel> playersInChannels; //Player -> Channel
	
	public ChatChannels() {
		//Create maps
		channels = new HashMap<>();
		playersInChannels = new HashMap<>();
		
		//Add Channels
		createChannel("guidechat", ChatColor.GOLD + "=> [GuideChat]");
		createChannel("modchat",  ChatColor.AQUA + "=> [ModChat]");
		createChannel("adminchat", ChatColor.RED + "=> [AdminChat]");
		createChannel("potatochat", ChatColor.DARK_AQUA + "=> [PotatoChat]");		
	}
	
	/**
	 * Send a message to all players in a channel
	 * @param sender The sender
	 * @param permission The channel permission
	 * @param args The message
	 * @throws CommandException if channel not available, or no permission
	 */
	public void sendToChannel(CommandSender sender, String permission, String[] args) throws CommandException {
		Channel channel = channels.get(permission);
		if (channel == null) throw new CommandException("This channel is not available, or doesn't exist."); //Check if active
		channel.sendChat(sender, args);
	}
		
	/**
	 * Check if a player is in a chat channel
	 * @param playername The player
	 * @return in a channel
	 */
	public boolean isPlayerInChannel(String playername) {
		return playersInChannels.containsKey(playername);
	}
	
	/**
	 * Check if a player is in a certain channel
	 * @param playername The player
	 * @param channel The channel
	 * @return is in that channel
	 */
	public boolean isPlayerInChannel(String playername, String channel) {
		Channel c = playersInChannels.get(playername);
		if (c == null || !c.permission.equalsIgnoreCase(channel)) {
			return false;
		}
		return true;
	}
	
	/**
	 * A player in a channel sends a message
	 * @param p The player
	 * @param message The message
	 */
	public void playerInChannel(Player p, String message) {
		try {
			Channel channel = playersInChannels.get(p.getName());
			channel.sendChat(p, message);
		} catch (CommandException | NullPointerException e) {
			Util.badMsg(p, "Hey! How'd you get in this channel?");
			playersInChannels.remove(p.getName());
		}
	}
	
	/**
	 * A player tries to toggle into a chat channel
	 * @param p The player
	 * @param chatPermission The permission needed
	 * @throws CommandException if no permission, or non existing channel
	 */
	public void playerSwitchChannel(Player p, String chatPermission) throws CommandException {
		chatPermission = chatPermission.toLowerCase(); //To Lowercase, just incase.
		Channel channel = channels.get(chatPermission); //Get channel
		if (channel == null) throw new CommandException("This channel is not available, or doesn't exist."); //Check if active
		if (!Util.testPermission(p, chatPermission)) { //Test if allowed to talk in channel
			throw new CommandException(ErrorMsg.noPermission);
		}
		playersInChannels.put(p.getName(), channel); //Put in channel
		Util.msg(p, "You are now talking in " + channel.format); //Msg
	}
	
	/**
	 * Player leaves a channel
	 * @param p The player
	 * @param msg Message the player that he has left that channel
	 */
	public void playerLeaveChannel(Player p, boolean msg) {
		Channel channel = playersInChannels.get(p.getName()); //Get channel
		if (channel == null) return; //Safety dance
		playersInChannels.remove(p.getName());
		if (msg) {
			Util.msg(p, "You have left the chat channel: " + ChatColor.GREEN + channel.permission);
		}
	}
	
	/**
	 * Create a new ChatChannel
	 * @param permission The permission needed / Also the command
	 * @param format The format send
	 */
	private void createChannel(String permission, String format) {
		channels.put(permission, new Channel(permission, format));
	}
	
	/**
	 * Get all allowed channels for a player
	 * @param p The player
	 * @return the channels
	 */
	public ArrayList<String> getAllowedChannels(Player p) {
		ArrayList<String> allowed = new ArrayList<>();
		for (String channel : channels.keySet()) {
			if (Util.testPermission(p, channel)) {
				allowed.add(channel);
			}
		}
		return allowed;
	}
	
	/**
	 * Send a message to a chat channel
	 * @param channel The channel
	 * @param player The Player
	 * @param message The message
	 */
	public void sendToChannel(String channel, String player, String message) {
		//Get channel
		Channel c = channels.get(channel);
		if (channel == null) return;
		
		//Send chat
		c.sendChat(player, message);
	}
	
	private class Channel {
		
		private String format;
		private String permission; //Same as Command
		
		public Channel(String permission, String format) {
			this.format = format;
			this.permission = permission;
		}
		
		/**
		 * Send a chat message to all the members of this channel
		 * @param sender The sender
		 * @param args The message
		 * @throws CommandException if no permission
		 */
		public void sendChat(CommandSender sender, String... args) throws CommandException {
			if (!Util.testPermission(sender, permission)) {
				throw new CommandException(ErrorMsg.noPermission);
			}
			if (args.length == 0) {
				throw new UsageException(permission + " [message]");
			}
			String message =  Util.buildString(args, " ", 0);
			Util.messagePermissionHolders(permission, format + ChatColor.WHITE + " <" + ChatColor.LIGHT_PURPLE + sender.getName() + ChatColor.WHITE + "> " + message);
			if (plugin.hasSlapBridged()) { //Check if API available
				SlapBridged.getAPI().chatChannelMessage(permission, sender.getName(), message);
			}
		}
		
		/**
		 * Send a chat message to all members of this channel
		 * @param player The player who send it
		 * @param message The message
		 */
		public void sendChat(String player, String message) {
			Util.messagePermissionHolders(permission, format + ChatColor.WHITE + " <" + ChatColor.LIGHT_PURPLE + player + ChatColor.WHITE + "> " + message);
		}
		
	}

	@Override
	public void shutdown() {
		//No shutdown needed
	}

}
