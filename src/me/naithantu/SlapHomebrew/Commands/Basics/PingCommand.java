package me.naithantu.SlapHomebrew.Commands.Basics;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_7_R4.entity.CraftPlayer;
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

        //Server status
        double tps = plugin.getLag().getTPS();
        String status = ChatColor.YELLOW + "Server Status: ";

        if (tps == 20.0) {
            status += ChatColor.GREEN + "Perfect!";
        } else if (tps >= 17 && tps <= 23) {
            status += ChatColor.GREEN + "All Good!";
        } else if (tps >= 14 && tps <= 26) {
            status += ChatColor.GOLD + "Small Hiccup.";
        } else {
            status += ChatColor.RED + "Struggling.";
        }

        status += " (" + ((double) Math.round(tps * 10) / 10) + " Ticks)";
        msg(status);

        //Ping status
		int ping = ((CraftPlayer) p).getHandle().ping; //Get the player's ping
		if (ping == 0) { //Not calculated yet
			msg(ChatColor.YELLOW + (sender == p ? "Your" : p.getName() + "'s") + " ping hasn't been calculated yet! Try again later!");
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
			msg(ChatColor.YELLOW + (sender == p ? "Your" : p.getName() + "'s") + " ping: " + cc + ping + "ms " + ChatColor.GRAY + "(Green = good, Red = bad)"); //Message
		}
		return true;
	}

}
