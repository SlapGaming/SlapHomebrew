package me.naithantu.SlapHomebrew.Commands.Chat;

import java.util.HashSet;

import me.naithantu.SlapHomebrew.SlapHomebrew;
import me.naithantu.SlapHomebrew.Commands.AbstractCommand;
import me.naithantu.SlapHomebrew.Commands.Exception.CommandException;
import me.naithantu.SlapHomebrew.Commands.Exception.UsageException;
import me.naithantu.SlapHomebrew.PlayerExtension.PlayerControl;
import me.naithantu.SlapHomebrew.PlayerExtension.SlapPlayer;
import me.naithantu.SlapHomebrew.Util.Util;
import nl.stoux.slapbridged.bukkit.SlapBridged;
import nl.stoux.slapbridged.objects.OtherServer;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class MsgCommand extends AbstractCommand {

	public MsgCommand(CommandSender sender, String[] args) {
		super(sender, args);
	}

	@Override
	public boolean handle() throws CommandException {
		testPermission("privatemsg"); //Test perm
		if (args.length < 2) throw new UsageException("msg <toPlayer> <Message..>"); //Usage
				
		//Get SlapPlayer
		SlapPlayer slapPlayer = getSlapPlayer();
		
		//Lowercase the targetType
		String targetLC = args[0].toLowerCase();
		
		//Get set of all players
		HashSet<String> targetPlayers = new HashSet<String>();
		
		//	=> This server
		for (Player p : Util.getOnlinePlayers()) {
			String toPlayer = p.getName();
			String toPlayerLC = toPlayer.toLowerCase();
			if (toPlayerLC.startsWith(targetLC)) {
				targetPlayers.add(toPlayer);
			}
		}
		
		//	=> If Bridged, add other servers
		if (plugin.hasSlapBridged()) {
			for (OtherServer server : SlapBridged.getAPI().getOtherServers()) {
				for (String toPlayer : server.getPlayers().keySet()) {
					String toPlayerLC = toPlayer.toLowerCase();
					if (toPlayerLC.startsWith(targetLC)) {
						targetPlayers.add(toPlayer);
					}
				}
			}
		}
		
		//Target player
		String targetPlayer = null;
		
		//Check if only messaging one person
		switch (targetPlayers.size()) {
		case 0: throw new CommandException("No player found for: " + args[0]); //No players
			
		case 1:
			for (String p : targetPlayers) {
				targetPlayer = p;
			}
			break;
			
		default: throw new CommandException("Multiple players found (" + Util.buildString(targetPlayers, ", ") + ")!"); //To many players found
		}
		
		sendMessage(slapPlayer, targetPlayer, Util.buildString(args, " ", 1));
		return true;
	}
	
	/**
	 * Send a message from one player to another player
	 * @param sendingSPlayer The sending SlapPlayer on this server
	 * @param toPlayerName The name of the receiving player
	 * @param message The message
	 */
	public static void sendMessage(SlapPlayer sendingSPlayer, String toPlayerName, String message) {
		String sendingPlayer = sendingSPlayer.getName();
		
		//Set last reply for player
		sendingSPlayer.setLastReply(toPlayerName);
									
		//Check if allowed colors
		boolean colors = Util.testPermission(sendingSPlayer.getPlayer(), "staff");
				
		//Color message
		String coloredMessage = (colors ? ChatColor.translateAlternateColorCodes('&', message) : message);
						
		//Display message to player
		sendingSPlayer.sendMessage("[" + ChatColor.GOLD + "Me" + ChatColor.WHITE + " -> " + ChatColor.GOLD + toPlayerName + ChatColor.WHITE + "] " + coloredMessage);
				
		//Get the player
		Player player = Bukkit.getPlayer(toPlayerName);
		if (player != null) {
			//Slap players
			SlapPlayer toSPlayer = PlayerControl.getPlayer(player);
			toSPlayer.setLastReply(sendingPlayer);
					
			//Show the player the same message
			player.sendMessage("[" + ChatColor.GOLD + sendingPlayer + ChatColor.WHITE + " -> " + ChatColor.GOLD + "Me" + ChatColor.WHITE + "] " + coloredMessage);
		}
				
		//Send it to other servers
		if (SlapHomebrew.getInstance().hasSlapBridged()) {
			SlapBridged.getAPI().playerSendsMsg(sendingPlayer, toPlayerName, message, colors);
		}
		
		//SocialSpy
		socialSpy(sendingPlayer, toPlayerName, coloredMessage);
	}
	
	/**
	 * Send a /msg as SocialSpy to permission holders
	 * @param fromPlayer The sending player
	 * @param toPlayer The receiving player
	 * @param message The send message
	 */
	public static void socialSpy(String fromPlayer, String toPlayer, String message) {
		String spyMessage = ChatColor.GRAY + "[Social] [" + fromPlayer + " -> " + toPlayer + "] " + message;
		
		for (Player p : Util.getOnlinePlayers()) { //Loop thru players
			String targetPlayer = p.getName();
			if (Util.testPermission(p, "socialspy")) { //Check if SocialSpy
				if (!fromPlayer.equals(targetPlayer) && !toPlayer.equals(targetPlayer)) { //Check if the player doesn't receive the message anyway
					p.sendMessage(spyMessage); //Send the spy
				}
			}
		}
	}
	
	
	

}
