package me.naithantu.SlapHomebrew.Commands.Staff;

import org.bukkit.command.CommandSender;

import me.naithantu.SlapHomebrew.Commands.AbstractCommand;
import me.naithantu.SlapHomebrew.Commands.Exception.CommandException;

public class VipForumCommand extends AbstractCommand {

	public VipForumCommand(CommandSender sender, String[] args) {
		super(sender, args);
	}

	@Override
	public boolean handle() throws CommandException {
		
		return true;
	}

}
