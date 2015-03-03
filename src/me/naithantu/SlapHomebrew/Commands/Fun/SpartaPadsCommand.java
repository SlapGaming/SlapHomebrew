package me.naithantu.SlapHomebrew.Commands.Fun;

import me.naithantu.SlapHomebrew.Commands.AbstractCommand;
import me.naithantu.SlapHomebrew.Commands.Exception.CommandException;
import me.naithantu.SlapHomebrew.Commands.Exception.UsageException;
import me.naithantu.SlapHomebrew.Controllers.MessageStringer.MessageCombiner;
import me.naithantu.SlapHomebrew.Controllers.SpartaPads;
import me.naithantu.SlapHomebrew.Controllers.SpartaPads.SpartaPad;
import me.naithantu.SlapHomebrew.PlayerExtension.SlapPlayer;
import me.naithantu.SlapHomebrew.Util.Util;
import nl.stoux.SlapPlayers.Util.DateUtil;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SpartaPadsCommand extends AbstractCommand {

	private SpartaPads pads;
	
	public SpartaPadsCommand(CommandSender sender, String[] args) {
		super(sender, args);
	}

	@Override
	public boolean handle() throws CommandException {
		Player p = getPlayer(); //Check if a player
		testPermission("spartapads"); //Permission
		if (args.length == 0) return false;
		
		//Get pads
		pads = plugin.getSpartaPads();
		
		switch(args[0].toLowerCase()) {
		
		case "info": //Get more info about a SpartaPad
			SpartaPad foundSpartaPad = findSpartaPad(p, (args.length == 1 ? -1 : 1));
			//Output info
			hMsg("SpartaPad ID: " + ChatColor.GREEN + foundSpartaPad.getID() + ChatColor.WHITE + " created by " + ChatColor.GREEN + foundSpartaPad.getCreator());
			//	=> Created on
			msg(ChatColor.GOLD + "  \u2517\u25B6 " + ChatColor.WHITE + "Created on: " + ChatColor.GREEN + DateUtil.format("dd/MM/yyyy HH:mm", foundSpartaPad.getCreatedTimestamp()));
			//	=> From location
			Location from = foundSpartaPad.getPadBlockLocation();
			msg(ChatColor.GOLD + "  \u2517\u25B6 " + ChatColor.WHITE + "From location: "+ ChatColor.GREEN + "X: " + from.getX() + " | Y: " + from.getY() + " | Z: " + from.getZ());
			//	=> To location
			Location to = foundSpartaPad.getTargetLocation();
			msg(ChatColor.GOLD + "  \u2517\u25B6 " + ChatColor.WHITE + "To location: " + ChatColor.GREEN + "X: " + to.getX() + " | Y: " + to.getY() + " | Z: " + to.getZ());
			//	=> Multiplier
			msg(ChatColor.GOLD + "  \u2517\u25B6 " + ChatColor.WHITE + "Vector Multiplier: " + ChatColor.GREEN + foundSpartaPad.getMultiplier());
			//	=> Message
			msg(ChatColor.GOLD + "  \u2517\u25B6 " + ChatColor.WHITE + (foundSpartaPad.getMessage() == null ? "No launch message set" : "Message: " + ChatColor.GREEN + foundSpartaPad.getMessage()));
			break;
			
		//Pad Creation
		case "create": //Create a new SpartaPad | Usage: /SpartaPads create [ID] <Optional: Multiplier>
			if (args.length == 1) throw new UsageException("SpartaPads create [ID] <Multiplier>");
			
			//Check if not already creating a pad
			if (pads.isCreatingSpartaPad(p.getName())) throw new CommandException("You're already creating a SpartaPad! Finish or Cancel it!");
			
			//Check for Pads
			Block block = p.getLocation().getBlock();
			//	=> Check if correct type
			Material type = block.getType();
			if (type != Material.WOOD_PLATE && type != Material.STONE_PLATE && type != Material.IRON_PLATE && type != Material.GOLD_PLATE) throw new CommandException("You need to be on a pressure plate!");
			//	=> Check if no pad already on this location
			if (pads.isSpartaPad(block.getLocation())) throw new CommandException("There is already a SpartaPad on this location!");
			
			//Get ID
			String ID = Util.sanitizeYamlString(args[1]);
			
			//Check if one already exists with this name
			if (pads.isSpartaPad(ID)) throw new CommandException("A SpartaPad already exists with this ID.");
			if (pads.isCreatingSpartaPadID(ID)) throw new CommandException("A SpartaPad is already being created with this ID.");
			
			//Check multiplier
			Integer multiplierValue = null;
			if (args.length > 2) {
				multiplierValue = parseIntPositive(args[2]);
			}
			
			//Create pad
			pads.createPad(ID, p.getName(), block.getLocation(), multiplierValue);
			hMsg("Pad created with ID: " + ID + " - Go to the target location and finish it using " + ChatColor.BLUE + "/SpartaPads finish");
			break;
			
		case "finish": case "done": //Finish the creation of a SpartaPad | Usage: /SpartaPads finish
			//Check if creating a pad
			if (!pads.isCreatingSpartaPad(p.getName())) throw new CommandException("You're not creating a SpartaPad, so there's nothing to finish!");
			
			//Finish it
			boolean finished = pads.finishPadCreation(p.getName(), p.getLocation());
			if (!finished) throw new CommandException("The TargetLocation cannot be in a different world!");
			hMsg("Pad finished and now in use!");
			break;
		
		case "cancel": //Cancel the creation of creating a SpartaPad | Usage: /SpartaPads cancel
			//Check if creating a pad
			if (!pads.isCreatingSpartaPad(p.getName())) throw new CommandException("You're not creating a SpartaPad, so there's nothing to finish!");
			
			//Cancel it
			pads.cancelPadCreation(p.getName());
			hMsg("Pad creation canceled!");
			break;
			
			
		//Pad modification
		case "remove": case "delete": //Remove an existing SpartaPad | Usage: /SpartaPads remove <Optional: ID>
			//Find pad
			foundSpartaPad = findSpartaPad(p, (args.length == 1 ? -1 : 1));
			
			//Remove pad
			pads.removeSpartaPad(foundSpartaPad);
			hMsg("Removed SpartaPad: " + foundSpartaPad.getID());
			break;
			
		case "removemessage": case "removelaunchmessage": case "rmessage": //Remove the launch message | Usage: /SpartaPads removeMessage <Optional: ID>
			//Find pad
			foundSpartaPad = findSpartaPad(p, (args.length == 1 ? -1 : 1));
			
			//Check if there is a message
			if (foundSpartaPad.getMessage() == null) throw new CommandException("This SpartaPad has no launch message.");
			
			//Remove it
			foundSpartaPad.setMessage(null);
			hMsg("Removed message from SpartaPad: " + foundSpartaPad.getID());
			break;
			
		case "setmessage": case "setlaunchmessage": //Set the launchmessage | Usage: /SpartaPads setMessage <Optional: ID>
			//Find pad
			final SpartaPad foundMessageSpartaPad = findSpartaPad(p, (args.length == 1 ? -1 : 1));
			
			//Get slap player
			SlapPlayer slapPlayer = getSlapPlayer();
			
			//Message creator
			MessageCombiner combiner = new MessageCombiner(slapPlayer) {
				@Override
				public void finish() {
					if (getMessage().isEmpty()) {
						hMsg("Empty message, setting message canceled!");
					} else {
						foundMessageSpartaPad.setMessage(getMessage());
						colorize();
						hMsg("Message set for SpartaPad '" + foundMessageSpartaPad.getID() + "': " + getMessage());
					}					
				}
			};
			slapPlayer.setMessageCombiner(combiner);
			hMsg("Creating message for SpartaPad '" + foundMessageSpartaPad.getID() + "'. Type your message (Can be multiple lines!).");
			break;
			
		case "setmultiplier": //Set the multiplier of the Vector | Usage /SpartaPads setMultiplier [int 1 <=> 10] <Optional: ID>
			//Usage
			if (args.length == 1) throw new UsageException("SpartaPads setMultiplier [multiplier 1<=>10] <Optional: ID>");
			
			//Check int
			int multiplier = parseIntPositive(args[1]);
			if (multiplier > 10) throw new CommandException("The multiplier can only be between 1 and 10.");
			
			//find SpartaPad
			foundSpartaPad = findSpartaPad(p, (args.length == 2 ? -1 : 2));
			
			//Set multiplier
			foundSpartaPad.setMultiplier(multiplier);
			hMsg("Multiplier of SpartaPad '" + foundSpartaPad.getID() + "' set to " + multiplier);
			break;
			
		case "settarget": case "setpadtarget": case "settargetlocation": //Set the target location for a SpartaPad | Usage /SpartaPads setTarget [ID]
			//Usage
			if (args.length != 2) throw new UsageException("Spartapads setTarget [ID]");
			
			//Find pad
			foundSpartaPad = findSpartaPad(p, (args.length == 1 ? -1 : 1));
			
			//Check for worlds
			if (!p.getWorld().getName().equals(foundSpartaPad.getPadBlockLocation().getWorld().getName())) throw new CommandException("The target location cannot be in another world..");
			
			//Set target
			foundSpartaPad.setTargetLocation(p.getLocation());
			hMsg("Target location for SpartaPad '" + foundSpartaPad.getID() + "' has been set to your location!");
			break;
			
		//Help
		case "help": //Get help for this command | Usage /SpartaPads help
			int page = 1;
			if (args.length > 1) { //Gave a page
				page = parseInt(args[1]);
			}
			String[] messages;
			if (page <= 1) {
				messages = new String[]{
					ChatColor.YELLOW + "================= " + ChatColor.GOLD  + "SpartaPads Help" + ChatColor.YELLOW  + " =================",
					ChatColor.GOLD + "/SpartaPads info <ID> : " + ChatColor.WHITE + "Get the info about a pad.",
					ChatColor.GOLD + "/SpartaPads create [ID] <Multiplier> : " + ChatColor.WHITE + "Create a new pad at your location.",
					ChatColor.GOLD + "/SpartaPads finish : " + ChatColor.WHITE + "Finish the creation of a pad, Target = Your location.",
					ChatColor.GOLD + "/SpartaPads cancel : " + ChatColor.WHITE + "Cancel the creation of a pad.",
					ChatColor.GOLD + "/SpartaPads delete <ID> : " + ChatColor.WHITE + "Remove an existing pad.",
					ChatColor.YELLOW + "================== " + ChatColor.GOLD  + "Page 1 out of 2" + ChatColor.YELLOW  + " =================="
				};
			} else {
				messages = new String[]{
					ChatColor.YELLOW + "================= " + ChatColor.GOLD  + "SpartaPads Help" + ChatColor.YELLOW  + " =================",
					ChatColor.GOLD + "/SpartaPads setMessage <ID> : " + ChatColor.WHITE + "Prompts you to set the launch message.",
					ChatColor.GOLD + "/SpartaPads removeMessage <ID> : " + ChatColor.WHITE + "Remove the laucn message.",
					ChatColor.GOLD + "/SpartaPads setMultiplier [Multiplier] <ID> : " + ChatColor.WHITE + "Set the Vector multiplier.",
					ChatColor.GOLD + "/SpartaPads setTarget [ID] : " + ChatColor.WHITE + "Set the launch target.",
					ChatColor.YELLOW + "================== " + ChatColor.GOLD  + "Page 2 out of 2" + ChatColor.YELLOW  + " =================="
				};
			}
			p.sendMessage(messages); //Send messages
			break;
			
		default:
			return false;
		}		
		return true;
	}
	
	
	/**
	 * Find a SpartaPad
	 * @param p The player
	 * @param arg The argument which specified the ID. Put -1 to get the pad based on the location
	 * @return The SpartaPad
	 * @throws CommandException if no SpartaPad found
	 */
	private SpartaPad findSpartaPad(Player p, int arg) throws CommandException {
		SpartaPad foundSpartaPad;
		if (arg == -1) { //No extra arguments found => Check for SpartaPad on location
			//Check if one exists on this location
			Location blockLocation = p.getLocation().getBlock().getLocation();
			if (!pads.isSpartaPad(blockLocation)) throw new CommandException("No active SpartaPad found on this location");
			
			//Get SpartaPad
			foundSpartaPad = pads.getSpartaPad(blockLocation);
		} else {
			//Sanatize to yml String
			String id = Util.sanitizeYamlString(args[arg]);
			
			//Check if pad with this ID exists
			if (!pads.isSpartaPad(id)) throw new CommandException("No active SpartaPad found with ID: " + id);
			
			//Get SpartaPad
			foundSpartaPad = pads.getSpartaPad(id);
		}
		return foundSpartaPad;
	}

}
