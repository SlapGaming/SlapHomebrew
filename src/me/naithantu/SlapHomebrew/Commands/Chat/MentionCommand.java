package me.naithantu.SlapHomebrew.Commands.Chat;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.naithantu.SlapHomebrew.Commands.AbstractCommand;
import me.naithantu.SlapHomebrew.Commands.Exception.CommandException;
import me.naithantu.SlapHomebrew.Commands.Exception.UsageException;
import me.naithantu.SlapHomebrew.Controllers.Mention;
import me.naithantu.SlapHomebrew.Util.Util;

public class MentionCommand extends AbstractCommand {

	public MentionCommand(CommandSender sender, String[] args) {
		super(sender, args);
	}

	@Override
	public boolean handle() throws CommandException {
		testPermission("mention");
		
		if (args.length == 0) return false; //Usage
		Player targetPlayer = null;
		Mention mention = plugin.getMention();
		
		switch (args[0].toLowerCase()) {
		case "info": //Get info about your current 
			if (args.length > 1 && Util.testPermission(sender, "mention.otherinfo")) {
				targetPlayer = getOnlinePlayer(args[1], false);
			} else {
				targetPlayer = getPlayer();
			}
			String targetname = targetPlayer.getName();
			
			hMsg("Information about " + (sender == targetPlayer ? "your" : targetname + "'s") + " current " + ChatColor.YELLOW + "@Mention" + ChatColor.WHITE + " status.");
			sender.sendMessage(ChatColor.GOLD + "  \u2517\u25B6 " + ChatColor.GRAY + "Mention notification sound: " + (mention.hasSoundOff(targetname) ? ChatColor.RED + "Off." : ChatColor.GREEN + "On."));
			sender.sendMessage(ChatColor.GOLD + "  \u2517\u25B6 " + ChatColor.GRAY + "Banned from mention system: " + (mention.isBanned(targetname) ? ChatColor.RED + "Yes." : ChatColor.GREEN + "No."));
			if (targetPlayer == sender) {
				sender.sendMessage(ChatColor.GOLD + "  \u2517\u25B6 " + ChatColor.GRAY + "Change sound setting with: " + ChatColor.RED + "/mention sound <on/off>");
			}
			break;
			
		case "ban": //Ban a player
			testPermission("mention.ban");
			if (args.length < 2) throw new UsageException("mention ban <Playername>"); //Usage
			targetPlayer = getOnlinePlayer(args[1], false); //Get player
			mention.setBanned(targetPlayer.getName(), true); //Set banned -> True
			hMsg(targetPlayer.getName() + " is now banned from the @Mention system.");
			break;
			
		case "unban": //Unban a player
			testPermission("mention.ban");
			if (args.length < 2) throw new UsageException("mention unban <Playername>"); //Usage
			targetPlayer = getOnlinePlayer(args[1], false); //Get player
			mention.setBanned(targetPlayer.getName(), false); //Set banned -> False
			hMsg(targetPlayer.getName() + " is now unbanned from the @Mention system.");
			break;
			
		case "sound": case "nofication": case "s": case "notificationsound": //Turn notification sound on/off
			targetPlayer = getPlayer();
			if (args.length < 2) { //Check usage
				throw new UsageException("mention sound on/off");
			}
			boolean on;
			switch (args[1].toLowerCase()) { //State
			case "on": case "true": 
				on = true;
				break;
			case "off": case "false":
				on = false;
				break;
			default:
				throw new UsageException("mention sound on/off");
			}
			mention.setSound(targetPlayer.getName(), on); //Set sound	
			hMsg("Turned mention notification sound " + (on ? ChatColor.GREEN + "on" : ChatColor.RED + "off"));
			break;
			
		case "help": //Help command
			hMsg("The @Mention feature is made so you can mention other players. You can do this by typing '@' followed by (the beginning of) the player's name. You can press 'TAB' to auto complete it.");
			sender.sendMessage(ChatColor.GOLD + "  \u2517\u25B6 /mention info : " + ChatColor.WHITE + "Get information about your current @Mention settings.");
			sender.sendMessage(ChatColor.GOLD + "  \u2517\u25B6 /mention sound <on/off> : " + ChatColor.WHITE + "Turn the notification sound on or off.");
			break;
			
		default:
			return false;
		}
		
		
		return true;
	}

}
