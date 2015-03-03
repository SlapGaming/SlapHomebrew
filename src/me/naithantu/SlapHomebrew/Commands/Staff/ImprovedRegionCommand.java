package me.naithantu.SlapHomebrew.Commands.Staff;

import com.sk89q.worldguard.protection.flags.DefaultFlag;
import com.sk89q.worldguard.protection.flags.Flag;
import me.naithantu.SlapHomebrew.Commands.AbstractCommand;
import me.naithantu.SlapHomebrew.Commands.Exception.CommandException;
import me.naithantu.SlapHomebrew.Commands.Exception.ErrorMsg;
import me.naithantu.SlapHomebrew.Commands.Staff.ImprovedRegion.*;
import me.naithantu.SlapHomebrew.PlayerExtension.PlayerControl;
import me.naithantu.SlapHomebrew.PlayerExtension.SlapPlayer;
import me.naithantu.SlapHomebrew.SlapHomebrew;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class ImprovedRegionCommand extends AbstractCommand {
		
	private Player p;
	
	public ImprovedRegionCommand(CommandSender sender, String[] args) {
		super(sender, args);
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
			iRgCommand = new SelectCommand(p, args);
			break;
			
		case "info": case "information": case "i": //Info about the WG
			testIRGPermission(Perm.infoOwn);
			iRgCommand = new InfoCommand(p, args, hasPermission(Perm.infoAll));
			break;
			
		case "define": case "def": case "create": case "d": //Define a region
			testIRGPermission(Perm.define);
			iRgCommand = new DefineCommand(p, args);
			break;
			
		case "redefine": case "red": case "redef": //Redefine a region
			testIRGPermission(Perm.redefine);
			iRgCommand = new RedefineCommand(p, args);
			break;
			
		case "setpriority": case "setp": case "setpri": case "priority": case "pri": //Set the priority of a region
			testIRGPermission(Perm.priority);
			iRgCommand = new PriorityCommand(p, args);
			break;
			
		case "flag": case "f": //Give a region a flag or remove a flag
			testIRGPermission(Perm.flagMod);
			iRgCommand = new FlagCommand(p, args, hasPermission(Perm.flagAll));
			break;
			
		case "addmember": case "addm": case "am": case "member": //Add a member to a region
			testIRGPermission(Perm.addMemberOwn);
			iRgCommand = new AddMemberCommand(p, args, hasPermission(Perm.addMemberAll));
			break;
			
		case "removemember": case "remmember": case "removem": case "rmember": case "remm": case "rm": //Remove a member from a region
			testIRGPermission(Perm.removeMemberOwn);
			iRgCommand = new RemoveMemberCommand(p, args, hasPermission(Perm.removeMemberAll));
			break;
			
		case "addowner": case "addo": case "ao": case "owner": //Add a owner to a region
			testIRGPermission(Perm.addOwner);
			iRgCommand = new AddOwnerCommand(p, args);
			break;
			
		case "removeowner": case "remowner": case "remo": case "removeo": case "rowner": case "ro": //Remove a owner of a region
			testIRGPermission(Perm.removeOwner);
			iRgCommand = new RemoveOwnerCommand(p, args);
			break;
			
		case "delete": case "del": case "remove": case "rem": //Remove a region
			testIRGPermission(Perm.delete);
			iRgCommand = new DeleteCommand(p, args);
			break;
			
		case "list": //Get a list of all the regions
			testIRGPermission(Perm.listOwn);
			iRgCommand = new ListCommand(p, args, hasPermission(Perm.listAll));
			break;
			
		case "tp": case "teleport": //Teleport to the WG
			testIRGPermission(Perm.teleport);
			iRgCommand = new TeleportCommand(p, args);
			break;
			
		case "copy": case "copysel": case "copyselection": //Copy the selection of a different player
			testIRGPermission(Perm.copySelection);
			throw new CommandException(ErrorMsg.notSupportedYet);
			//Break;
			
		case "togglerg": case "toggleirg": case "toggleregion": //Toggle /rg <-> /irg for this player
			testIRGPermission(Perm.toggleRegion);
			SlapPlayer sp = PlayerControl.getPlayer(p); //Get the SlapPlayer
			boolean hasToggled = sp.hasToggledRegion(); //Check if toggled
			sp.setToggledRegion(!hasToggled); //Revert it
			hMsg((hasToggled ? "Untoggled" : "Toggled") + " /region"); //Message
			break;
			
		case "group": case "seen": case "seengroup": case "groupseen": //Get Group & Last seen of owners & members
			testIRGPermission(Perm.seengroup);
			iRgCommand = new SeenGroupCommand(p, args);
			break;
			
		case "help":
			testIRGPermission(Perm.help);
			iRgCommand = new HelpCommand(p, args, hasPermission(Perm.helpStaff));
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
		
		/** Gets you a list of all the owners & members with their PermissionGroups & Last seen */
		seengroup("irg.seengroup"),
		
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
	
	/**
	 * TabComplete on this command
	 * @param sender The sender of the command
	 * @param args given arguments
	 * @return List of options
	 */
	public static List<String> tabComplete(CommandSender sender, String[] args) {
		boolean staff = sender.hasPermission(Perm.helpStaff.toString());
		if (args.length == 1) { //No command given
			List<String> list;
			if (!staff) { //No staff commands
				list = createNewList("list", "info", "addmember", "removemember");
			} else { //All commands (Without OP ones)
				list = createNewList("addmember", "addowner", "define", "delete", "flag", "seengroup", "info", "list", "setpriority", "redefine", "removemember", "removeowner", "select", "teleport");
			}
			
			//Filter results
			filterResults(list, args[0]);
			return list;
		} else if (staff) { //Staff commands 
			switch (args[0].toLowerCase()) {
			case "select": case "sel": case "s": //Selects a WG
				if (args.length == 2) {
					return listRegions(sender, args[1]);
				} else if (args.length == 3) {
					return createNewList("sel");
				} else {
					return createEmptyList();
				}
				
			case "flag": case "f": //Give a region a flag or remove a flag
				if (args.length == 2) {
					return listRegions(sender, args[1]);
				} else if (args.length == 3) { //The flag
					if (sender.hasPermission(Perm.flagAll.toString())) { //Return all flags
						List<String> flags = createEmptyList();
						for (Flag<?> f : DefaultFlag.getFlags()) { //Get all flags
							flags.add(f.getName());
						}
						return filterResults(flags, args[2]); //Filter flags
					} else {
						return filterResults( //REturn mod flags
							createNewList("snowfall", "snowmelt", "iceform", "icemelt", "mushroomgrowth", "grassgrowth", "myceliumspread", "vinegrowth"),
							args[2]
						);
					}
				} else if (args.length == 4) { //Last argument
					return filterResults( //Return filtered options
						createNewList("none", "remove", "delete", "allow", "true", "on", "deny", "false", "off"),
						args[3]
					);
				} else {
					return null;
				}
				
			case "info": case "information": case "i": //Info about the WG
			case "redefine": case "red": case "redef": //Redefine a region
			case "setpriority": case "setp": case "setpri": case "priority": case "pri": //Set the priority of a region
			case "delete": case "del": case "remove": case "rem": //Remove a region
			case "tp": case "teleport": //Teleport to the WG
			case "group": case "seen": case "seengroup": case "groupseen": //Get Group & Last seen of owners & members
				if (args.length == 2) {
					return listRegions(sender, args[1]); //Return regions
				} else {
					return null;
				}
				
			case "addmember": case "addm": case "am": case "member": //Add a member to a region
			case "removemember": case "remmember": case "removem": case "rmember": case "remm": case "rm": //Remove a member from a region
			case "addowner": case "addo": case "ao": case "owner": //Add a owner to a region
			case "removeowner": case "remowner": case "remo": case "removeo": case "rowner": case "ro": //Remove a owner of a region
				if (args.length == 2) {
					return listRegions(sender, args[1]);
				} else {
					return listAllPlayers(sender.getName());
				}
				
			case "list": //Get a list of all the regions
				if (args.length == 2) { //First argument
					List<String> players = listAllPlayers();
					players.add(0, "all");
					return filterResults(players, args[1]);
				} else {
					return createEmptyList();
				}
				
				
			case "togglerg": case "toggleirg": case "toggleregion": //Toggle /rg <-> /irg for this player
				return createEmptyList();
				
			case "copy": case "copysel": case "copyselection": //Copy the selection of a different player
			case "define": case "def": case "create": case "d": //Define a region
				return null;
				
			}
		}
		return null;
	}
	
	/**
	 * List filtered results of all regions
	 * or if the arg is 3 chars or shorter player names
	 * @param sender The sender
	 * @param arg The given argument to filter on
	 * @return filtered list
	 */
	private static List<String> listRegions(CommandSender sender, String arg) {
		if (arg.length() > 3 && sender instanceof Player) { //If atleast some letters given
			return filterResults( //Filter results
				new ArrayList<String>( //Create new list with all regions in that world
					SlapHomebrew.getInstance().getworldGuard().getRegionManager(((Player) sender).getWorld()).getRegions().keySet()
				),
				arg
			);
		} else {
			return null;
		}
	}
	
	
}
