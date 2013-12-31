package me.naithantu.SlapHomebrew.Commands.Lists;

import me.naithantu.SlapHomebrew.Commands.AbstractCommand;
import me.naithantu.SlapHomebrew.Commands.Exception.CommandException;
import me.naithantu.SlapHomebrew.Controllers.ChangeLog;
import me.naithantu.SlapHomebrew.Util.Util;

import org.bukkit.command.CommandSender;

public class ChangeLogCommand extends AbstractCommand {
	
	private static ChangeLog changeLog = null;

	public ChangeLogCommand(CommandSender sender, String[] args) {
		super(sender, args);
		if (changeLog == null) {
			changeLog = plugin.getChangeLog();
		}
	}

	@Override
	public boolean handle() throws CommandException {
		if (args.length == 0) { //get Latest changes
			changeLog.showPage(sender, 1);
		} else {
			if (args.length > 2 && args[0].equalsIgnoreCase("add")) { //Adding to changelog
				testPermission("addchangelog");
				if (!args[1].matches("\\d{2}/\\d{2}/\\d{4}")) throw new CommandException("Invalid date. Format: DD-MM-YYYY"); //Check if date given
				String change = Util.buildString(args, " ", 2); //Parse change
				changeLog.addToChangelog(args[1], change); //Add
				hMsg("Added to changelog: " + change);
				return true;
			} else if (args[0].equals("reload")) { //Reload changelog
				changeLog.reload();
				hMsg("Changelog reloaded.");
				return true;
			}
			
			//Wants a certain page of the changelog
			changeLog.showPage(sender, parseInt(args[0])); //Parse page & Send changelog
		}
		return true;
	}

}
