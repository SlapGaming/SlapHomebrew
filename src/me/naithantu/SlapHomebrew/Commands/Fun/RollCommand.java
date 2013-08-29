package me.naithantu.SlapHomebrew.Commands.Fun;

import java.util.Random;

import me.naithantu.SlapHomebrew.SlapHomebrew;
import me.naithantu.SlapHomebrew.Commands.AbstractCommand;
import me.naithantu.SlapHomebrew.Controllers.Lottery;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public class RollCommand extends AbstractCommand {
	
	private static Lottery lottery;
	
	public RollCommand(CommandSender sender, String[] args, SlapHomebrew plugin) {
		super(sender, args, plugin);
		if (lottery == null) {
			lottery = plugin.getLottery();
		}
	}

	public boolean handle() {
		if (!testPermission(sender, "roll")) {
			this.noPermission(sender);
			return true;
		}
		
		if (lottery.getPlaying()) {
			if (!lottery.getLottery().containsKey(sender.getName())) {
				Random random = new Random();
				int randInt = random.nextInt(101);
				lottery.getLottery().put(sender.getName(), randInt);
				Bukkit.getServer().broadcastMessage(ChatColor.GOLD + "[SLAP] " + ChatColor.WHITE + sender.getName() + " rolled " + Integer.toString(randInt) + "!");
			} else {
				this.badMsg(sender, "You have already rolled in this lottery!");
			}
		} else if (lottery.isFakeLotteryPlaying()) {
			if (!lottery.hasAlreadyFakeRolled(sender.getName())) {
				if (sender.getName().equals(lottery.getFakeLotteryWinner())) {
					Bukkit.getServer().broadcastMessage(ChatColor.GOLD + "[SLAP] " + ChatColor.WHITE + sender.getName() + " rolled 100!");
					lottery.fakeRoll(sender.getName(), 100);
				} else {
					Bukkit.getServer().broadcastMessage(ChatColor.GOLD + "[SLAP] " + ChatColor.WHITE + sender.getName() + " rolled 0!");
					lottery.fakeRoll(sender.getName(), 0);
				}
			} else {
				this.badMsg(sender, "You have already rolled in this lottery!");
			}
		} else {
			this.badMsg(sender, "There is currently no lottery playing!");
		}
		

		return true;
	}
}
