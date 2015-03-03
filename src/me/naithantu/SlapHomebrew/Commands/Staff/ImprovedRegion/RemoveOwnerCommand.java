package me.naithantu.SlapHomebrew.Commands.Staff.ImprovedRegion;

import com.sk89q.worldguard.domains.DefaultDomain;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import me.naithantu.SlapHomebrew.Commands.Exception.CommandException;
import me.naithantu.SlapHomebrew.Commands.Exception.UsageException;
import me.naithantu.SlapHomebrew.Controllers.PlayerLogging.RegionLogger;
import me.naithantu.SlapHomebrew.Controllers.PlayerLogging.RegionLogger.ChangeType;
import me.naithantu.SlapHomebrew.Controllers.PlayerLogging.RegionLogger.ChangerIsA;
import me.naithantu.SlapHomebrew.Util.Util;
import nl.stoux.SlapPlayers.Model.Profile;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.UUID;

public class RemoveOwnerCommand extends AbstractImprovedRegionCommand {

	public RemoveOwnerCommand(Player p, String[] args) {
		super(p, args);
	}

	@Override
	protected void action() throws CommandException {
		if (args.length < 3) throw new UsageException("irg removeowner <Region ID> <Owner 1> [Owner 2].."); //Usage
		
		validateRegionID(args[1]); //Check if a valid region
		ProtectedRegion region = getRegion(args[1]); //Get the region
		
		ArrayList<Profile> offPlayers = new ArrayList<>();
		for (int x = 2; x < args.length; x++) { //Get players
			offPlayers.add(getOfflinePlayer(args[x]));
		}
		
		if (offPlayers.size() == 0) throw new CommandException("No players found!");

        HashSet<String> playernames = new HashSet<>();
        //Remove the owners
		DefaultDomain owners = region.getOwners();
		for (Profile player : offPlayers) {
            UUID playerUUID = UUID.fromString(player.getUUIDString());
			if (owners.contains(playerUUID)) {
				owners.removePlayer(playerUUID);
                playernames.add(player.getCurrentName());
			}
		}
		
		//Save & Msg
		saveChanges();
		hMsg("Removed " + ChatColor.RED + Util.buildString(playernames, ChatColor.YELLOW + ", " + ChatColor.RED) + ChatColor.YELLOW + " as owners from region " + ChatColor.RED + region.getId());
		
		//Log
		RegionLogger.logRegionChange(region, p, ChangerIsA.staff, ChangeType.removeowner, Util.buildString(playernames, " "));
	}

}
