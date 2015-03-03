package me.naithantu.SlapHomebrew.Commands.Chat;

import me.naithantu.SlapHomebrew.Commands.AbstractCommand;
import me.naithantu.SlapHomebrew.Commands.Exception.CommandException;
import me.naithantu.SlapHomebrew.Commands.Exception.UsageException;
import me.naithantu.SlapHomebrew.PlayerExtension.SlapPlayer;
import me.naithantu.SlapHomebrew.Util.Util;
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
		Player targetPlayer = null;
		
		//	=> This server
		targetPlayer = plugin.getServer().getPlayer(replyPlayer);
		
		//Check if the player is found
		if (targetPlayer == null) {
			slapPlayer.setLastReply(null); //Reset last reply
			throw new CommandException("There is no one to reply to!");
		}
		
		//Send the message
		MsgCommand.sendMessage(slapPlayer, replyPlayer, Util.buildString(args, " ", 0));
		return true;
	}

}
