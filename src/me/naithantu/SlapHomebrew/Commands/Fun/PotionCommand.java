package me.naithantu.SlapHomebrew.Commands.Fun;

import me.naithantu.SlapHomebrew.SlapHomebrew;
import me.naithantu.SlapHomebrew.Commands.AbstractCommand;
import me.naithantu.SlapHomebrew.Commands.Exception.CommandException;
import me.naithantu.SlapHomebrew.Commands.Exception.ErrorMsg;
import me.naithantu.SlapHomebrew.Util.Util;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;

public class PotionCommand extends AbstractCommand {
	public PotionCommand(CommandSender sender, String[] args, SlapHomebrew plugin) {
		super(sender, args, plugin);
	}

	public boolean handle() throws CommandException {
		testPermission("potion"); //Test permission

		int time = 30, power = 3;
		if (args.length <= 1) return false; //Check usage
		
		Player potionPlayer;
		String playername = args[1].toLowerCase();
		if (playername.equalsIgnoreCase("me") || playername.equalsIgnoreCase("self")) { //Applying on the commandsender
			potionPlayer = getPlayer(); //Cast to player
		} else {
			potionPlayer = getOnlinePlayer(playername, false); //Get an online player by that name
		}
		
		String potionName = args[0].toLowerCase();
		
		if (potionName.equals("remove") || potionName.equals("cleanse")) { //Check if removing
			Util.wipeAllPotionEffects(potionPlayer);
			hMsg("Potion effects removed for player " + potionPlayer.getName() + "!");
			return true;
		}
		
		try { //Parse time & power values if given
			if (args.length > 2) time = Integer.parseInt(args[2]);
			if (args.length > 3) power = Integer.parseInt(args[3]);
		} catch (NumberFormatException e) {
			throw new CommandException(ErrorMsg.notANumber);
		}
		
		PotionEffect pEff = Util.getPotionEffect(potionName, time, power); //Get the PotionEffect
		if (pEff != null) { //Check if valid
			potionPlayer.addPotionEffect(pEff, true);
			hMsg("Potion effect added for player " + potionPlayer.getName() + "!");
		} else {
			throw new CommandException("That potion effect does not exist!");
		}
		return true;
	}

}
