package me.naithantu.SlapHomebrew.Commands;

import java.util.List;

import me.naithantu.SlapHomebrew.Horses;
import me.naithantu.SlapHomebrew.SlapHomebrew;

import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Player;

public class HorseCommand extends AbstractCommand {

	private static Horses horses;
	
	protected HorseCommand(CommandSender sender, String[] args, SlapHomebrew plugin) {
		super(sender, args, plugin);
		if (horses == null) {
			horses = plugin.getHorses();
		}
	}

	@Override
	public boolean handle() {
		if (!(sender instanceof Player)) {
			this.badMsg(sender, "You need to be in-game to do that!");
			return true;
		}

		if (!testPermission(sender, "horse")) {
			this.noPermission(sender);
			return true;
		}
		
		if (args.length == 0) {
			return false;
		}
		
		Player rider = (Player) sender;
		Entity vehicle = rider.getVehicle();
		
		if (vehicle == null || vehicle.getType() != EntityType.HORSE) {
			this.badMsg(sender, "You need to be on a horse to do that!");
			return true;
		}
		
		Horse horse = (Horse) vehicle;
		
		if (args[0].equals("claim")) {
			horses.claimHorse(horse, rider);
			return true;
		}
		
		String entityID = horse.getUniqueId().toString();
		
		if (!horses.getOwner(entityID).equals(rider.getName())) {
			badMsg(sender, "You are not the owner of this horse.");
			return true;
		}
		
		switch (args[0]) {
		case "owner": case "changeowner":
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
			if (args.length > 1) {
				OfflinePlayer allowedPlayer = plugin.getServer().getOfflinePlayer(args[1]);
				if (allowedPlayer != null) {
					if (rider.getName().equals(allowedPlayer.getName())) {
						badMsg(sender, "You are the owner of this horse.");
					} else {
						horses.allowOnHorse(entityID, rider, allowedPlayer.getName());
					}
				} else {
					badMsg(sender, "This player doesn't exist");
				}
			} else {
				badMsg(sender, "You need to enter the name of the allowed player.");
			}
			break;
		case "deny":
			if (args.length > 1) {
				OfflinePlayer allowedPlayer = plugin.getServer().getOfflinePlayer(args[1]);
				if (allowedPlayer != null) {
					if (rider.getName().equals(allowedPlayer.getName())) {
						badMsg(sender, "You are the owner of this horse.");
					} else {
						horses.denyOnHorse(entityID, rider, allowedPlayer.getName());
					}
				} else {
					badMsg(sender, "This player doesn't exist");
				}
			} else {
				badMsg(sender, "You need to enter the name of the denied player.");
			}
			break;
		case "allowed":
			List<String> allowedPlayers = horses.getAllowedPlayers(entityID);
			if (allowedPlayers == null) {
				msg(sender, "There are no other players allowed on this horse.");
			} else {
				int xCount = 1; int listSize = allowedPlayers.size();
				String allowedPlayersString = "";
				System.out.println(listSize);
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
		}
		return true;
	}

	
	
}
