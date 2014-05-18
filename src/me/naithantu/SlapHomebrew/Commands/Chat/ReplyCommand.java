package me.naithantu.SlapHomebrew.Commands.Chat;

import me.naithantu.SlapHomebrew.Commands.AbstractCommand;
import me.naithantu.SlapHomebrew.Commands.Exception.CommandException;
import me.naithantu.SlapHomebrew.Commands.Exception.UsageException;
import me.naithantu.SlapHomebrew.PlayerExtension.SlapPlayer;
import me.naithantu.SlapHomebrew.Util.Util;
import nl.stoux.slapbridged.bukkit.SlapBridged;
import nl.stoux.slapbridged.objects.OtherPlayer;
import nl.stoux.slapbridged.objects.OtherServer;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ReplyCommand extends AbstractCommand {

	public ReplyCommand(CommandSender sender, String[] args) {
		super(sender, args);
	}

	@Override
	public boolean handle() throws CommandException {
		testPermission("privatemsg"); //Test perms
		if (args.length == 0) throw new UsageException("reply <Message..>"); //Usage
		
		//Get SlapPlayer
		SlapPlayer slapPlayer = getSlapPlayer();
		
		//Check if the player has anyone to reply to
		String replyPlayer = slapPlayer.getLastReply();
		if (replyPlayer == null) throw new CommandException("There is no one to reply to!");
		
		//Try to find the player
		OtherPlayer oTargetPlayer = null;
		Player targetPlayer = null;
		
		//	=> This server
		targetPlayer = plugin.getServer().getPlayer(replyPlayer);
		
		//	=> other servers
		if (targetPlayer == null && plugin.hasSlapBridged()) {
			for (OtherServer server : SlapBridged.getAPI().getOtherServers()) {
				OtherPlayer p = server.getPlayers().get(replyPlayer);
				if (p != null) {
					oTargetPlayer = p;
					break;
				}
			}
		}
		
		//Check if the player is found
		if (oTargetPlayer == null && targetPlayer == null) {
			slapPlayer.setLastReply(null); //Reset last reply
			throw new CommandException("There is no one to reply to!");
		}
		
		//Send the message
		MsgCommand.sendMessage(slapPlayer, replyPlayer, Util.buildString(args, " ", 0));
		return true;
	}

}
