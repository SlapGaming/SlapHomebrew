package me.naithantu.SlapHomebrew.Commands.Teleport;

import me.naithantu.SlapHomebrew.Commands.AbstractCommand;
import me.naithantu.SlapHomebrew.Commands.Exception.CommandException;
import me.naithantu.SlapHomebrew.Util.Util;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TeleportCommand extends AbstractCommand {

	public TeleportCommand(CommandSender sender, String[] args) {
		super(sender, args);
	}

	@Override
	public boolean handle() throws CommandException {
		Player p = getPlayer();
		testPermission("tp.direct");
		
		if (args.length != 1) return false; //Usage
		
		Player targetPlayer = getOnlinePlayer(args[0], false); //Get target
		boolean tpUnder = Util.safeTeleport(p, targetPlayer.getLocation(), targetPlayer.isFlying(), true); //Teleport the player
		p.sendMessage(ChatColor.GRAY + "Teleported" + (tpUnder ? " under the player" : "") + "..."); //Msg
		return true;
	}

}
