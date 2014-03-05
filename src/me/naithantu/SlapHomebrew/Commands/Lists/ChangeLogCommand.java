package me.naithantu.SlapHomebrew.Commands.Lists;

import java.util.List;

import me.naithantu.SlapHomebrew.SlapHomebrew;
import me.naithantu.SlapHomebrew.Commands.AbstractCommand;
import me.naithantu.SlapHomebrew.Commands.Exception.CommandException;
import me.naithantu.SlapHomebrew.Controllers.ChangeLog;
import me.naithantu.SlapHomebrew.Util.DateFormatUtil;
import me.naithantu.SlapHomebrew.Util.Util;

import org.bukkit.command.CommandSender;

public class ChangeLogCommand extends AbstractCommand {
	
	public ChangeLogCommand(CommandSender sender, String[] args) {
		super(sender, args);
	}

	@Override
	public boolean handle() throws CommandException {
		//Get changelog controller
		ChangeLog changeLog = plugin.getChangeLog();
		
		if (args.length == 0) { //get Latest changes
			changeLog.showPage(sender, 1);
		} else {
			if (args.length > 2 && args[0].equalsIgnoreCase("add")) { //Adding to changelog
				testPermission("addchangelog");
				if (!args[1].matches("\\d{2}/\\d{2}/\\d{4}")) throw new CommandException("Invalid date. Format: DD/MM/YYYY"); //Check if date given
				String change = Util.buildString(args, " ", 2); //Parse change
				changeLog.addToChangelog(args[1], change); //Add
				hMsg("Added to changelog: " + change);
				return true;
			} else if (args[0].equals("reload")) { //Reload changelog
				testPermission("reloadchangelog");
				changeLog.reload();
				hMsg("Changelog reloaded.");
				return true;
			}
			
			//Wants a certain page of the changelog
			changeLog.showPage(sender, parseInt(args[0])); //Parse page & Send changelog
		}
		return true;
	}
	
	/**
	 * TabComplete on this command
	 * @param sender The sender of the command
	 * @param args given arguments
	 * @return List of options
	 */
	public static List<String> tabComplete(CommandSender sender, String[] args) {
		List<String> list;
		switch(args.length) {
		case 1:
			list = createEmptyList();
			if (Util.testPermission(sender, "addchangelog")) { //If has permission to add to changelog
				list.add("add");
			}
			if (Util.testPermission(sender, "reloadchangelog")) { //If has permission to reload changelog
				list.add("reload");
			}
			//Add pages
			int page = 1;
			int pages = SlapHomebrew.getInstance().getChangeLog().getPages(); //Get pages
			while (page <= pages) { //Add pages
				list.add(String.valueOf(page++));
			}
			filterResults(list, args[0]); //Filter the results
			return list;
			
		case 2:
			if (args[0].equalsIgnoreCase("add") && Util.testPermission(sender, "addchangelog") && args[1].isEmpty()) { //Give the current date if nothing else given
				return createNewList(DateFormatUtil.formatDate("dd/MM/yyyy"));
			}
		default:
			return createEmptyList();
		}
	}

}
