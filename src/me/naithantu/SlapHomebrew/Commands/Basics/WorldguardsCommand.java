package me.naithantu.SlapHomebrew.Commands.Basics;

import java.util.Iterator;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

import me.naithantu.SlapHomebrew.SlapHomebrew;
import me.naithantu.SlapHomebrew.Commands.AbstractCommand;

public class WorldguardsCommand extends AbstractCommand {

	public WorldguardsCommand(CommandSender sender, String[] args, SlapHomebrew plugin) {
		super(sender, args, plugin);
	}
	
	@Override
	public boolean handle() {
		if (!testPermission(sender, "worldGuards")) {
			noPermission(sender);
			return true;
		}
		if (!(sender instanceof Player)) {
			badMsg(sender, "You need to be in-game to do that!");
			return true;
		}
		try {
			Player player = (Player) sender;
			WorldGuardPlugin wg = plugin.getworldGuard();
			ApplicableRegionSet regions = wg.getRegionManager(player.getWorld()).getApplicableRegions(player.getLocation());
			LocalPlayer localPlayer = wg.wrapPlayer(player);
			String[] messages = new String[2];
			messages[0] = ChatColor.YELLOW + "Can you build? " + (regions.canBuild(localPlayer) ? "Yes" : "No");
			Iterator<ProtectedRegion> it = regions.iterator();
			boolean first = true;
			while (it.hasNext()) {
				if (first) {
					first = false;
					messages[1] = ChatColor.YELLOW + "Regions: " + it.next().getId();
				} else {
					messages[1] = messages[1] + ", " + it.next().getId();
				}
			}
			if (first) {
				player.sendMessage(ChatColor.YELLOW + "No defined regions here!");
			} else {
				player.sendMessage(messages);
			}
		} catch (Exception e) {
			badMsg(sender, "Failed to get regions.");
		}
		return true;
	}
	

}
