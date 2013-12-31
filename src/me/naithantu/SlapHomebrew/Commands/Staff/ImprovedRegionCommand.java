package me.naithantu.SlapHomebrew.Commands.Staff;

import me.naithantu.SlapHomebrew.SlapHomebrew;
import me.naithantu.SlapHomebrew.Commands.AbstractCommand;
import me.naithantu.SlapHomebrew.Commands.Exception.CommandException;
import me.naithantu.SlapHomebrew.Commands.Exception.ErrorMsg;
import me.naithantu.SlapHomebrew.Commands.Staff.ImprovedRegion.AbstractImprovedRegionCommand;
import me.naithantu.SlapHomebrew.Commands.Staff.ImprovedRegion.AddMemberCommand;
import me.naithantu.SlapHomebrew.Commands.Staff.ImprovedRegion.AddOwnerCommand;
import me.naithantu.SlapHomebrew.Commands.Staff.ImprovedRegion.DefineCommand;
import me.naithantu.SlapHomebrew.Commands.Staff.ImprovedRegion.DeleteCommand;
import me.naithantu.SlapHomebrew.Commands.Staff.ImprovedRegion.FlagCommand;
import me.naithantu.SlapHomebrew.Commands.Staff.ImprovedRegion.HelpCommand;
import me.naithantu.SlapHomebrew.Commands.Staff.ImprovedRegion.InfoCommand;
import me.naithantu.SlapHomebrew.Commands.Staff.ImprovedRegion.ListCommand;
import me.naithantu.SlapHomebrew.Commands.Staff.ImprovedRegion.PriorityCommand;
import me.naithantu.SlapHomebrew.Commands.Staff.ImprovedRegion.RedefineCommand;
import me.naithantu.SlapHomebrew.Commands.Staff.ImprovedRegion.RemoveMemberCommand;
import me.naithantu.SlapHomebrew.Commands.Staff.ImprovedRegion.RemoveOwnerCommand;
import me.naithantu.SlapHomebrew.Commands.Staff.ImprovedRegion.SelectCommand;
import me.naithantu.SlapHomebrew.Commands.Staff.ImprovedRegion.TeleportCommand;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import ru.tehkode.permissions.PermissionUser;
import ru.tehkode.permissions.bukkit.PermissionsEx;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;

public class ImprovedRegionCommand extends AbstractCommand {
	
	private static WorldGuardPlugin wg;
	
	private Player p;
	
	
	public ImprovedRegionCommand(CommandSender sender, String[] args, SlapHomebrew plugin) {
		super(sender, args, plugin);
		if (wg == null) {
			wg = plugin.getworldGuard();
		}
	}
	

	@Override
	public boolean handle() throws CommandException { //Handler
		p = getPlayer();
		if (args.length == 0) return false; //Usage
				
		//iRg command
		AbstractImprovedRegionCommand iRgCommand = null;
		
		switch (args[0].toLowerCase()) {
		case "select": case "sel": case "s": //Selects a WG
			testIRGPermission(Perm.select);
			iRgCommand = new SelectCommand(plugin, p, args);
			break;
			
		case "info": case "information": case "i": //Info about the WG
			testIRGPermission(Perm.infoOwn);
			iRgCommand = new InfoCommand(plugin, p, args, hasPermission(Perm.infoAll));
			break;
			
		case "define": case "def": case "create": case "d": //Define a region
			testIRGPermission(Perm.define);
			iRgCommand = new DefineCommand(plugin, p, args);
			break;
			
		case "redefine": case "red": case "redef": //Redefine a region
			testIRGPermission(Perm.redefine);
			iRgCommand = new RedefineCommand(plugin, p, args);
			break;
			
		case "setpriority": case "setp": case "setpri": case "priority": case "pri": //Set the priority of a region
			testIRGPermission(Perm.priority);
			iRgCommand = new PriorityCommand(plugin, p, args);
			break;
			
		case "flag": case "f": //Give a region a flag or remove a flag
			testIRGPermission(Perm.flagMod);
			iRgCommand = new FlagCommand(plugin, p, args, hasPermission(Perm.flagAll));
			break;
			
		case "addmember": case "addm": case "am": case "member": //Add a member to a region
			testIRGPermission(Perm.addMemberOwn);
			iRgCommand = new AddMemberCommand(plugin, p, args, hasPermission(Perm.addMemberAll));
			break;
			
		case "removemember": case "remmember": case "removem": case "rmember": case "remm": case "rm": //Remove a member from a region
			testIRGPermission(Perm.removeMemberOwn);
			iRgCommand = new RemoveMemberCommand(plugin, p, args, hasPermission(Perm.removeMemberAll));
			break;
			
		case "addowner": case "addo": case "ao": case "owner": //Add a owner to a region
			testIRGPermission(Perm.addOwner);
			iRgCommand = new AddOwnerCommand(plugin, p, args);
			break;
			
		case "removeowner": case "remowner": case "remo": case "removeo": case "rowner": case "ro": //Remove a owner of a region
			testIRGPermission(Perm.removeOwner);
			iRgCommand = new RemoveOwnerCommand(plugin, p, args);
			break;
			
		case "delete": case "del": case "remove": case "rem": //Remove a region
			testIRGPermission(Perm.delete);
			iRgCommand = new DeleteCommand(plugin, p, args);
			break;
			
		case "list": //Get a list of all the regions
			testIRGPermission(Perm.listOwn);
			iRgCommand = new ListCommand(plugin, p, args, hasPermission(Perm.listAll));
			break;
			
		case "tp": case "teleport": //Teleport to the WG
			testIRGPermission(Perm.teleport);
			iRgCommand = new TeleportCommand(plugin, p, args);
			break;
			
		case "seen": //Get the /seen of the players on the region
			testIRGPermission(Perm.seen);
			throw new CommandException(ErrorMsg.notSupportedYet);
			//Break;
			
		case "group": //Get the ranks of the players on the region
			testIRGPermission(Perm.group);
			throw new CommandException(ErrorMsg.notSupportedYet);
			//Break;
			
		case "copy": case "copysel": case "copyselection": //Copy the selection of a different player
			testIRGPermission(Perm.copySelection);
			throw new CommandException(ErrorMsg.notSupportedYet);
			//Break;
			
		case "togglerg": case "toggleirg": case "toggleregion": //Toggle /rg <-> /irg for this player
			testIRGPermission(Perm.toggleRegion);
			PermissionUser pexUser = PermissionsEx.getUser(p);
			if (pexUser != null) {
				if (pexUser.has("-irg.regionoverride")) {
					pexUser.removePermission("-irg.regionoverride");
					hMsg("/region will not be overriden with /irg");
				} else {
					pexUser.addPermission("-irg.regionoverride");
					hMsg("Region override is now disabled for you.");
				}
			} else {
				hMsg("You are not a PEX User?");
			}
			break;
			
		case "help":
			testIRGPermission(Perm.help);
			iRgCommand = new HelpCommand(plugin, p, args, hasPermission(Perm.helpStaff));
			break;
			
		default:
			return false;
		}
		
		if (iRgCommand != null) { //Handle command if not null
			iRgCommand.handleIRG();
		}
		return true;
	}

