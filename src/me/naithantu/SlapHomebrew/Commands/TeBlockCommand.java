package me.naithantu.SlapHomebrew.Commands;

import java.util.HashSet;

import me.naithantu.SlapHomebrew.SlapHomebrew;

import org.bukkit.command.CommandSender;

public class TeBlockCommand extends AbstractCommand {
	static HashSet<String> chatBotBlocks = new HashSet<String>();

	public TeBlockCommand(CommandSender sender, String[] args) {
		super(sender, args);
	}

	public boolean handle() {
		if (!testPermission(sender, "tpblock")) {
			this.noPermission(sender);
			return true;
		}

		if (SlapHomebrew.tpBlocks.contains(sender.getName())) {
			SlapHomebrew.tpBlocks.remove(sender.getName());
			this.msg(sender, "You have been removed from the tpblock list!");
		} else {
			SlapHomebrew.tpBlocks.add(sender.getName());
			this.msg(sender, "You have been added to the tpblock list!");
		}

		return true;
	}
}
