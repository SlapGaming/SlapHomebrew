package me.naithantu.SlapHomebrew.Commands;

import me.naithantu.SlapHomebrew.SlapHomebrew;
import me.naithantu.SlapHomebrew.Util;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class WarpsonicCommand extends AbstractCommand {
	public WarpsonicCommand(CommandSender sender, String[] args, SlapHomebrew plugin) {
		super(sender, args, plugin);
	}

	public boolean handle() {
		if (!(sender instanceof Player)) {
			this.badMsg(sender, "You need to be in-game to do that!");
			return true;
		}

		if (!testPermission(sender, "warpsonic")) {
			this.noPermission(sender);
			return true;
		}

		final Player player = (Player) sender;
		if (Util.hasEmptyInventory(player)) {
			plugin.getSonic().addPlayer(player.getName());
			final World world = Bukkit.getServer().getWorld("world_sonic");
			player.teleport(new Location(world, 1394.5, 64.0, -425.5));
			final Block block = world.getBlockAt(1397, 64, -429);
			block.setType(Material.REDSTONE_TORCH_ON);
			Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable(){
				public void run(){
					block.setType(Material.AIR);
					player.teleport(new Location(world, 1355.5, 68, -416.5, 180, 0));
				}
			}, 8);
			this.msg(sender, "You have been teleported to the sonic racetrack!");
		} else {
			this.badMsg(sender, "Empty your inventory and take off your armor, then use /warpsonic again!");
		}
		return true;
	}
}
