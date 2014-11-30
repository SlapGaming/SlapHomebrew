package me.naithantu.SlapHomebrew.Commands.Staff.ImprovedRegion;

import com.sk89q.worldguard.bukkit.commands.region.RegionPrintoutBuilder;
import com.sk89q.worldguard.domains.DefaultDomain;
import me.naithantu.SlapHomebrew.Commands.Exception.CommandException;
import me.naithantu.SlapHomebrew.Commands.Exception.IRGException;
import me.naithantu.SlapHomebrew.Commands.Staff.ImprovedRegionCommand.Perm;

import me.naithantu.SlapHomebrew.PlayerExtension.UUIDControl;
import me.naithantu.SlapHomebrew.Util.Log;
import me.naithantu.SlapHomebrew.Util.Util;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import com.sk89q.worldguard.protection.regions.ProtectedRegion;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class InfoCommand extends AbstractImprovedRegionCommand {

	private boolean all;
	
	/**
	 * Make a new info command
	 * @param p The player
	 * @param args The args of the command
	 * @param all Is allowed to check all regions
	 */
	public InfoCommand(Player p, String[] args, boolean all) {
		super(p, args);
		this.all = all;
	}

	@Override
	protected void action() throws CommandException {		
		ProtectedRegion region = null;
		if (args.length == 1 || (args.length == 2 && args[1].equalsIgnoreCase("sel"))) { //No regionname specified. Get highest priority
			if (all) { //Allowed to get all regions
				region = getHighestPriorityRegion();
			} else { //Only allowed to get their own regions
				region = getHighestOwnedPriorityRegion(true);
			}
		} else {
			validateRegionID(args[1]); //Check if valid region
			region = getRegion(args[1]); //Get the region by name
			if (!all) { //If not acces to all regions
				String playername = p.getName();
				if (!region.getMembers().contains(p.getUniqueId()) && !region.getOwners().contains(p.getUniqueId())) { //Check if owner or member
					throw new IRGException("You are not the owner or member of the specified region.");
				}
			}
		}
		
		if (args[args.length - 1].equalsIgnoreCase("sel")) { //Select region
			if (p.hasPermission(Perm.select.toString())) {
				setPlayerSelection(region);
			}
		}
		
		RegionPrintoutBuilder builder = new RegionPrintoutBuilder(region, null); //Build the string
		try {
            //First line
            builder.append(ChatColor.GRAY);
            builder.append("\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550");
            builder.append(" Region Info ");
            builder.append("\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550");
            builder.append("\n");
            //Basic info
            builder.appendBasics();
            builder.appendFlags();
            builder.appendParents();

            //Users
            builder.append(ChatColor.BLUE + "Owners: ");
            builder.append(uuidSetToString(region.getOwners()));
            builder.append("\n");
            builder.append(ChatColor.BLUE + "Members: ");
            builder.append(uuidSetToString(region.getMembers()));
            builder.append("\n");

            //Bounds
            builder.appendBounds();

            //Send
            builder.send(p);
        } catch (Exception e) {
            Util.badMsg(p, "An error occurred, sorry!");
            Log.warn("Error occurred (IRG Info Command): " + e.getMessage());
        }
	}

    private String uuidSetToString(DefaultDomain domain) {
        UUIDControl uuidControl = UUIDControl.getInstance();
        Set<UUID> set = domain.getUniqueIds();
        if (set.isEmpty()) {
            return ChatColor.RED + "(none)";
        } else {
            HashSet<String> names = new HashSet<>();
            for (UUID owner : domain.getUniqueIds()) {
                names.add(uuidControl.getUUIDProfile(owner).getCurrentName());
            }
            return ChatColor.YELLOW + Util.buildString(names, ", ");
        }
    }

}
