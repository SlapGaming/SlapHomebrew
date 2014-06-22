package me.naithantu.SlapHomebrew.Commands.Stats;

import me.naithantu.SlapHomebrew.Commands.Exception.UsageException;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.naithantu.SlapHomebrew.Commands.AbstractCommand;
import me.naithantu.SlapHomebrew.Commands.Exception.CommandException;
import me.naithantu.SlapHomebrew.Controllers.PlayerLogging.DeathLogger;

public class KillsCommand extends AbstractCommand {

	public KillsCommand(CommandSender sender, String[] args) {
		super(sender, args);
	}

	@Override
	public boolean handle() throws CommandException {
		testPermission("kills");

        //Check if the player is still executing a DB command
		checkDoingCommand();

        //Default params
        boolean leaderboard = false;
        boolean monthly = false;
        //Check params
        if (args.length > 0) {
            switch (args[0].toLowerCase()) {
                case "lb": case "leaderboard":
                    leaderboard = true;
                    if (args.length > 1) { //Will accept any second param
                        monthly = true;
                    }
                    break;

                default:
                    throw new UsageException("kills leaderboard/lb [monthly]");
            }
        }

        //Player is going to do a DB request
		addDoingCommand();

        //Make the request @ the DeathLogger.
		if (!leaderboard) {
            Player p = getPlayer();
            DeathLogger.sendPlayerKills(p);
        } else {
            DeathLogger.sendKillsLeaderboard(sender, monthly);
        }
		return true;
	}

}
