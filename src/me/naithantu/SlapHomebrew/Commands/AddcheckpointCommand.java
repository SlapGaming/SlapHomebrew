package me.naithantu.SlapHomebrew.Commands;

import me.naithantu.SlapHomebrew.SlapHomebrew;
import org.bukkit.command.CommandSender;

public class AddcheckpointCommand extends AbstractCommand {
	public AddcheckpointCommand(CommandSender sender, String[] args, SlapHomebrew plugin) {
		super(sender, args, plugin);
	}

	public boolean handle() {
		if (!testPermission(sender, "addcheckpoint")) {
			this.noPermission(sender);
			return true;
		}

		plugin.getSonic().addCheckpoint(args[0], Integer.parseInt(args[1]));
		return true;
	}
}
