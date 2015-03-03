package me.naithantu.SlapHomebrew.Commands.Staff.ImprovedRegion;

import com.sk89q.minecraft.util.commands.CommandException;
import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.bukkit.selections.CuboidSelection;
import com.sk89q.worldedit.bukkit.selections.Polygonal2DSelection;
import com.sk89q.worldedit.bukkit.selections.Selection;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.managers.storage.StorageException;
import com.sk89q.worldguard.protection.regions.GlobalProtectedRegion;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedPolygonalRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import me.naithantu.SlapHomebrew.Commands.AbstractCommand;
import me.naithantu.SlapHomebrew.Commands.Exception.FoundRegionsException;
import me.naithantu.SlapHomebrew.Commands.Exception.IRGException;
import me.naithantu.SlapHomebrew.Util.Util;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.UUID;

public abstract class AbstractImprovedRegionCommand extends AbstractCommand {
	
	private boolean aSync;
	
	protected WorldGuardPlugin wg;
	protected Player p;
	
	protected RegionManager rm;
	
	/**
	 * Make a new iRG command in sync
	 * @param p The player
	 * @param args The args of the command
	 */
	public AbstractImprovedRegionCommand(Player p, String[] args) {
		super(p, args);
		this.p = p;
		this.aSync = false;
		wg = plugin.getworldGuard();
		getRegionManager();
	}
	
	/**
	 * Make a new iRG command with possible aSync
	 * @param p The player
	 * @param args The args of the command
	 * @param aSync is aSync
	 */
	public AbstractImprovedRegionCommand(Player p, String[] args, boolean aSync) {
		super(p, args);
		this.p = p;
		this.aSync = aSync;
		wg = plugin.getworldGuard();
		getRegionManager();
	}
	
	/**
	 * Get the regionmanager of the world the player is currently in 
	 * @return the manager
	 */
	private void getRegionManager() {
		rm = wg.getRegionManager(p.getWorld());
	}
	
	@Override
	public boolean handle() { return true; }
	
	/**
	 * Handle the command
	 */
	public void handleIRG() {
		if (aSync) {
			Util.runASync(new Runnable() {
				
				@Override
				public void run() {
					try {
						action();
					} catch (me.naithantu.SlapHomebrew.Commands.Exception.CommandException e) {
						Util.badMsg(p, e.getMessage());
					}
				}
			});
		} else {
			try {
				action();
			} catch (me.naithantu.SlapHomebrew.Commands.Exception.CommandException e) {
				Util.badMsg(p, e.getMessage());
			}
		}
	}
	
	/**
	 * The action this command needs to take (Warning -- In A Sync)
	 */
	protected abstract void action() throws me.naithantu.SlapHomebrew.Commands.Exception.CommandException;
	
	/**
	 * Set the player's WE CUI selection
	 * @param region The region
	 */
	protected void setPlayerSelection(ProtectedRegion region) {
		try {
			WorldEditPlugin we = wg.getWorldEdit(); //Get the WE plugin
			World world = p.getWorld(); //Get the player's world
			
			//Set selection
			if (region instanceof ProtectedCuboidRegion) { //Region is cuboid
				ProtectedCuboidRegion cuboid = (ProtectedCuboidRegion) region;
	            Vector pt1 = cuboid.getMinimumPoint();
	            Vector pt2 = cuboid.getMaximumPoint();
	            CuboidSelection selection = new CuboidSelection(world, pt1, pt2);
	            we.setSelection(p, selection);
	            hMsg("Selected the cuboid region: " + region.getId());
			} else if (region instanceof ProtectedPolygonalRegion) { //Region is Poly
	            ProtectedPolygonalRegion poly2d = (ProtectedPolygonalRegion) region;
	            Polygonal2DSelection selection = new Polygonal2DSelection(
	                    world, poly2d.getPoints(),
	                    poly2d.getMinimumPoint().getBlockY(),
	                    poly2d.getMaximumPoint().getBlockY() );
	            we.setSelection(p, selection);
				hMsg("Selected the polygon region: " + region.getId());
			} else if (region instanceof GlobalProtectedRegion) { //Global region
				badMsg("You can't select the global region. That would cover the entire world!");
			} else { //Unkown type
				badMsg("Unkown region type, couldn't select..");
			}
		} catch (CommandException e) {
			badMsg(e.getMessage());
		}	
	}
	
	/**
	 * Send a 'bad' message to the sender of this command
	 * @param msg The message
	 */
	protected void badMsg(String msg) {
		Util.badMsg(p, msg);
	}
	
	@Override
	protected void hMsg(String msg) {
		p.sendMessage(ChatColor.YELLOW + "[iRG] " + msg);
	}
	
