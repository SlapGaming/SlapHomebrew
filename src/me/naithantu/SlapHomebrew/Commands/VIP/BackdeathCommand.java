package me.naithantu.SlapHomebrew.Commands.VIP;

import me.naithantu.SlapHomebrew.Commands.AbstractVipCommand;
import me.naithantu.SlapHomebrew.Commands.Exception.CommandException;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class BackdeathCommand extends AbstractVipCommand {

	public BackdeathCommand(CommandSender sender, String[] args) {
		super(sender, args);
	}

	public boolean handle() throws CommandException {
		Player player = getPlayer();
		testPermission("backdeath");
		
		if (plugin.getBackDeathMap().containsKey(player.getName())) {
			player.teleport(plugin.getBackDeathMap().get(player.getName()));
			hMsg("You have been warped to your death location!");
		}
		return true;
	}
}
