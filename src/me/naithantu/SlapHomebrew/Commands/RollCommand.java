package me.naithantu.SlapHomebrew.Commands;

import java.util.Random;

import me.naithantu.SlapHomebrew.SlapHomebrew;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public class RollCommand extends AbstractCommand {
	public RollCommand(CommandSender sender, String[] args) {
		super(sender, args);
	}

	public boolean handle() {
		if (!testPermission(sender, "roll")) {
			this.noPermission(sender);
			return true;
		}

		if (SlapHomebrew.lotteryPlaying == true) {
			if (!SlapHomebrew.lottery.containsKey(sender.getName())) {
				Random random = new Random();
				int randInt = random.nextInt(101);
				if (!SlapHomebrew.lottery.containsValue(randInt)) {
					SlapHomebrew.lottery.put(sender.getName(), randInt);
				}
				Bukkit.getServer().broadcastMessage(ChatColor.GOLD + "[SLAP] " + ChatColor.WHITE + sender.getName() + " rolled " + Integer.toString(randInt) + "!");
			} else {
				this.badMsg(sender, "You have already rolled in this lottery!");
			}
		} else {
			this.badMsg(sender, "There is currently no lottery playing!");
		}

		return true;
	}
}