	/**
	 * Permission ENUM
	 */
	public enum Perm {
		/**	Allows you to select a region */
		select("irg.select"),
		
		/**	Get the information about a region you own */
		infoOwn("irg.info"),
		
		/** Get the information about any region */
		infoAll("irg.info.all"),
		
		/** Allows you to define a region */
		define("irg.define"),
		
		/** Allows you to redefine a region */
		redefine("irg.redefine"),
		
		/** Allows you to set the priority of a region */
		priority("irg.priority"),
		
		/** Allows you to set a certain set of flags on a region */
		flagMod("irg.flag"),
		
		/** Allows you to set all flags on a region */
		flagAll("irg.flag.all"),
		
		/** Allows you to add a member to a region (possible without giving the region name) */
		addMemberOwn("irg.addmember"),
		
		/** Allows you to add a member to a region (always with the region name) */
		addMemberAll("irg.addmember.all"),
		
		/** Allows you to remove a member from a region you own (possible without giving region name) */
		removeMemberOwn("irg.removemember"),
		
		/** Allows you to add a member to a region (always with the region name) */
		removeMemberAll("irg.removemember.all"),
		
		/** Allows you to add an owner to a region */
		addOwner("irg.addowner"),
		
		/** Allows you to remove an owner from a region */
		removeOwner("irg.removeowner"),
		
		/** Allows you to get a list of all your regions */
		listOwn("irg.list"),
		
		/** Allows you to get a list of all regions */
		listAll("irg.list.all"),
		
		/** Allows you to delete a region */
		delete("irg.delete"),
		
		/** Gets you a list of all the owners & members with their PermissionGroups */
		group("irg.group"),
		
		/** Gets you a list of all the owners & members with their last online time */
		seen("irg.seen"),
		
		/** Copy the selection another staff member has */
		copySelection("irg.copyselection"),
		
		/** Allows you to teleport to a region */
		teleport("irg.teleport"),
		
		/** Toggle between /region and /improvedregion */
		toggleRegion("irg.toggleregion"),
		
		/** Display the help page for members */
		help("irg.help"),
		
		/** Display the help page for staff */
		helpStaff("irg.help.staff");
		
		private String permission;
		
		private Perm(String permission) {
			this.permission = permission;
		}
		
		@Override
		public String toString() {
			return permission;
		}
		
	}
	
	/**
	 * Check a permission
	 * @param perm The permission
	 * @return allowed
	 */
	public boolean hasPermission(Perm perm) {
		return sender.hasPermission(perm.toString());
	}
	
	/**
	 * Check if the {@link CommandSender} has a certain permission
	 * If not throw a no permission error
	 * @param p The permission
	 * @return is allowed
	 * @throws CommandException 
	 */
	public boolean testIRGPermission(Perm perm) throws CommandException {
		boolean returnBool = sender.hasPermission(perm.toString());
		if (returnBool == false) throw new CommandException(ErrorMsg.noPermission);
		return returnBool;
	}	
	
}