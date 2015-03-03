package me.naithantu.SlapHomebrew.Commands.Basics;

import me.naithantu.SlapHomebrew.Commands.AbstractCommand;
import me.naithantu.SlapHomebrew.Commands.Exception.CommandException;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CreativeextraCommand extends AbstractCommand {
	
	public CreativeextraCommand(CommandSender sender, String[] args) {
		super(sender, args);
	}

	public boolean handle() throws CommandException {
		Player p = getPlayer();
		testPermission("creativeextra");
		plugin.getExtras().getMenus().getCreativeMenu().open(p);
		return true;
	}
}