	/**
	 * Find the region with the highest priority on the player's location
	 * @return The Found Region
	 * @throws FoundRegionsException
	 */
	protected ProtectedRegion getHighestPriorityRegion() throws FoundRegionsException {
		ProtectedRegion region = null;
		ApplicableRegionSet regionSet = rm.getApplicableRegions(p.getLocation()); //Get all regions on location
		if (regionSet.size() == 0) throw new FoundRegionsException(); //If no regions found throw error
		
		HashSet<String> foundRegions = new HashSet<>();
		
		int highestPriority = -9001; boolean oneRegion = false;
		for (ProtectedRegion foundRegion : regionSet) { //Loop thru regions
			foundRegions.add(foundRegion.getId()); //Add to set
			int foundPriority = foundRegion.getPriority(); //Get the region's priority
			if (foundPriority > highestPriority) { //If found priority is higher then current high
				region = foundRegion; //Set region
				oneRegion = true; //Currently only one region
				highestPriority = foundPriority; //Set highest priority
			} else if (foundPriority == highestPriority) { //Another region found with the same priority
				oneRegion = false; //No longer only one region
			}
		}
		if (!oneRegion) throw new FoundRegionsException(foundRegions);
		return region;
	}
	
	/**
	 * Find the region with the highest priority on the player's location
	 * The player has to be a owner of the region.
	 * @param member also return region if owner
	 * @return The Found Region
	 * @throws FoundRegionsException
	 */
	protected ProtectedRegion getHighestOwnedPriorityRegion(boolean member) throws FoundRegionsException {
		ProtectedRegion region = null;
		ApplicableRegionSet regionSet = rm.getApplicableRegions(p.getLocation()); //Get all regions on location
		if (regionSet.size() == 0) throw new FoundRegionsException(); //If no regions found throw error

		HashSet<String> foundRegions = new HashSet<>();
		
		int highestPriority = -9001; boolean oneRegion = false;
		UUID playerUUID = p.getUniqueId();
		for (ProtectedRegion foundRegion : regionSet) { //Loop thru regions
			boolean isOwner = foundRegion.getOwners().contains(playerUUID);
			boolean isMember = foundRegion.getMembers().contains(playerUUID);
			
			if (!isMember && !isOwner) {
				continue;
			} else if (isMember && !member && !isOwner) {
				continue;
			}
			
			foundRegions.add(foundRegion.getId()); //Add to set
			
			int foundPriority = foundRegion.getPriority(); //Get the region's priority
			if (foundPriority > highestPriority) { //If found priority is higher then current high
				region = foundRegion; //Set region
				oneRegion = true; //Currently only one region
				highestPriority = foundPriority; //Set highest priority
			} else if (foundPriority == highestPriority) { //Another region found with the same priority
				oneRegion = false; //No longer only one region
			}
		}
		if (region == null) throw new FoundRegionsException(p.getName(), member); //No regions found that this player can acces
		if (!oneRegion) throw new FoundRegionsException(foundRegions); //Multiple regions with the same priority
		return region;
	}
	
	/**
	 * Check if a region ID is valid
	 * @param id The ID
	 * @throws IRGException if not valid
	 */
	protected void validateRegionID(String id) throws IRGException {
		if (!ProtectedRegion.isValidId(id) || id.equalsIgnoreCase("__global__")) throw new IRGException("'" + id + "' is not a valid region.");
	}
	
	/**
	 * Get the player's current WE CUI selection
	 * @return the selection or null
	 */
	protected Selection getSelection() {
		Selection selection = null;
		try {
			selection = wg.getWorldEdit().getSelection(p); //Get the selection of the player 
		} catch (CommandException e) {
			badMsg(e.getMessage());
			return null;
		}
		
		if (selection == null) { //Check if the player made a selection
			badMsg("You need to select an area first using WorldEdit!");
			return null;
		}
		
		return selection;
	}

	/**
	 * Create a new ProtectedRegion based on the player's selection
	 * @param id The id of the new region
	 * @return The new region
	 * @throws IRGException if selection is null, or unsupported regiontype
	 */
	protected ProtectedRegion createRegionFromSelection(String id) throws IRGException {
		Selection selection = getSelection();
		if (selection == null) throw new IRGException("Nothing is selected.");
		
        // Detect the type of region from WorldEdit
        if (selection instanceof Polygonal2DSelection) { //Polygon
            Polygonal2DSelection polySel = (Polygonal2DSelection) selection;
            int minY = polySel.getNativeMinimumPoint().getBlockY();
            int maxY = polySel.getNativeMaximumPoint().getBlockY();
            return new ProtectedPolygonalRegion(id, polySel.getNativePoints(), minY, maxY);
        } else if (selection instanceof CuboidSelection) { //Cuboid
            BlockVector min = selection.getNativeMinimumPoint().toBlockVector();
            BlockVector max = selection.getNativeMaximumPoint().toBlockVector();
            return new ProtectedCuboidRegion(id, min, max);
        } else { //Something else -> Not supported
            throw new IRGException("You can only use Polygon & Cuboid selections with WorldGuard");
        }
	}
	
	/**
	 * Save the regions
	 * @throws IRGException 
	 */
	protected void saveChanges() throws IRGException {
		try {
			rm.save();
		} catch (StorageException e) {
			throw new IRGException("Failed to save regions.. Something might be horrible wrong.. Exception: " + e.getMessage());
		}
	}
	
	/**
	 * Get a protected region
	 * @param name The name of the region
	 * @return 
	 * @throws IRGException
	 */
	protected ProtectedRegion getRegion(String name) throws IRGException {
		ProtectedRegion region = rm.getRegion(name);
		if (region == null) throw new IRGException("No region found with this name.");
		return region;
	}
	
}
