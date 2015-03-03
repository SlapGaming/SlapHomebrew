package me.naithantu.SlapHomebrew.Commands.Basics;

import me.naithantu.SlapHomebrew.Commands.AbstractCommand;
import me.naithantu.SlapHomebrew.Commands.Exception.CommandException;
import me.naithantu.SlapHomebrew.Commands.Exception.ErrorMsg;
import net.milkbowl.vault.economy.Economy;
import nl.stoux.SlapPlayers.Model.Profile;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class PayCommand extends AbstractCommand {

	public PayCommand(CommandSender sender, String[] args) {
		super(sender, args);
	}

	public boolean handle() throws CommandException {
		Player p = getPlayer();
		testPermission("pay"); //Test permission

		if (args.length != 2) return false; //Check usage
		
		Double amount;
		try {
			amount = Double.parseDouble(args[1].replaceAll("[^0-9\\.]", "")); ///Parse ammount
			if (amount <= 0 || Double.isInfinite(amount)) { //Check if valid
				throw new CommandException(ErrorMsg.notANumber);
			}
		} catch(NumberFormatException e) {
			throw new CommandException(ErrorMsg.notANumber);
		}
				
		//Round the amount to 2 decimals.
		long temp = Math.round(amount * 100);
		amount = (double) temp/100;
		
		Economy eco = plugin.getEconomy(); //Get Economy
		
		Profile offPlayer = getOfflinePlayer(args[0]); //Check if valid player
		String receiver = offPlayer.getCurrentName();
		if (!eco.hasAccount(receiver)) throw new CommandException("This player is not able to recieve money."); //Check if reciever has an account
		
		double senderBalance = eco.getBalance(sender.getName()); //Get sender balance
		if (senderBalance < amount)	throw new CommandException(ErrorMsg.noMoney);
		
		//Add and remove money.
		eco.withdrawPlayer(p.getName(), amount);
		eco.depositPlayer(receiver, amount);
		
		//Send messages.
		msg(ChatColor.GREEN + "$" + Double.toString(amount) + " has been sent to " + receiver + ".");
		CommandSender receivingPlayer = plugin.getServer().getPlayer(receiver);
		if(receivingPlayer != null){
			receivingPlayer.sendMessage(ChatColor.GREEN + "$"  + amount + " has been received from " + sender.getName() + ".");
		}
		return true;
	}
}
