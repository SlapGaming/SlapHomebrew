package me.naithantu.SlapHomebrew.Commands.Staff;

import me.naithantu.SlapHomebrew.Commands.AbstractCommand;
import me.naithantu.SlapHomebrew.Commands.Exception.CommandException;
import me.naithantu.SlapHomebrew.Commands.Exception.UsageException;
import me.naithantu.SlapHomebrew.Controllers.MuteController;
import nl.stoux.SlapPlayers.Model.Profile;
import org.bukkit.command.CommandSender;

/**
 * Created by Stoux (http://www.stoux.nl)
 * Created on 29-6-14
 * Project: SLAP Code
 */
public class UnmuteCommand extends AbstractCommand {

    private MuteController muteController;

    public UnmuteCommand(CommandSender sender, String[] args) {
        super(sender, args);

        //Get muteController
        muteController = plugin.getMuteController();
    }

    @Override
    public boolean handle() throws CommandException {
        testPermission("mute.unmute"); //Perms
        if (args.length != 1) throw new UsageException("unmute <Player>"); //Usage

        //Parse player
        Profile player = getOfflinePlayer(args[0]);

        //Unmute player
        boolean unmuted = muteController.unmute(player.getUUIDString());

        //Notify sender
        if (unmuted) {
            hMsg(player.getCurrentName() + " has been unmuted.");
        } else {
            throw new CommandException(player.getCurrentName() + " isn't muted.");
        }
        return true;
    }
}
