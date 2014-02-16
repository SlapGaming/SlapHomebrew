package me.naithantu.SlapHomebrew.Commands.Fun;

import java.util.Random;

import me.naithantu.SlapHomebrew.Commands.AbstractCommand;
import me.naithantu.SlapHomebrew.Commands.Exception.CommandException;
import me.naithantu.SlapHomebrew.Commands.Exception.ErrorMsg;
import me.naithantu.SlapHomebrew.Controllers.Lottery;
import me.naithantu.SlapHomebrew.Util.Util;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class RollCommand extends AbstractCommand {
		
	public RollCommand(CommandSender sender, String[] args) {
		super(sender, args);
	}

	public boolean handle() throws CommandException {
		Player p = getPlayer();
		testPermission("roll");
		
		String playername = p.getName();
		Lottery lottery = plugin.getLottery(); //Get lottery
		
		if (lottery.getPlaying()) { //Check if lottery is running
			if (!lottery.getLottery().containsKey(p.getName())) { //Check if already rolled
				Random random = new Random();
				int randInt = random.nextInt(101);
				lottery.getLottery().put(playername, randInt);
				Util.broadcastHeader(playername + " rolled " + randInt + "!");
			} else {
				throw new CommandException(ErrorMsg.alreadyRolled);
			}
		} else if (lottery.isFakeLotteryPlaying()) {
			if (!lottery.hasAlreadyFakeRolled(playername)) {
				if (playername.equals(lottery.getFakeLotteryWinner())) {
					Util.broadcastHeader(playername + " rolled 100!");
					lottery.fakeRoll(playername, 100);
				} else {
					Util.broadcastHeader(playername + " rolled 0!");
					lottery.fakeRoll(playername, 0);
				}
			} else {
				throw new CommandException(ErrorMsg.alreadyRolled);
			}
		} else {
			throw new CommandException("There is currently no lottery playing!");
		}
		

		return true;
	}
}
