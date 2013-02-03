package me.naithantu.SlapHomebrew.Commands;

import java.util.HashSet;

import me.naithantu.SlapHomebrew.SlapHomebrew;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TeCommand extends AbstractCommand {
	static HashSet<String> chatBotBlocks = new HashSet<String>();
	
	public TeCommand(CommandSender sender, String[] args) {
		super(sender, args);
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
			if (SlapHomebrew.tpBlocks.contains(tpPlayer)) {
				if (!player.hasPermission("slaphomebrew.tpblockoverride")
						&& !Bukkit.getPluginManager().getPlugin("SlapHomebrew").getConfig().getStringList("tpallow." + tpPlayer).contains(player.getName().toLowerCase())) {
					this.badMsg(sender, "You may not tp to that player at the moment, use /tpa [playername] to request a teleport!");
					return true;
				}
			}
			if (!Bukkit.getPlayer(arg).getWorld().getName().equalsIgnoreCase("world_pvp") && !Bukkit.getPlayer(arg).getWorld().getName().equalsIgnoreCase("world_the_end")) {
				double yLocation = 0;
				Location tpLocation = null;
				for (yLocation = 0; yLocation > -300 && Bukkit.getPlayer(arg).getLocation().add(0, yLocation, 0).getBlock().getType() == Material.AIR; yLocation--) {
				}
				if (yLocation < -299) {
					this.badMsg(sender, "There is no floor below the target player!");
				} else {
					tpLocation = Bukkit.getPlayer(tpPlayer).getLocation().add(0, yLocation + 1, 0);
					player.teleport(tpLocation);
				}
				player.sendMessage(ChatColor.GRAY + "Teleporting...");
			} else {
				this.badMsg(sender, "You may not tp to that player at the moment, he/she is in a pvp world!");
			}

		} else {
			return false;
		}
		return true;
	}
}
