package me.naithantu.SlapHomebrew.Commands.Chat;

import java.util.ArrayList;

import me.naithantu.SlapHomebrew.Commands.AbstractCommand;
import me.naithantu.SlapHomebrew.Commands.Exception.CommandException;
import me.naithantu.SlapHomebrew.Commands.Exception.ErrorMsg;
import me.naithantu.SlapHomebrew.Commands.Exception.UsageException;
import me.naithantu.SlapHomebrew.Controllers.ChatChannels;
import me.naithantu.SlapHomebrew.Util.Util;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ChatToggleCommand extends AbstractCommand {
	
	private ChatChannels cc;
	
	private Player p;
	private String playername;
	
	public ChatToggleCommand(CommandSender sender, String[] args) {
		super(sender, args);
	}

	@Override
	public boolean handle() throws CommandException {
		p = getPlayer(); //Only for players		
		playername = p.getName();
		
		//Get Channels
		cc = plugin.getChatChannels();
		ArrayList<String> channels = cc.getAllowedChannels(p);
		
		//Check if allowed for any channel
		if (channels.isEmpty()) throw new CommandException(ErrorMsg.noPermission);
		
		if (channels.size() == 1 && args.length == 0) { //1 Channel, No Parameters
			switchChannel(channels.get(0));
		} else if (args.length == 0) { //Multiple channels
			throw new UsageException("chattoggle <none | list | [channel]>");
		} else {
			switch (args[0].toLowerCase()) {
			case "none": //Leave channel
				if (cc.isPlayerInChannel(playername)) { //Check if in a channel
					cc.playerLeaveChannel(p, true); //Leave channel
				} else { 
					throw new CommandException("You aren't in a chat channel.");
				}
				break;
				
			case "list": //Get list of all allowed channels
				hMsg("Channels: " + ChatColor.RED + Util.buildString(channels, ChatColor.WHITE + ", " + ChatColor.RED));
				break;
								
			default: //Switch to a channel
				switchChannel(args[0]);
				break;
			}
		}
		return true;
	}
	
	/**
	 * Switch to a (different) channel
	 * Or if already in this channel, leave it
	 * @param channel The channel
	 * @throws CommandException
	 */
	private void switchChannel(String channel) throws CommandException {
		channel = channel.toLowerCase();
		if (cc.isPlayerInChannel(playername)) { //Player is in a channel
			if (cc.isPlayerInChannel(playername, channel)) { //Leaving channel
				cc.playerLeaveChannel(p, true);
			} else { //In a different channel
				cc.playerLeaveChannel(p, false);
				cc.playerSwitchChannel(p, channel);
			}
		} else { //Not in a channel
			cc.playerSwitchChannel(p, channel);
		}
	}

}
