package me.naithantu.SlapHomebrew.Commands.VIP;

import me.naithantu.SlapHomebrew.Commands.AbstractVipCommand;
import me.naithantu.SlapHomebrew.Commands.Exception.CommandException;
import org.bukkit.command.CommandSender;

import java.util.HashSet;

public class TpBlockCommand extends AbstractVipCommand {

	public TpBlockCommand(CommandSender sender, String[] args) {
		super(sender, args);
	}

	public boolean handle() throws CommandException {
		testPermission("tpblock");

        //TODO: Update to UUIDs
		HashSet<String> tpBlocks = plugin.getTpBlocks();
		if (tpBlocks.contains(sender.getName())) {
			tpBlocks.remove(sender.getName());
			hMsg("You have been removed from the tpblock list!");
		} else {
			tpBlocks.add(sender.getName());
			hMsg("You have been added to the tpblock list!");
		}

		return true;
	}
}
