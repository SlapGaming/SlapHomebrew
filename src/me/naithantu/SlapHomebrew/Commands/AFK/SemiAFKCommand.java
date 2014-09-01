package me.naithantu.SlapHomebrew.Commands.AFK;

import me.naithantu.SlapHomebrew.Commands.AbstractCommand;
import me.naithantu.SlapHomebrew.Commands.Exception.CommandException;
import me.naithantu.SlapHomebrew.Controllers.AwayFromKeyboard;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Created by Stoux on 02/09/2014.
 */
public class SemiAFKCommand extends AbstractCommand {

    public SemiAFKCommand(CommandSender sender, String[] args) {
        super(sender, args);
    }

    @Override
    public boolean handle() throws CommandException {
        Player p = getPlayer(); //Check if player
        testPermission("afk"); //Check if able to go AFK

        //Get playername
        String playername = p.getName();

        //Get the AFK Controller
        AwayFromKeyboard afk = plugin.getAwayFromKeyboard();

        //Check if AFK
        if (afk.isAfk(p)) {
            //=> Leave AFK is already AFK
            afk.leaveAfk(p);
        } else {
            //=> Go Semi-AFK
            afk.goAfk(p, "Semi-AFK");
        }

        return true;
    }
}
