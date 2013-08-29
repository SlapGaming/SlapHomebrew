package me.naithantu.SlapHomebrew.Commands.Games;

import me.naithantu.SlapHomebrew.SlapHomebrew;
import me.naithantu.SlapHomebrew.Commands.AbstractCommand;
import me.naithantu.SlapHomebrew.Util.Util;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class WarpcakedefenceCommand extends AbstractCommand {
	public WarpcakedefenceCommand(CommandSender sender, String[] args, SlapHomebrew plugin) {
		super(sender, args, plugin);
	}

	public boolean handle() {
		if (!(sender instanceof Player)) {
			this.badMsg(sender, "You need to be in-game to do that!");
			return true;
		}

		if (!testPermission(sender, "warpcakedefence")) {
			this.noPermission(sender);
			return true;
		}

		Player player = (Player) sender;
		if (plugin.isAllowCakeTp() == true) {
			if (Util.hasEmptyInventory(player)) {
				player.teleport(new Location(Bukkit.getServer().getWorld("world") , 333.0, 28.0, -722.0));
				this.msg(sender, "You have been teleported to cake defence!");
				player.setFoodLevel(20);
				player.setHealth(20d);
			} else {
				this.badMsg(sender, "Empty your inventory and take off your armor, then use /warpcakedefence again!");
			}
		} else {
			if (Util.hasEmptyInventory(player)) {
				player.teleport(new Location(Bukkit.getServer().getWorld("world"), 333.0, 45.0, -751.0));
				this.msg(sender, "You aren't allowed to tp to cakedefence now, you have been teleported to the spectator area!");
			} else {
				this.badMsg(sender, "Empty your inventory and take off your armor, then use /warpcakedefence again!");
			}
		}
		return true;
	}
}
