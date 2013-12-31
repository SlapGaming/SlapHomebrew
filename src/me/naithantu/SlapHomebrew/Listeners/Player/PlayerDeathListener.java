package me.naithantu.SlapHomebrew.Listeners.Player;

import java.util.Random;

import me.naithantu.SlapHomebrew.Controllers.Flag;
import me.naithantu.SlapHomebrew.Controllers.PlayerLogger;
import me.naithantu.SlapHomebrew.Controllers.PlayerLogger.DeathType;
import me.naithantu.SlapHomebrew.Listeners.AbstractListener;
import me.naithantu.SlapHomebrew.Util.Util;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.SkullType;
import org.bukkit.World;
import org.bukkit.configuration.Configuration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

public class PlayerDeathListener extends AbstractListener {
	
	private PlayerLogger playerLogger;

	public PlayerDeathListener(PlayerLogger playerLogger) {
		this.playerLogger = playerLogger;
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerDeathMonitor(PlayerDeathEvent event) {
		if (event.getEntity() instanceof Player) {
			Player player = event.getEntity();
			World world = player.getWorld();
			String playername = player.getName();
			
			//Check for suicides
			if (playerLogger.hasCommitedSuicide(playername)) {
				event.setDeathMessage(null);
				plugin.getServer().broadcastMessage(playername + " has committed suicide..");
			}
			
			String message = event.getDeathMessage();
			if (message != null) { 
				
				//Deaths logger
				if (player.getKiller() != null) {
					playerLogger.addKill(player.getKiller().getName());
					playerLogger.addDeath(DeathType.player, playername);
				} else {
					if (message.contains("slain by")) {
						playerLogger.addDeath(DeathType.mob, playername);
					} else {
						playerLogger.addDeath(DeathType.other, playername);
					}
				}
				
				//Only send death messages from pvp world to players in the pvp world.
				if (world.getName().equalsIgnoreCase("world_pvp")) {
					event.setDeathMessage(null);
					for (Player messagePlayer : Bukkit.getServer().getOnlinePlayers()) {
						if (messagePlayer.getWorld().getName().equalsIgnoreCase("world_pvp"))
							messagePlayer.sendMessage(message);
					}
					System.out.println(message);
				}
			}

			//Add backdeath location if not in pvp/end world or nobackdeath region.
			if (player.hasPermission("slaphomebrew.backdeath")) {
				if (!world.getName().equalsIgnoreCase("world_pvp") && !world.getName().equalsIgnoreCase("world_the_end") && !world.getName().equalsIgnoreCase("world_sonic") && !Util.hasFlag(plugin, player.getLocation(), Flag.NOBACKDEATH)) {
					plugin.getBackDeathMap().put(playername, player.getLocation());
					player.sendMessage(ChatColor.GRAY + "Use the /backdeath command to return to your death point.");
				}
			}

			//Print death location to console.
			Location location = player.getLocation();
			System.out.println(playername + " died at (" + location.getBlockX() + ", " + location.getBlockY() + ", " + location.getBlockZ() + " in world " + world.getName() + ").");			
		}
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerDeath(PlayerDeathEvent event) {
		//Drop player heads in pvp world.
		if (event.getEntity().getWorld().getName().equals("world_pvp")) {
			Entity killer;
			if (event.getEntity().getKiller() instanceof Projectile) {
				Projectile projectile = (Projectile) event.getEntity().getKiller();
				killer = projectile.getShooter();
			} else {
				killer = event.getEntity().getKiller();
			}

			if (killer instanceof Player) {
				Random random = new Random();
				int randomNumber = random.nextInt(100) + 1;
				Configuration config = plugin.getConfig();
				if (!config.contains("headdropchance")) {
					config.set("headdropchance", 5);
					plugin.saveConfig();
				}

				if (randomNumber <= config.getInt("headdropchance")) {
					ItemStack skull = new ItemStack(Material.SKULL_ITEM, 1, (short) SkullType.PLAYER.ordinal());
					SkullMeta skullMeta = (SkullMeta) skull.getItemMeta();
					skullMeta.setOwner(event.getEntity().getName());
					skull.setItemMeta(skullMeta);
					event.getDrops().add(skull);
				}
			}
		}
	}
}
