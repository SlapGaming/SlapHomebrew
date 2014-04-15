package me.naithantu.SlapHomebrew.Commands.Fun;

import nl.stoux.slapbridged.bukkit.SlapBridged;
import nl.stoux.slapbridged.objects.OtherPlayer;
import nl.stoux.slapbridged.objects.OtherServer;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import ru.tehkode.permissions.PermissionUser;
import ru.tehkode.permissions.bukkit.PermissionsEx;

import me.naithantu.SlapHomebrew.Commands.AbstractCommand;
import me.naithantu.SlapHomebrew.Commands.Exception.CommandException;
import me.naithantu.SlapHomebrew.Util.Util;

public class WaveCommand extends AbstractCommand {

	public WaveCommand(CommandSender sender, String[] args) {
		super(sender, args);
	}

	@Override
	public boolean handle() throws CommandException {
		Player fromPlayer = getPlayer();
		testPermission("wave");
				
		if (args.length != 1) return false;
		
		String gray = ChatColor.GRAY.toString(); //Short for grays due to heavy usage
				
		if (args[0].equalsIgnoreCase("everyone")) { //If waving to everyone
			Util.broadcast(gray + " ** " + getName(fromPlayer) + gray + " waves to " + ChatColor.GOLD + "Everyone" + gray + " **");
			
			//SlapBridged event
			if (plugin.hasSlapBridged()) {
				SlapBridged.getAPI().playerWavesToEveryone(fromPlayer.getName());
			}
			
		} else { //Waving to player
			String toName = "";
			String toColoredName = "";
			
			//Get player
			Player toPlayer;
			if (plugin.hasSlapBridged()) {
				//=> Check online player first
				toPlayer = plugin.getServer().getPlayer(args[0]);
				if (toPlayer == null) {
					//Player not found, check other servers
					int argLength = args[0].length();
					
					//	=> Loop thru servers => Thru players
					for (OtherServer server : SlapBridged.getAPI().getOtherServers()) {
						for (OtherPlayer player : server.getPlayers().values()) {
							String playername = player.getPlayername();
							int nameLength = playername.length();
							if (nameLength >= argLength) { //Check if name is bigger or equal length of argument
								if (playername.substring(0, argLength).equalsIgnoreCase(args[0])) { //Compare strings
									//	=> Player found, set name
									toName = playername;
									//	=> Set colored name (get prefix first)
									if (player.getPrefix() != null && player.getPrefix().length() > 1) {
										toColoredName += Util.colorize(player.getPrefix().substring(0, 2));
									} else {
										toColoredName += ChatColor.WHITE;
									}
									toColoredName += playername;
									break; //Break player loop
								}
							}
						}
						if (!toName.equals("")) break; //Break server loop if found
					}
				} else {
					//Player found
					toName = toPlayer.getName();
					toColoredName = getName(toPlayer);
				}
				
				//Final check
				if (toName.equals("")) {
					throw new CommandException("There is no player with the name '" + args[0] + "' online!");
				}
			} else {
				//=> Get online player. Throw error if not found
				toPlayer = getOnlinePlayer(args[0], false); //Get target
				toName = toPlayer.getName();
				toColoredName = getName(toPlayer);
			}
			
			//Check if not waving to self, and if allowed to wave to self
			if (fromPlayer.getName().equals(toName) && !Util.testPermission(fromPlayer, "wave.self")) {
				throw new CommandException("You cannot wave to yourself..");
			}
			
			//Broadcast
			Util.broadcast(gray + " ** " + getName(fromPlayer) + gray + " waves to " + toColoredName + gray + " **");
			
			//SlapBridged event
			if (plugin.hasSlapBridged()) {
				SlapBridged.getAPI().playerWavesToPlayer(fromPlayer.getName(), toName);
			}
		}
		return true;
	}
	
	private String getName(Player p) {
		PermissionUser user = PermissionsEx.getUser(p);
		String name;
		if (user.getPrefix() != null && user.getPrefix().length() > 1) {
			name = Util.colorize(user.getPrefix().substring(0, 2));
		} else {
			name = ChatColor.WHITE.toString();
		}
		name += p.getName();
		return name;
	}

}
