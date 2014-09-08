package me.naithantu.SlapHomebrew.Commands.Homes;

import java.util.List;

import me.naithantu.SlapHomebrew.PlayerExtension.UUIDControl;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.naithantu.SlapHomebrew.Commands.AbstractCommand;
import me.naithantu.SlapHomebrew.Commands.Exception.CommandException;
import me.naithantu.SlapHomebrew.Controllers.Homes;
import me.naithantu.SlapHomebrew.Controllers.FancyMessage.FancyMessage;
import me.naithantu.SlapHomebrew.Util.Util;

public class HomesCommand extends AbstractCommand {

	public HomesCommand(CommandSender sender, String[] args) {
		super(sender, args);
	}

	@Override
	public boolean handle() throws CommandException {
		final Player p = getPlayer(); //Player
		testPermission("home"); //Perm
		
		checkDoingCommand();
		
		final String playername = p.getName();
		final Homes homes = plugin.getHomes(); //Get Homes
        final UUIDControl.UUIDProfile profile = getUUIDProfile();
		
		final List<String> homeList = homes.getHomes(playername); //Get homes
		final int homesSize = homeList.size(); //Get size
		
		if (homesSize == 0) {
			hMsg("You have 0 out of " + homes.getTotalNumberOfHomes(profile.getUserID()) + " homes."); //Send used homes
		} else {
			addDoingCommand();
			Util.runASync(new Runnable() {
				
				@Override
				public void run() {
					FancyMessage fm = new FancyMessage(" \u2517\u25B6 ").color(ChatColor.GOLD)
							.addText("Homes -> Click").tooltip("Click on a home to teleport to that home!")
							.addText(": "); //Create new FancyMessage
					
					boolean first = true;
					for (String home : homeList) { //Loop thru homes
						if (!first) { //If not first add comma
							fm.addText(", ");
						}
						fm.addText(home).color(ChatColor.GRAY).runCommand("/home " + home); //Add home
						first = false;
					}
					
					//Parse to JSON
					String json = fm.toJSONString();
					
					//Send used homes
					hMsg("You have " + homesSize + " out of " + homes.getTotalNumberOfHomes(profile.getUserID()) + " homes.");
					//Send clickable homes
					Util.sendJsonMessage(p, json);					
					
					//Remove doing command
					removeDoingCommand();
				}
			});
		}
		return true;
	}

}
