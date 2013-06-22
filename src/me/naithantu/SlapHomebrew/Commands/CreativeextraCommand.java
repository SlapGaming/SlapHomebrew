package me.naithantu.SlapHomebrew.Commands;

import me.naithantu.SlapHomebrew.SlapHomebrew;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CreativeextraCommand extends AbstractCommand {
	SlapHomebrew plugin;
	
	public CreativeextraCommand(CommandSender sender, String[] args, SlapHomebrew plugin) {
		super(sender, args, plugin);
		this.plugin = plugin;
	}

	public boolean handle() {
		if (!(sender instanceof Player)) {
			this.badMsg(sender, "You need to be in-game to do that!");
			return true;
		}

		if (!testPermission(sender, "creativeextra")) {
			this.noPermission(sender);
			return true;
		}

		plugin.getExtras().getMenus().getCreativeMenu().open((Player) sender);
		return true;
	}
}
