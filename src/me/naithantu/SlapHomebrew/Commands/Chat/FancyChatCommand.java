package me.naithantu.SlapHomebrew.Commands.Chat;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.naithantu.SlapHomebrew.Commands.AbstractCommand;
import me.naithantu.SlapHomebrew.Commands.Exception.CommandException;
import me.naithantu.SlapHomebrew.Commands.Exception.UsageException;
import me.naithantu.SlapHomebrew.Controllers.FancyMessage.FancyMessageControl;
import me.naithantu.SlapHomebrew.Util.Util;

public class FancyChatCommand extends AbstractCommand {

	private FancyMessageControl fmc;
	
	public FancyChatCommand(CommandSender sender, String[] args) {
		super(sender, args);
	}

	@Override
	public boolean handle() throws CommandException {
		testPermission("fancymessage");
		if (args.length == 0) throw new UsageException("fancymessage < [message] | show [message] [player] | list | reload >"); //Usage
		fmc = plugin.getFancyMessage(); //Get FancyMessage control
		
		switch (args[0].toLowerCase()) {
		case "show": //Show message (to a player)
			if (args.length < 2) {
				throw new UsageException("fancymessage show [message] [player]");
			}
			Player p;
			if (args.length == 2) { //No player given
				p = getPlayer();
			} else { //Find player
				p = getOnlinePlayer(args[2], false);
			}
			Util.sendJsonMessage(p, getMessage(args[1]));
			break;
			
		case "list":
			hMsg("Fancy messages: " + ChatColor.RED + Util.buildString(fmc.getMessageNames(), ChatColor.WHITE +", " + ChatColor.RED));
			break;
			
		case "reload":
			Util.runASync(plugin, new Runnable() {
				@Override
				public void run() {
					fmc.reloadFile();
				}
			});
			hMsg("Reloading Fancy Messages.");
			break;
			
		default:
			Util.broadcastJsonMessage(getMessage(args[0]));
		}		
		return true;
	}
	
	/**
	 * Get a Json Message
	 * @param name The name of the message
	 * @return The Json Message
	 * @throws CommandException if no message with that name
	 */
	private String getMessage(String name) throws CommandException {
		if (!fmc.isMessage(name)) {
			throw new CommandException("There is no Fancy Message with that name.");
		}
		return fmc.getJsonMessage(name);
	}

}
