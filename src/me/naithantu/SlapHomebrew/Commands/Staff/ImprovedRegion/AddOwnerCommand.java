package me.naithantu.SlapHomebrew.Commands.Staff.ImprovedRegion;

import java.util.ArrayList;

import me.naithantu.SlapHomebrew.SlapHomebrew;
import me.naithantu.SlapHomebrew.Commands.Exception.CommandException;
import me.naithantu.SlapHomebrew.Commands.Exception.UsageException;
import me.naithantu.SlapHomebrew.Util.Util;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import com.sk89q.worldguard.domains.DefaultDomain;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

public class AddOwnerCommand extends AbstractImprovedRegionCommand {

	public AddOwnerCommand(SlapHomebrew plugin, Player p, String[] args) {
		super(plugin, p, args);
	}

	@Override
	protected void action() throws CommandException {
		if (args.length < 3) throw new UsageException("irg addowner <Region ID> <Owner 1> [Owner 2].."); //Usage
		
		validateRegionID(args[1]); //Check if a valid region
		ProtectedRegion region = getRegion(args[1]); //Get the region
		
		ArrayList<String> offPlayers = new ArrayList<>();
		for (int x = 2; x < args.length; x++) { //Get players
			offPlayers.add(getOfflinePlayer(args[x]).getName());
		}
		
		if (offPlayers.size() == 0) throw new CommandException("No players found!");
		
		DefaultDomain owners = region.getOwners();
		for (String player : offPlayers) {
			if (!owners.contains(player)) {
				owners.addPlayer(player);
			}
		}
		
		saveChanges();
		hMsg("Added " + ChatColor.RED + Util.buildString(offPlayers, ChatColor.YELLOW + ", " + ChatColor.RED) + ChatColor.YELLOW + " as owners to region " + ChatColor.RED + region.getId());
		
	}

}
