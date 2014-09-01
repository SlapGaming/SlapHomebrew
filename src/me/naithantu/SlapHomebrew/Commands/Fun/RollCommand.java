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

        //Get the playername
		String playername = p.getName();

        //Get the lottery controller
		Lottery lottery = plugin.getLottery();

        //Check if the lottery is running
        if (lottery.isPlaying()) {
            //Get the player's UUID
            String UUID = p.getUniqueId().toString();

            //Check if already rolled
            if (lottery.hasRolled(UUID)) throw new CommandException(ErrorMsg.alreadyRolled);

            //Roll
            int rolledNumber = lottery.getRandom().nextInt(101);
            lottery.roll(UUID, rolledNumber);
            //=> Broadcast roll
            Util.broadcastHeader(playername + " rolled " + rolledNumber + "!");
        } else if (lottery.isFakeLotteryPlaying()) {
            //Check if already rolled
            if (lottery.hasAlreadyFakeRolled(playername)) throw new CommandException(ErrorMsg.alreadyRolled);

            //Add roll
            lottery.fakeRoll(playername);

            //Roll a number
            int rolledNumber = 9001;
            if (!playername.equalsIgnoreCase(lottery.getFakeLotteryWinner())) {
                //=> If the player is not the winner, roll something weird
                rolledNumber = (-50 + lottery.getRandom().nextInt(50));
            }

            //Broadcast the roll
            Util.broadcastHeader(playername + " rolled " + rolledNumber);
        } else {
            throw new CommandException("There is currently no lottery playing!");
        }

		return true;
	}
}
