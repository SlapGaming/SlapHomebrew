package me.naithantu.SlapHomebrew.Commands.VIP;

import me.naithantu.SlapHomebrew.SlapHomebrew;
import me.naithantu.SlapHomebrew.Commands.AbstractCommand;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TeCommand extends AbstractCommand {

	public TeCommand(CommandSender sender, String[] args, SlapHomebrew plugin) {
		super(sender, args, plugin);
	}

	public boolean handle() {
		if (!(sender instanceof Player)) {
			this.badMsg(sender, "You need to be in-game to do that!");
			return true;
		}

		if (!testPermission(sender, "tp")) {
			this.noPermission(sender);
			return true;
		}

		String arg = null;
		Player player = (Player) sender;
		if (args.length == 1) {
			arg = args[0];
			String tpPlayer;
			try {
				tpPlayer = Bukkit.getPlayer(arg).getName();
			} catch (NullPointerException e) {
				this.badMsg(sender, "Error: Player not found.");
				return true;
			}
			if (plugin.getTpBlocks().contains(tpPlayer)) {
				if (!player.hasPermission("slaphomebrew.tpblockoverride")
						&& !Bukkit.getPluginManager().getPlugin("SlapHomebrew").getConfig().getStringList("tpallow." + tpPlayer).contains(player.getName().toLowerCase())) {
					this.badMsg(sender, "You may not tp to that player at the moment, use /tpa [playername] to request a teleport!");
					return true;
				}
			}

			Player targetPlayer = Bukkit.getPlayer(arg);
			String targetWorld = targetPlayer.getWorld().getName();
			if (targetWorld.equalsIgnoreCase("world_pvp")) {
				this.badMsg(sender, "You may not tp to that player at the moment, he/she is in a pvp world!");
			} else if (targetWorld.equalsIgnoreCase("world_sonic")) {
				this.badMsg(sender, "You may not tp to that player at the moment, he/she is in the mini-games world!");
			} else {
				double yLocation = 0;
				Location tpLocation = null;
				for (yLocation = 0; yLocation > -300 && targetPlayer.getLocation().add(0, yLocation, 0).getBlock().getType() == Material.AIR; yLocation--) {
				}
				if (yLocation < -299) {
					this.badMsg(sender, "There is no floor below the target player!");
				} else {
					tpLocation = Bukkit.getPlayer(tpPlayer).getLocation().add(0, yLocation + 1, 0);
					player.teleport(tpLocation);
					if (!player.hasPermission("slaphomebrew.staff")) {
						Bukkit.getPlayer(tpPlayer).sendMessage(ChatColor.GRAY + player.getName() + " has teleported to you!");
					}
				}
				player.sendMessage(ChatColor.GRAY + "Teleporting...");
			}

		} else {
			return false;
		}
		return true;
	}
}
