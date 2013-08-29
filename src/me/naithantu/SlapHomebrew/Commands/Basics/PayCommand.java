package me.naithantu.SlapHomebrew.Commands.Basics;

import java.util.HashSet;

import me.naithantu.SlapHomebrew.SlapHomebrew;
import me.naithantu.SlapHomebrew.Commands.AbstractCommand;
import net.milkbowl.vault.economy.Economy;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public class PayCommand extends AbstractCommand {
	static HashSet<String> chatBotBlocks = new HashSet<String>();

	public PayCommand(CommandSender sender, String[] args, SlapHomebrew plugin) {
		super(sender, args, plugin);
	}

	public boolean handle() {
		if (!testPermission(sender, "pay")) {
			this.noPermission(sender);
			return true;
		}

		if (args.length < 2)
			return false;
		Double amount;
		try{
			amount = Double.parseDouble(args[1].replaceAll("[^0-9\\.]", ""));
		}catch(NumberFormatException e){
			return false;
		}
		
		if (amount <= 0 || Double.isInfinite(amount))
			return false;
				
		//Round the amount to 2 decimals.
		long temp = Math.round(amount * 100);
		amount = (double) temp/100;

		Economy eco = plugin.getEconomy();
		
		String receiver = args[0];
		double senderBalance = eco.getBalance(sender.getName());
		if(!eco.hasAccount(receiver)){
			this.badMsg(sender, "Player not found (use exact player name).");
			return true;
		}
		
		if(senderBalance < amount){
			this.badMsg(sender, "You do not have sufficient funds.");
			return true;
		}
		
		//Add and remove money.
		eco.withdrawPlayer(sender.getName(), amount);
		eco.depositPlayer(receiver, amount);
		
		//Send messages.
		sender.sendMessage(ChatColor.GREEN + "$" + Double.toString(amount) + " has been sent to " + receiver + ".");
		CommandSender receivingPlayer = plugin.getServer().getPlayer(receiver);
		if(receivingPlayer != null){
			receivingPlayer.sendMessage(ChatColor.GREEN + "$"  + amount + " has been received from " + sender.getName() + ".");
		}
		return true;
	}
}
