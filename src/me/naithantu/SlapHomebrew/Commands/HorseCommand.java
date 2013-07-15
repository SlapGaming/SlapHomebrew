package me.naithantu.SlapHomebrew.Commands;

import java.util.List;

import me.naithantu.SlapHomebrew.Horses;
import me.naithantu.SlapHomebrew.SlapHomebrew;

import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Horse.Variant;
import org.bukkit.entity.Player;

public class HorseCommand extends AbstractCommand {

	private static Horses horses;
	private Horse horse;
	private String horseID;
	private Player rider;
	
	protected HorseCommand(CommandSender sender, String[] args, SlapHomebrew plugin) {
		super(sender, args, plugin);
		if (horses == null) {
			horses = plugin.getHorses();
		}
	}

	@Override
	public boolean handle() {
		
		if (sender.hasPermission("slaphomebrew.addhorses")) {
			if (args.length  == 2) {
				if (args[0].toLowerCase().equals("addhorses")) {
					OfflinePlayer offPlayer = plugin.getServer().getOfflinePlayer(args[1]);
					String name;
					if (offPlayer.getPlayer() != null) {
						name = offPlayer.getPlayer().getName();
					} else {
						name = offPlayer.getName();
					}
					if (offPlayer != null) {
						horses.playerDonatedForHorses(name);
					}
					return true;
				}
			}
		}
		
		
		if (!(sender instanceof Player)) {
			this.badMsg(sender, "You need to be in-game to do that!");
			return true;
		}
		
		if (args.length == 0) {
			return false;
		}
		
		if (!testPermission(sender, "horse")) {
			this.noPermission(sender);
			return true;
		}
		
		if (args[0].toLowerCase().equals("help")) {
			int page = 1;
			if (args.length > 1) {
				if (args[1].equals("2")) {
					page = 2;
				}
			}
			if (page == 1) {
				sender.sendMessage(ChatColor.YELLOW + "==================== " + ChatColor.GOLD  + "Horse Help" + ChatColor.YELLOW  + " ====================");
				sender.sendMessage(ChatColor.GOLD + "/horse allow <player> : " + ChatColor.WHITE + "allow another player to use your horse.");
				sender.sendMessage(ChatColor.GOLD + "/horse deny <player> : " + ChatColor.WHITE + "deny an allowed player from futher using your horse.");
				sender.sendMessage(ChatColor.GOLD + "/horse changeowner <player> : " + ChatColor.WHITE + "give all rights over the horse to the specified player. You will LOSE ALL rights!");
				sender.sendMessage(ChatColor.GOLD + "/horse claim : " + ChatColor.WHITE + "claim a tamed horse that is not auto claimed.");
				sender.sendMessage(ChatColor.YELLOW + "================== " + ChatColor.GOLD  + "Page 1 out of 2" + ChatColor.YELLOW  + " ==================");
			} else {
				sender.sendMessage(ChatColor.YELLOW + "==================== " + ChatColor.GOLD  + "Horse Help" + ChatColor.YELLOW  + " ====================");
				sender.sendMessage(ChatColor.GOLD + "/horse info : " + ChatColor.WHITE + "get the owner's name");
				sender.sendMessage(ChatColor.GOLD + "/horse allowed : " + ChatColor.WHITE + "get a list of all the players who are allowed to use this horse.");
				sender.sendMessage(ChatColor.GOLD + "/horse special [undead/skeleton] : " + ChatColor.WHITE + "mutates your horse into an undead/skeleton. [VIP/Donators only]");
				sender.sendMessage(ChatColor.GOLD + "/horse special info : " + ChatColor.WHITE + "get your current special horses stats.");
				sender.sendMessage(ChatColor.YELLOW + "================== " + ChatColor.GOLD  + "Page 2 out of 2" + ChatColor.YELLOW  + " ==================");
			}
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
						badMsg(sender, "You are already the owner of this horse.");
					} else {
						horses.changeOwner(horse, rider, newOwner);
					}
				} else {
					badMsg(sender, "This player doesn't exist/is not online.");
				}
			} else {
				badMsg(sender, "You need to enter the name of the new owner.");
			}
			break;
		case "allow":
			if (!isOwnerOnHorse()) {
				return true;
			}
			if (args.length > 1) {
				OfflinePlayer allowedPlayer = plugin.getServer().getOfflinePlayer(args[1]);
				if (allowedPlayer != null) {
					if (rider.getName().equals(allowedPlayer.getName())) {
						badMsg(sender, "You are the owner of this horse.");
					} else {
						horses.allowOnHorse(horseID, rider, allowedPlayer.getName());
					}
				} else {
					badMsg(sender, "This player doesn't exist");
				}
			} else {
				badMsg(sender, "You need to enter the name of the allowed player.");
			}
			break;
		case "deny":
			if (!isOwnerOnHorse()) {
				return true;
			}
			if (args.length > 1) {
				OfflinePlayer allowedPlayer = plugin.getServer().getOfflinePlayer(args[1]);
				if (allowedPlayer != null) {
					if (rider.getName().equals(allowedPlayer.getName())) {
						badMsg(sender, "You are the owner of this horse.");
					} else {
						horses.denyOnHorse(horseID, rider, allowedPlayer.getName());
					}
				} else {
					badMsg(sender, "This player doesn't exist");
				}
			} else {
				badMsg(sender, "You need to enter the name of the denied player.");
			}
			break;
		case "allowed":
			if (!isOwnerOnHorse()) {
				return true;
			}
			List<String> allowedPlayers = horses.getAllowedPlayers(horseID);
			if (allowedPlayers == null) {
				msg(sender, "There are no other players allowed on this horse.");
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
				msg(sender, "You have allowed the following people on this horse: " + allowedPlayersString);
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
		case "special":
			if (args.length > 1) {
				switch (args[1].toLowerCase()) {
				case "undead": case "skeleton":
					if (isOwnerOnHorse()) {
						horses.createSpecialHorseCommand(horse, rider, args[1].toLowerCase());
					}
					break;
				case "info":
					horses.specialHorsesStats((Player)sender);
					break;
				default: 
					return false;
				}
			} else {
				return false;
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
				this.badMsg(sender, "You need to be on a horse to do that!");
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
				badMsg(sender, "You are not the owner of this horse.");
			}
		} else {
			msg(rider, "This horse doesn't have an owner yet! use /horse claim (if it's yours)");
			
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
