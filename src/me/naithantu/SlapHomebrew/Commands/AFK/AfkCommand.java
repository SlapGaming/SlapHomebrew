package me.naithantu.SlapHomebrew.Commands.AFK;

import me.naithantu.SlapHomebrew.SlapHomebrew;
import me.naithantu.SlapHomebrew.Commands.AbstractCommand;
import me.naithantu.SlapHomebrew.Commands.Exception.CommandException;
import me.naithantu.SlapHomebrew.Controllers.AwayFromKeyboard;
import me.naithantu.SlapHomebrew.Util.Util;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class AfkCommand extends AbstractCommand {
	
	private static AwayFromKeyboard afk = null;
	
	public AfkCommand(CommandSender sender, String[] args, SlapHomebrew plugin) {
		super(sender, args, plugin);
		if (afk == null) {
			afk = plugin.getAwayFromKeyboard();
		}
	}

	public boolean handle() throws CommandException {
		Player p = getPlayer(); //Cast to player
		String playername = p.getName();
		
		//Prevent auto AFK | command: /afk -prevent
		if (args.length == 1) {
			if (args[0].equals("-prevent")) {
				testPermission("afk.prevent"); //Check if correct permssion
				boolean preventAfk = afk.hasPreventAFK(playername); //Check if prevent AFK is on 
				
				//Change current AFK Prevent state
				if (preventAfk) afk.removeFromPreventAFK(playername);
				else afk.setPreventAFK(playername);
				
				hMsg("Prevent AFK is " + (preventAfk ? "off." : "on.")); //Send current status message
			}
		}
		
		if (!afk.isAfk(playername)) {
			//Player currently not AFK -> Go AFK
			if (args.length == 0) {
				//No reason
				afk.goAfk(playername, "AFK");
			} else if (args.length > 0) {				
				afk.goAfk(playername, Util.buildString(args, " ", 0));
			}
		} else {
			//Player AFK -> Leave AFK
			afk.leaveAfk(playername);
		}
		return true;
	}
}