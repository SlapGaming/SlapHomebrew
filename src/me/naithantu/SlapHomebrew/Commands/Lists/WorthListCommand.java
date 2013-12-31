package me.naithantu.SlapHomebrew.Commands.Lists;

import me.naithantu.SlapHomebrew.Commands.AbstractCommand;
import me.naithantu.SlapHomebrew.Commands.Exception.CommandException;
import me.naithantu.SlapHomebrew.Commands.Exception.ErrorMsg;
import me.naithantu.SlapHomebrew.Controllers.WorthList;

import org.bukkit.command.CommandSender;

public class WorthListCommand extends AbstractCommand {
	
	public WorthListCommand(CommandSender sender, String[] args) {
		super(sender, args);
	}

	@Override
	public boolean handle() throws CommandException {
		testPermission("worthlist"); //Test permission
		int page = 1; 
		if (args.length > 0) { //If page number given
			page = parseInt(args[0]); //Parse page
			if (page < 1) throw new CommandException(ErrorMsg.notANumber);
		}
		WorthList worthList = plugin.getWorthList(); //Get WorthList
		if (page > worthList.getPages()) throw new CommandException("There are only " + worthList.getPages() + " pages.");
		worthList.sendPage(sender, page); //Send worthlist page
		return true;
	}

}
