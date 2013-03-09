package me.naithantu.SlapHomebrew.Listeners;

import me.naithantu.SlapHomebrew.SlapHomebrew;

import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

public class DeathListener implements Listener {
	SlapHomebrew plugin;
		public DeathListener(SlapHomebrew plugin){
			this.plugin = plugin;
		}
	
	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent event) {
		if (event.getEntity() instanceof Player) {
			Player player = event.getEntity();
			World world = player.getWorld();
			if (player.hasPermission("slaphomebrew.backdeath")) {
				Boolean allowBackDeath = true;
				RegionManager regionManager = plugin.getWorldGuard().getRegionManager(world);
				ApplicableRegionSet noBackDeathRegions = regionManager.getApplicableRegions(player.getLocation());
				for(ProtectedRegion region: noBackDeathRegions){
					if(region.getMembers().contains("nobackdeath")){
						allowBackDeath = false;
						break;
					}
				}
				
				if (!world.getName().equalsIgnoreCase("world_pvp") && !world.getName().equalsIgnoreCase("world_the_end") && allowBackDeath) {
					SlapHomebrew.backDeath.put(player.getName(), player.getLocation());
					player.sendMessage(ChatColor.GRAY + "Use the /backdeath command to return to your death point.");
				}
			}
			System.out.println(player.getName() + " died at (" + player.getLocation().getBlockX() + ", " + player.getLocation().getBlockY() + ", " + player.getLocation().getBlockZ() + ").");
		}
	}
}
