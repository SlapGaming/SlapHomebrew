package me.naithantu.SlapHomebrew.Commands.Staff.ImprovedRegion;

import me.naithantu.SlapHomebrew.SlapHomebrew;
import me.naithantu.SlapHomebrew.Commands.Exception.CommandException;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class HelpCommand extends AbstractImprovedRegionCommand {

	private boolean staff;
	
	public HelpCommand(SlapHomebrew plugin, Player p, String[] args, boolean staff) {
		super(p, args);
		this.staff = staff;
	}


	@Override
	protected void action() throws CommandException {
		int page = 1;
		if (staff && args.length > 1) {
			page = parseIntPositive(args[1]);
			if (page < 1) page = 1;
			else if (page > 4) page = 4;
		}
		
		msg(ChatColor.YELLOW + "==== " + ChatColor.GOLD + "Improved Region Commands" + ChatColor.YELLOW + " ====" + ChatColor.GRAY + " (<Needed> | [Optional])");
		
		if (staff) {
			switch (page) {
			case 1:
				sendHelpLine("addmember <Region ID> <Member 1> [Member 2]..", "Add 1 or more members");
				sendHelpLine("addowner <Region ID> <Owner 1> [Owner 2]..", "Add 1 or more owners");
				sendHelpLine("define <Region ID> [owner1] [owner2]..", "Define a new region (with 1 or more owners");
				sendHelpLine("delete <Region ID>", "Delete a region");
				break;
				
			case 2:
				sendHelpLine("flag <Region ID> <Flag> <Flag Parameter(s)>", "Add/Remove a flag");
				sendHelpLine("info [Region ID] [sel]", "Get the information about a region [Also Select it]");
				sendHelpLine("list <Player | All> [Page]", "Get a list of all regions (of a player)");
				sendHelpLine("setpriority <Region ID> <Priority>", "Change the priority of a region");
				break;
				
			case 3:
				sendHelpLine("redefine <Region ID>", "Redefine a region");
				sendHelpLine("removemember <Region ID> <Member 1> [Member 2]..", "Remove 1 or more members");
				sendHelpLine("removeowner <Region ID> <Owner 1> [Owner 2]..", "Remove 1 or more owners");
				sendHelpLine("select [Region ID]", "Select a region");
				break;
				
			case 4:
				sendHelpLine("teleport <Region ID> [sel]", "Teleport to a region");
				break;
			}
		} else {
			sendHelpLine("addmember <Member 1>.. [-region <Region ID>]", "Add members to your region (Will pick the one you're standing in if no region ID given).");
			sendHelpLine("info [Region ID]", "Get information about your region.");
			sendHelpLine("list", "Get a list of your regions (in this world)");
			sendHelpLine("removemember <Member 1>.. [-region <Region ID>]", "Remove members from your region (Will pick the one you're standing in if no region ID given).");
		}
		msg(ChatColor.YELLOW + "=========== " + ChatColor.GOLD + "Page " + page + " / " + (staff ? "4" : "1") + ChatColor.YELLOW + " ===========");
	}
	
	/**
	 * Send the info about a command
	 * @param command The command
	 * @param info The info
	 */
	private void sendHelpLine(String command, String info) {
		msg(ChatColor.GOLD + "/irg " + command + " : " + "\n" + "   " + ChatColor.WHITE + info);
	}
	

}
