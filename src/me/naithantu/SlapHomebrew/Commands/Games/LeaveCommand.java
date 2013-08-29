package me.naithantu.SlapHomebrew.Commands.Games;

import java.util.HashSet;

import me.naithantu.SlapHomebrew.SlapHomebrew;
import me.naithantu.SlapHomebrew.Commands.AbstractCommand;
import me.naithantu.SlapHomebrew.Util.Util;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class LeaveCommand extends AbstractCommand {

	public LeaveCommand(CommandSender sender, String[] args, SlapHomebrew plugin) {
		super(sender, args, plugin);
	}

	public boolean handle() {
		if (!testPermission(sender, "leave")) {
			this.noPermission(sender);
			return true;
		}

		final Player player = (Player) sender;
		if (player.getWorld().getName().equalsIgnoreCase("world_pvp") || player.getWorld().getName().equalsIgnoreCase("world_nether")) {
			final HashSet<String> pvpTimer = plugin.getExtras().getPvpTimer();
			if (!pvpTimer.contains(player.getName())) {
				final HashSet<String> pvpWorld = plugin.getExtras().getPvpWorld();
				pvpWorld.add(player.getName());
				pvpTimer.add(player.getName());
				Util.msg(sender, "Teleporting to spawn in 10 seconds!");
				Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
					public void run() {
						if (pvpWorld.contains(player.getName())) {
							pvpWorld.remove(player.getName());
							pvpTimer.remove(player.getName());
							World world = Bukkit.getServer().getWorld("world_start");
							player.teleport(world.getSpawnLocation());
							Util.msg(sender, "You have been teleported to spawn!");
						} else {
							pvpTimer.remove(player.getName());
						}
					}
				}, 200);
			} else {
				player.sendMessage(ChatColor.RED + "You are not allowed to use this more then once per 10 seconds!");
			}

		} else {
			Util.badMsg(sender, "You are not in the pvp world!");
		}
		return true;
	}
}
