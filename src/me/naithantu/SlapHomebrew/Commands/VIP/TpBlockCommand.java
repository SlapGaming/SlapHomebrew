package me.naithantu.SlapHomebrew.Commands.VIP;

import java.util.HashSet;

import me.naithantu.SlapHomebrew.SlapHomebrew;
import me.naithantu.SlapHomebrew.Commands.AbstractCommand;

import org.bukkit.command.CommandSender;

public class TpBlockCommand extends AbstractCommand {

	public TpBlockCommand(CommandSender sender, String[] args, SlapHomebrew plugin) {
		super(sender, args, plugin);
	}

	public boolean handle() {
		if (!testPermission(sender, "tpblock")) {
			this.noPermission(sender);
			return true;
		}

		HashSet<String> tpBlocks = plugin.getTpBlocks();
		if (tpBlocks.contains(sender.getName())) {
			tpBlocks.remove(sender.getName());
			this.msg(sender, "You have been removed from the tpblock list!");
		} else {
			tpBlocks.add(sender.getName());
			this.msg(sender, "You have been added to the tpblock list!");
		}

		return true;
	}
}
