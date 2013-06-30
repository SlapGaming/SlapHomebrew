package me.naithantu.SlapHomebrew.Listeners;

import me.naithantu.SlapHomebrew.Flag;
import me.naithantu.SlapHomebrew.SlapHomebrew;
import me.naithantu.SlapHomebrew.Util;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

public class PlayerDeathListener implements Listener {
	SlapHomebrew plugin;

	public PlayerDeathListener(SlapHomebrew plugin) {
		this.plugin = plugin;
	}

	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent event) {
		if (event.getEntity() instanceof Player) {
			Player player = event.getEntity();
			World world = player.getWorld();
			//Only send death messages from pvp world to players in the pvp world.
			if (world.getName().equalsIgnoreCase("world_pvp")) {
				String message = event.getDeathMessage();
				event.setDeathMessage(null);
				for (Player messagePlayer : Bukkit.getServer().getOnlinePlayers()) {
					if (messagePlayer.getWorld().getName().equalsIgnoreCase("world_pvp"))
						messagePlayer.sendMessage(message);
				}
				System.out.println(message);
			}

			//Add backdeath location if not in pvp/end world or nobackdeath region.
			if (player.hasPermission("slaphomebrew.backdeath")) {
				if (!world.getName().equalsIgnoreCase("world_pvp") && !world.getName().equalsIgnoreCase("world_the_end") && !Util.hasFlag(plugin, player.getLocation(), Flag.NOBACKDEATH)) {
					SlapHomebrew.backDeath.put(player.getName(), player.getLocation());
					player.sendMessage(ChatColor.GRAY + "Use the /backdeath command to return to your death point.");
				}
			}

			//Print death location to console.
			Location location = player.getLocation();
			System.out.println(player.getName() + " died at (" + location.getBlockX() + ", " + location.getBlockY() + ", " + location.getBlockZ() + " in world " + world.getName() + ").");
		}
	}
}
