package me.naithantu.SlapHomebrew.Commands.Basics;

import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import me.naithantu.SlapHomebrew.Commands.AbstractCommand;
import me.naithantu.SlapHomebrew.Commands.Exception.CommandException;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Iterator;

public class WorldguardsCommand extends AbstractCommand {

	public WorldguardsCommand(CommandSender sender, String[] args) {
		super(sender, args);
	}
	
	@Override
	public boolean handle() throws CommandException {
		Player player = getPlayer(); //Cast to player
		testPermission("worldguards"); //Test permission
		
		try {
			WorldGuardPlugin wg = plugin.getworldGuard();
			ApplicableRegionSet regions = wg.getRegionManager(player.getWorld()).getApplicableRegions(player.getLocation());
			LocalPlayer localPlayer = wg.wrapPlayer(player); //Make a WorldGuard player
			
			String[] messages = new String[2];
			messages[0] = ChatColor.YELLOW + "Can you build? " + (regions.canBuild(localPlayer) ? "Yes" : "No");
			Iterator<ProtectedRegion> it = regions.iterator();
			boolean first = true;
			while (it.hasNext()) { //Loop thru found regions
				if (first) {
					first = false;
					messages[1] = ChatColor.YELLOW + "Regions: " + it.next().getId();
				} else {
					messages[1] = messages[1] + ", " + it.next().getId();
				}
			}
			if (first) { //If no regions found
				player.sendMessage(ChatColor.YELLOW + "No defined regions here!");
			} else {
				player.sendMessage(messages);
			}
		} catch (Exception e) {
			throw new CommandException("Failed to get regions.");
		}
		return true;
	}
	

}
