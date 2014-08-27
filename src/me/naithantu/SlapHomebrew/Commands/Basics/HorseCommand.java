package me.naithantu.SlapHomebrew.Commands.Basics;

import java.util.List;

import me.naithantu.SlapHomebrew.Commands.AbstractCommand;
import me.naithantu.SlapHomebrew.Commands.Exception.CommandException;
import me.naithantu.SlapHomebrew.Controllers.Horses;
import me.naithantu.SlapHomebrew.PlayerExtension.UUIDControl;
import me.naithantu.SlapHomebrew.Util.Util;

import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Horse.Variant;
import org.bukkit.entity.Player;

import com.earth2me.essentials.Essentials;
import com.earth2me.essentials.User;

public class HorseCommand extends AbstractCommand {

	private Horses horses;
	private Horse horse;
	private String horseID;
	private Player rider;
	
	private Essentials ess;
	
	public HorseCommand(CommandSender sender, String[] args) {
		super(sender, args);
		horses = plugin.getHorses();
		ess = plugin.getEssentials();
	}

	@Override
	public boolean handle() throws CommandException {
		if (args.length == 2 && args[0].equalsIgnoreCase("addhorses")) {
			testPermission("addhorses");
			UUIDControl.UUIDProfile offPlayer = getOfflinePlayer(args[1]);
			horses.playerDonatedForHorses(offPlayer.getCurrentName());
		}
		
		Player player = getPlayer(); //Get player
		testPermission("horse"); //Test perm
		if (args.length == 0) return false; //Check usage
		
		if (args[0].toLowerCase().equals("help")) { //Help command
			int page = 1;
			if (args.length > 1) { //Gave a page
				page = parseInt(args[1]);
			}
			String[] messages;
			if (page <= 1) {
				messages = new String[]{
					ChatColor.YELLOW + "==================== " + ChatColor.GOLD  + "Horse Help" + ChatColor.YELLOW  + " ====================",
					ChatColor.GOLD + "/horse allow <player> : " + ChatColor.WHITE + "allow another player to use your horse.",
					ChatColor.GOLD + "/horse deny <player> : " + ChatColor.WHITE + "deny an allowed player from futher using your horse.",
					ChatColor.GOLD + "/horse changeowner <player> : " + ChatColor.WHITE + "give all rights over the horse to the specified player. You will LOSE ALL rights!",
					ChatColor.GOLD + "/horse claim : " + ChatColor.WHITE + "claim a tamed horse that is not auto claimed.",
					ChatColor.YELLOW + "================== " + ChatColor.GOLD  + "Page 1 out of 3" + ChatColor.YELLOW  + " =================="
				};
			} else if (page == 2) {
				messages = new String[]{
					ChatColor.YELLOW + "==================== " + ChatColor.GOLD  + "Horse Help" + ChatColor.YELLOW  + " ====================",
					ChatColor.GOLD + "/horse info : " + ChatColor.WHITE + "get the owner's name",
					ChatColor.GOLD + "/horse allowed : " + ChatColor.WHITE + "get a list of all the players who are allowed to use this horse.",
					ChatColor.GOLD + "/horse mutate [zombie/skeleton] : " + ChatColor.WHITE + "mutates your horse into an zombie/skeleton. [VIP/Donators only]",
					ChatColor.GOLD + "/horse mutate info : " + ChatColor.WHITE + "gets your mutated horses stats.",
					ChatColor.YELLOW + "================== " + ChatColor.GOLD  + "Page 2 out of 3" + ChatColor.YELLOW  + " =================="
				};
			} else {
				messages = new String[]{
					ChatColor.YELLOW + "==================== " + ChatColor.GOLD  + "Horse Help" + ChatColor.YELLOW  + " ====================",
					ChatColor.GOLD + "/horse public : " + ChatColor.WHITE + "allow everyone on this horse (while staying the owner).",
					ChatColor.GOLD + "/horse private : " + ChatColor.WHITE + "allow no-one on this horse.",
					ChatColor.YELLOW + "================== " + ChatColor.GOLD  + "Page 3 out of 3" + ChatColor.YELLOW  + " =================="
				};
			}
			player.sendMessage(messages); //Send messages
			return true;
		}
						
		switch (args[0].toLowerCase()) {
		case "claim":
			if (!isOnHorse()) {
				return true;
			}
			horses.claimHorse(horse, (Player)sender);
			break;
		case "owner": case "changeowner":
			if (!isOwnerOnHorse()) {
				return true;
			}
			if (horse.getVariant() == Variant.SKELETON_HORSE || horse.getVariant() == Variant.UNDEAD_HORSE) {
				rider.sendMessage(ChatColor.RED + "This horse will not accept a different owner (you're too awesome).");
				return true;
			}
			if (args.length > 1) {
				Player newOwner = plugin.getServer().getPlayerExact(args[1]);
				if (newOwner != null) {
					if (rider.getName().equals(newOwner.getName())) {
						Util.badMsg(sender, "You are already the owner of this horse.");
					} else {
						horses.changeOwner(horse, rider, newOwner);
					}
				} else {
					Util.badMsg(sender, "This player doesn't exist/is not online.");
				}
			} else {
				Util.badMsg(sender, "You need to enter the name of the new owner.");
			}
			break;
		case "allow":
			if (!isOwnerOnHorse()) {
				return true;
			}
			if (args.length > 1) {
				User allowedPlayer = ess.getUserMap().getUser(args[1]);
				if (allowedPlayer != null) {
					if (rider.getName().equals(allowedPlayer.getName())) {
						Util.badMsg(sender, "You are the owner of this horse.");
					} else {
						horses.allowOnHorse(horseID, rider, allowedPlayer.getName());
					}
				} else {
					Util.badMsg(sender, "This player doesn't exist");
				}
			} else {
				Util.badMsg(sender, "You need to enter the name of the allowed player.");
			}
			break;
		case "deny":
			if (!isOwnerOnHorse()) {
				return true;
			}
			if (args.length > 1) {
				User allowedPlayer = ess.getUserMap().getUser(args[1]);
				if (allowedPlayer != null) {
					if (rider.getName().equals(allowedPlayer.getName())) {
						Util.badMsg(sender, "You are the owner of this horse.");
					} else {
						horses.denyOnHorse(horseID, rider, allowedPlayer.getName());
					}
				} else {
					Util.badMsg(sender, "This player doesn't exist");
				}
			} else {
				Util.badMsg(sender, "You need to enter the name of the denied player.");
			}
			break;
		case "allowed":
			if (!isOwnerOnHorse()) {
				return true;
			}
			List<String> allowedPlayers = horses.getAllowedPlayers(horseID);
			if (allowedPlayers == null) {
				hMsg("There are no other players allowed on this horse.");
			} else {
				int xCount = 1; int listSize = allowedPlayers.size();
				String allowedPlayersString = "";
				for (String allowedPlayer : allowedPlayers) {
					if (xCount == 1) {
						allowedPlayersString = allowedPlayer;
					} else if (xCount == listSize) {
						allowedPlayersString = allowedPlayersString + " & " + allowedPlayer;
					} else {
						allowedPlayersString = allowedPlayersString + ", " + allowedPlayer;
					}
					xCount++;
				}
				hMsg("You have allowed the following people on this horse: " + allowedPlayersString);
			}
			break;
		case "info":
			if (isOnHorse(false)) {
				horses.infoHorse(rider, horseID);
			} else {
				horses.issuedInfoCommand(rider.getName());
				rider.sendMessage(ChatColor.GOLD + "[SLAP] " + ChatColor.WHITE + "Right click on the horse you want info about");
			}
			break;
		case "mutate":
			if (args.length > 1) {
				switch (args[1].toLowerCase()) {
				case "zombie": case "skeleton":
					if (isOwnerOnHorse()) {
						horses.createSpecialHorseCommand(horse, rider, args[1].toLowerCase());
					}
					break;
				case "info":
					if (Util.testPermission(sender, "staff")) {
						if (args.length == 3) {
							User target = ess.getUserMap().getUser(args[2]);
							if (target != null) {
								horses.specialHorsesStats(target.getName(), (Player)sender);
								return true;
							}
						}
					}
					horses.specialHorsesStats((Player)sender);
					break;
				default: 
					return false;
				}
			} else {
				return false;
			}
			break;
		case "unclaim":
			Util.badMsg(sender, "This command isn't supported yet.");
			break;
		case "public":
			if (!isOwnerOnHorse()) {
				return true;
			}
			if (horses.setHorsePublic(horse, rider)) {
				sender.sendMessage(Util.getHeader() + "The horse can now be accessed by anyone! Be careful.");
			}
			break;
		case "private":
			if (!isOwnerOnHorse()) {
				return true;
			}
			if (horses.setHorsePrivate(horse, rider)) {
				sender.sendMessage(Util.getHeader() + "The horse is now private and can only be accessed by you!");
			}
			break;
		default:
			return false;
		}
		
		return true;
	}
	
	private boolean isOnHorse(){
		return isOnHorse(true);
	}

	private boolean isOnHorse(boolean message){
		rider = (Player) sender;
		Entity vehicle = rider.getVehicle();
		
		if (vehicle == null || vehicle.getType() != EntityType.HORSE) {
			if (message) {
				Util.badMsg(sender, "You need to be on a horse to do that!");
			}
			return false;
		}
		
		horse = (Horse) vehicle;	
		horseID = horse.getUniqueId().toString();
		return true;
	}
	
	private boolean isOwner(){
		if (horses.hasOwner(horseID)) {
			if (horses.getOwner(horseID).equals(sender.getName())) {
				return true;
			} else {
				Util.badMsg(sender, "You are not the owner of this horse.");
			}
		} else {
			hMsg("This horse doesn't have an owner yet! use /horse claim (if it's yours)");
			
		}
		return false;
	}
	
	private boolean isOwnerOnHorse(){
		if (!isOnHorse()) {
			return false;
		} else {
			if (!isOwner()) {
				return false;
			} else {
				return true;
			}
		}
	}
	
}
