package me.naithantu.SlapHomebrew.Commands;

import java.util.HashSet;

import me.naithantu.SlapHomebrew.SlapHomebrew;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CakedefenceCommand extends AbstractCommand {
	static HashSet<String> chatBotBlocks = new HashSet<String>();

	boolean cakePlaying = false;
	SlapHomebrew plugin;
	
	public CakedefenceCommand(CommandSender sender, String[] args, SlapHomebrew plugin) {
		super(sender, args, plugin);
		this.plugin = plugin;
	}

	public boolean handle() {
		if (!(sender instanceof Player)) {
			this.badMsg(sender, "You need to be in-game to do that!");
			return true;
		}

		if (!testPermission(sender, "cakedefence")) {
			this.noPermission(sender);
			return true;
		}

		Player player = (Player) sender;
		String arg;
		if (args.length == 1) {
			arg = args[0];
			if (arg.equalsIgnoreCase("toggle")) {
				if (SlapHomebrew.allowCakeTp == false) {
					SlapHomebrew.allowCakeTp = true;
					player.sendMessage(ChatColor.GOLD + "[SLAP] " + ChatColor.WHITE + "Players can now teleport to cake defence!");
				} else {
					SlapHomebrew.allowCakeTp = false;
					player.sendMessage(ChatColor.GOLD + "[SLAP] " + ChatColor.WHITE + "Players can no longer teleport to cake defence!");
				}
			}
			if (arg.equalsIgnoreCase("startround")) {
				if (cakePlaying == true) {
					player.sendMessage(ChatColor.GOLD + "[SLAP] " + ChatColor.WHITE + "Wait at least one minute before you start the next round!");
				} else {
					Bukkit.getServer().broadcastMessage(ChatColor.GOLD + "[SLAP] " + ChatColor.WHITE + "The next round of cake defence is starting in 10 seconds!");
					final World world = Bukkit.getServer().getWorld("world");
					cakePlaying = true;
					Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
						public void run() {
							cakePlaying = false;
						}
					}, 1200);
					Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
						public void run() {
							world.getBlockAt(323, 24, -716).setTypeId(76);
							Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
								public void run() {
									Bukkit.getServer().broadcastMessage(ChatColor.GOLD + "[SLAP] " + ChatColor.WHITE + "The next round of cake defence has started!");
									world.getBlockAt(323, 24, -716).setTypeId(0);
								}
							}, 60);
						}
					}, 140);
				}

			}
		} else {
			return false;
		}

		return true;
	}
}
