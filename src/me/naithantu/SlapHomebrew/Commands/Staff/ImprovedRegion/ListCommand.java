package me.naithantu.SlapHomebrew.Commands.Staff.ImprovedRegion;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;

import me.naithantu.SlapHomebrew.Commands.Exception.CommandException;
import me.naithantu.SlapHomebrew.Commands.Exception.UsageException;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import com.sk89q.worldguard.protection.regions.ProtectedRegion;

public class ListCommand extends AbstractImprovedRegionCommand {

	private boolean all;
	
	public ListCommand(Player p, String[] args, boolean all) {
		super(p, args);
		this.all = all;
	}

	@Override
	protected void action() throws CommandException {
		String ownedBy = null;
		int biggerThen = 2; //Parse length for pages
		if (all) {
			if (args.length == 1 || (args.length > 2 && args[1].equalsIgnoreCase("-p"))) throw new UsageException("irg list <Player | all>"); //Usage
			if (!args[1].equalsIgnoreCase("all")) { //Check if all or player
				ownedBy = getOfflinePlayer(args[1]).getCurrentName();
			}
		} else {
			ownedBy = p.getName();
			biggerThen = 1;
		}
		
		
		int page = 1;
		if (args.length > biggerThen) { //parse page if given
			page = parseIntPositive(args[biggerThen]);
		}
		page = page - 1;
		
		Map<String, ProtectedRegion> regions = rm.getRegions();
		ArrayList<RegionListEntry> entries = new ArrayList<>();
		
        int index = 0;
        for (String id : regions.keySet()) {
            RegionListEntry entry = new RegionListEntry(id, index++);
            
            // Filtering by owner?
            if (ownedBy != null) {
                entry.isOwner = regions.get(id).isOwner(ownedBy);
                entry.isMember = regions.get(id).isMember(ownedBy);

                if (!entry.isOwner && !entry.isMember) {
                    continue; // Skip
                }
            }

            entries.add(entry);
        }
        
        Collections.sort(entries); //Sort
        
        final int totalSize = entries.size();
        final int pageSize = 10;
        final int pages = (int) Math.ceil(totalSize / (float) pageSize);

        hMsg(ChatColor.RED
                + (ownedBy == null ? "Regions (page " : "Regions for " + ownedBy + " (page ")
                + (page + 1) + " of " + pages + ") in this world:");

        if (page < pages) {
            // Print
            for (int i = page * pageSize; i < page * pageSize + pageSize; i++) {
                if (i >= totalSize) {
                    break;
                }
                sender.sendMessage(ChatColor.YELLOW.toString() + entries.get(i));
            }
        }
		
	}
	
	private class RegionListEntry implements Comparable<RegionListEntry> {
		
	    private final String id;
	    private final int index;
	    boolean isOwner;
	    boolean isMember;

	    public RegionListEntry(String id, int index) {
	        this.id = id;
	        this.index = index;
	    }

	    @Override
	    public int compareTo(RegionListEntry o) {
	        if (isOwner != o.isOwner) {
	            return isOwner ? 1 : -1;
	        }
	        if (isMember != o.isMember) {
	            return isMember ? 1 : -1;
	        }
	        return id.compareTo(o.id);
	    }

	    @Override
	    public String toString() {
	        if (isOwner) {
	            return (index + 1) + ". +" + id;
	        } else if (isMember) {
	            return (index + 1) + ". -" + id;
	        } else {
	            return (index + 1) + ". " + id;
	        }
	    }
		
		
		
	}

}
