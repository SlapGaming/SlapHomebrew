package me.naithantu.SlapHomebrew.Commands.Chat;

import me.naithantu.SlapHomebrew.Commands.AbstractCommand;
import me.naithantu.SlapHomebrew.Commands.Exception.CommandException;
import me.naithantu.SlapHomebrew.Controllers.ChatChannels;
import org.bukkit.command.CommandSender;

public class ChatCommand extends AbstractCommand {

	private String channel;
	
	public ChatCommand(CommandSender sender, String channel, String[] args) {
		super(sender, args);
		this.channel = channel;
	}

	@Override
	public boolean handle() throws CommandException {
		ChatChannels channels = plugin.getChatChannels();
		channels.sendToChannel(sender, channel, args);
		return true;
	}

}
