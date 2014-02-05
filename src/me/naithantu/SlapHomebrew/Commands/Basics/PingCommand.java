package me.naithantu.SlapHomebrew.Commands.Basics;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_7_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;

import me.naithantu.SlapHomebrew.Commands.AbstractCommand;
import me.naithantu.SlapHomebrew.Commands.Exception.CommandException;
import me.naithantu.SlapHomebrew.Util.Util;

public class PingCommand extends AbstractCommand {

	public PingCommand(CommandSender sender, String[] args) {
		super(sender, args);
	}

	@Override
	public boolean handle() throws CommandException {
		Player p;
		if (args.length > 0 && Util.testPermission(sender, "pingother")) { //If pinging other player
			p = getOnlinePlayer(args[0], false); //Get player
		} else {
			p = getPlayer();
			testPermission("ping");
		}
		
		int ping = ((CraftPlayer) p).getHandle().ping; //Get the player's ping
		if (ping == 0) { //Not calculated yet
			hMsg("Your ping hasn't been calculated yet! Try again later!");
		} else {
			ping = ping / 2;
			ChatColor cc;
			if (ping < 75) { //Good ping
				cc = ChatColor.GREEN;
			} else if (ping < 150) { //Decent
				cc = ChatColor.DARK_GREEN;
			} else if (ping < 250) { //Mediocre ping
				cc = ChatColor.GOLD;
			} else if (ping < 400) { //Bad ping
				cc = ChatColor.RED;
			} else { //Horrible ping
				cc = ChatColor.DARK_RED;
			}
			hMsg((sender == p ? "Your" : p.getName() + "'s") + " ping (Connection latency) is: " + cc + ping + "ms " + ChatColor.GRAY + "(lower is better)"); //Message
		}
		return true;
	}

}
