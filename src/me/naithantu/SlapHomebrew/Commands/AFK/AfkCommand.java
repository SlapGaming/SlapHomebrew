package me.naithantu.SlapHomebrew.Commands.AFK;

import me.naithantu.SlapHomebrew.Commands.AbstractCommand;
import me.naithantu.SlapHomebrew.Commands.Exception.CommandException;
import me.naithantu.SlapHomebrew.Controllers.AwayFromKeyboard;
import me.naithantu.SlapHomebrew.Util.Util;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class AfkCommand extends AbstractCommand {
	
	public AfkCommand(CommandSender sender, String[] args) {
		super(sender, args);
	}

	public boolean handle() throws CommandException {
		Player p = getPlayer(); //Check if player
        testPermission("afk"); //Check if able to go AFK

        //Get playername
		String playername = p.getName();

        //Get the AFK Controller
		AwayFromKeyboard afk = plugin.getAwayFromKeyboard();
		
		//Prevent auto AFK | command: /afk -prevent
		if (args.length == 1) {
			if (args[0].equals("-prevent")) {
				testPermission("afk.prevent"); //Check if correct permssion
				boolean preventAfk = afk.hasPreventAFK(p); //Check if prevent AFK is on
				
				//Change current AFK Prevent state
				if (preventAfk) afk.removeFromPreventAFK(p);
				else afk.setPreventAFK(p);
				
				hMsg("Prevent AFK is " + (preventAfk ? "off." : "on.")); //Send current status message
				return true;
			}
		}
		
		if (!afk.isAfk(p)) {
			//Player currently not AFK -> Go AFK
			if (args.length == 0) {
				//No reason
				afk.goAfk(p, "AFK");
			} else if (args.length > 0) {				
				afk.goAfk(p, Util.buildString(args, " ", 0));
			}
		} else {
			//Player AFK -> Leave AFK
			afk.leaveAfk(p);
		}
		return true;
	}
}