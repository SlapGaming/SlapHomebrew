package me.naithantu.SlapHomebrew.Commands;

import java.util.List;

import me.naithantu.SlapHomebrew.SlapHomebrew;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;

public class TpallowCommand extends AbstractCommand {
	
	public TpallowCommand(CommandSender sender, String[] args, SlapHomebrew plugin) {
		super(sender, args, plugin);
	}

	public boolean handle() {
		if (!testPermission(sender, "tpblock")) {
			this.noPermission(sender);
			return true;
		}

		FileConfiguration config = plugin.getConfig();
		if (args.length == 1) {
			String arg = args[0].toLowerCase();
			if (!config.getStringList("tpallow." + sender.getName()).contains(arg)) {
				List<String> tempList = config.getStringList("tpallow." + sender.getName());
				tempList.add(arg);
				config.set("tpallow." + sender.getName(), tempList);
				this.msg(sender, "Added " + args[0] + " to the whitelist!");
				plugin.saveConfig();
			} else {
				List<String> tempList = config.getStringList("tpallow." + sender.getName());
				tempList.remove(arg);
				config.set("tpallow." + sender.getName(), tempList);
				this.msg(sender, " Removed " + args[0] + " from the whitelist!");
				plugin.saveConfig();
			}
		} else {
			this.msg(sender, "You are currently allowing:");
			this.msg(sender, config.getStringList("tpallow." + sender.getName()) + "");
		}

		return true;
	}
}
