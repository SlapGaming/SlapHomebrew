package me.naithantu.SlapHomebrew.Commands.Staff;

import java.util.Set;

import me.naithantu.SlapHomebrew.Commands.AbstractCommand;
import me.naithantu.SlapHomebrew.Commands.Exception.CommandException;
import me.naithantu.SlapHomebrew.Commands.Exception.ErrorMsg;
import me.naithantu.SlapHomebrew.Commands.Exception.UsageException;
import me.naithantu.SlapHomebrew.Controllers.MessageStringer.MessageCommandCombiner;
import me.naithantu.SlapHomebrew.PlayerExtension.PlayerControl;
import me.naithantu.SlapHomebrew.PlayerExtension.SlapPlayer;
import me.naithantu.SlapHomebrew.Storage.YamlStorage;
import me.naithantu.SlapHomebrew.Util.Util;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.MemorySection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

public class MessageCommand extends AbstractCommand {
	
	private static long lastMessage = 0L;
	
    private YamlStorage messageStorage;
    private FileConfiguration messageConfig;
	
	public MessageCommand(CommandSender sender, String[] args) {
		super(sender, args);
        messageStorage = plugin.getMessageStorage();
        messageConfig = messageStorage.getConfig();
	}
	
	public boolean handle() throws CommandException {
		testPermission("message"); //Test perm
		if (args.length < 1) return false; //Check usage
		switch (args[0].toLowerCase()) {
		case "show": //Show the message to the CommandSender
			if (args.length != 2) throw new UsageException("message show [message]"); //Check usage
			msg(getMessage(args[1])); //Send message
			break;
			
		case "list": //Show a full list of messages to the player
			if (!messageConfig.contains("messages")) throw new CommandException("There are no messages!"); //Check if config contains messages 
			Set<String> messages = ((MemorySection) messageConfig.get("messages")).getKeys(true); //Get all messages
			hMsg("Messages: " + ChatColor.RED + Util.buildString(messages, ChatColor.WHITE + ", " + ChatColor.RED)); //Send messages
			break;
			
		case "create": //Create a new message
			testPermission("message.admin"); //Test perm
			if (args.length != 2) throw new UsageException("message create [message]"); //Check usage
			SlapPlayer slapPlayer = PlayerControl.getPlayer(getPlayer()); //Get SlapPlayer
			if (slapPlayer.isCombiningMessage()) { //Check if already combining
				throw new CommandException(ErrorMsg.alreadyCombining);
			}
			String messageName = args[1].toLowerCase();
			slapPlayer.setMessageCombiner(new MessageCommandCombiner(slapPlayer, messageName));
			hMsg("Type the message now, the name of this message is going to be: " + messageName);
			break;
			
		case "remove": //Remove a message
			testPermission("message.admin"); //Test perm
			if (args.length != 2) throw new UsageException("message remove [message]"); //Check usage
			messageName = args[1].toLowerCase();
			checkForMessage(messageName); //Check if message exists
			messageConfig.set("messages." + messageName, null); //Remove from config
			messageStorage.saveConfig();
			hMsg("Removed message: " + messageName);
			break;
			
		case "reload": //Reload the config
			testPermission("message.admin"); //Test perm
			messageStorage.reloadConfig(); //Reload messages
			hMsg("Reloaded messages config!");
			break;
			
		default: //Broadcast a message
			String message = getMessage(args[0].toLowerCase());
			if (sender instanceof Player) { //Spamming prevention for Staff
				if (System.currentTimeMillis() - lastMessage > 2500) { //Check if last message was 2.5+ seconds ago
					lastMessage = System.currentTimeMillis(); //Set last message
				} else { //Throw spamming error
					throw new CommandException("Wait a bit longer before spamming everyone again.");
				}
			}
			plugin.getServer().broadcastMessage(message); //Get message & broodkast
		}
		return true;
	}
	
	/**
	 * Check if a message exists
	 * @param path The path to the message (Prepended with message.)
	 * @throws CommandException if message not found
	 */
	private void checkForMessage(String path) throws CommandException {
		if (!messageConfig.contains("messages." + path)) throw new CommandException("That message does not exist. Type /message list for all messages.");
	}
	
	/**
	 * Get the message from the config
	 * @param path The path to the message (Prepended with message.)
	 * @throws CommandException if message not found
	 */
	private String getMessage(String path) throws CommandException {
		checkForMessage(path);
		return ChatColor.translateAlternateColorCodes('&', messageConfig.getString("messages." + path));
	}
}
