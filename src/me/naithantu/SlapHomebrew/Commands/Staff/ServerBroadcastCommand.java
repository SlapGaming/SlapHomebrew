package me.naithantu.SlapHomebrew.Commands.Staff;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import me.naithantu.SlapHomebrew.Commands.AbstractCommand;
import me.naithantu.SlapHomebrew.Commands.Exception.CommandException;
import me.naithantu.SlapHomebrew.Commands.Exception.UsageException;
import me.naithantu.SlapHomebrew.Util.Util;

public class ServerBroadcastCommand extends AbstractCommand {

	public ServerBroadcastCommand(CommandSender sender, String[] args) {
		super(sender, args);
	}

	@Override
	public boolean handle() throws CommandException {
		testPermission("serverbroadcast"); //Test permission
		if (args.length == 0) throw new UsageException("sBroadcast [Message]"); //Usage
		
		
		//Create broadcast
		String message = "[&cServer-Broadcast&f] &a" + Util.buildString(args, " ", 0);
		Util.broadcast(ChatColor.translateAlternateColorCodes('&', message));

		return true;
	}

}
