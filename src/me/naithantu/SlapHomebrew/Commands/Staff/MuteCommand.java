package me.naithantu.SlapHomebrew.Commands.Staff;

import me.naithantu.SlapHomebrew.Commands.AbstractCommand;
import me.naithantu.SlapHomebrew.Commands.Exception.CommandException;
import me.naithantu.SlapHomebrew.Commands.Exception.ErrorMsg;
import me.naithantu.SlapHomebrew.Commands.Exception.UsageException;
import me.naithantu.SlapHomebrew.Controllers.MuteController;
import me.naithantu.SlapHomebrew.Util.Util;
import nl.stoux.SlapPlayers.Model.Profile;
import nl.stoux.SlapPlayers.Util.DateUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

/**
 * Created by Leon on 28-6-14.
 */
public class MuteCommand extends AbstractCommand {

    private MuteController muteController;

    public MuteCommand(CommandSender sender, String[] args) {
        super(sender, args);

        //Get controller
        this.muteController = plugin.getMuteController();
    }

    @Override
    public boolean handle() throws CommandException {
        //Test permissions
        boolean staff = Util.testPermission(sender, "mute.staff");
        boolean check = Util.testPermission(sender, "mute.check");

        if (!staff && !check) { //No permissions at all
            throw new CommandException(ErrorMsg.noPermission);
        } else if (check && !staff) { //is only able to check own mute status
            Player p = getPlayer();
            String UUID = p.getUniqueId().toString();

            //Send status
            sendMutedStatus(UUID, "You are");
            return true;
        }

        //Has staff permission
        if (args.length == 0) return false; //Usage

        //Check sub command
        switch (args[0].toLowerCase()) {
            case "list":case "l": //Listing of all muted people
                int page = 1;

                //Check for page param
                if (args.length > 1) {
                    page = parseIntPositive(args[1]);
                }

                //Create page listing
                throw new CommandException(ErrorMsg.notSupportedYet);
//              return true;

            case "info":case "i": //Info about a player
                if (args.length != 2) throw new UsageException("mute info <Player>"); //Usage

                //Get player
                Profile offPlayer = getOfflinePlayer(args[1]);

                //Get status
                sendMutedStatus(offPlayer.getUUIDString(), offPlayer.getCurrentName() + " is");
                return true;

            case "help": //Help page
                throw new CommandException(ErrorMsg.notSupportedYet);

        }

        //=> Mute player
        if (args.length < 3) throw new UsageException("mute <Player> <Perm | 3days5hours2seconds (or similar)> <Reason>");

        //Get player
        Profile offPlayer = getOfflinePlayer(args[0]);

        //=> Check if the player can be muted
        if (Util.checkPermission(offPlayer.getUUIDString(), "mute.exempt")) {
            throw new CommandException("This player cannot be muted.");
        }

        //Parse mute duration
        long mutedTill = System.currentTimeMillis() + Util.parseToTime(args[1]);

        //Build reason
        String reason = Util.buildString(args, " ", 2);

        //Check who issued this mute
        String mutedBy = sender.getName();
        if (sender instanceof Player) {
            mutedBy = ((Player) sender).getUniqueId().toString();
        }

        //Mute player
        muteController.setMuted(offPlayer.getUUIDString(), reason, mutedBy, mutedTill);

        //Notify sender
        hMsg(offPlayer.getCurrentName() + " has been muted " + (mutedTill == -1 ? "permanently." : "till " + ChatColor.GREEN + DateUtil.format("dd-MM-yyyy HH:mm", mutedTill) + ChatColor.WHITE + "."));

        //Notify player if online
        Player p = Bukkit.getPlayer(UUID.fromString(offPlayer.getUUIDString()));
        if (p != null) {
            Util.msg(p, "You have been muted. Use /muted for more info.");
        }

        return true;
    }


    /**
     * Send the muted status to the sender of this command
     * @param UUID The UUID of the player
     * @param adressing The person before any status, eg: "You are" or "Stoux is"
     */
    private void sendMutedStatus(String UUID, String adressing) {
        if (muteController.isMuted(UUID)) { //Check if muted
            long mutedTill = muteController.mutedTill(UUID); //Get till when
            if (mutedTill == -1) { //Perm muted
                hMsg(adressing + " permanently muted.");
            } else {
                hMsg(adressing + " muted till " + ChatColor.GREEN + DateUtil.format("dd-MM-yyyy HH:mm", mutedTill) + ChatColor.WHITE + ".");
            }

        } else {
            hMsg(adressing + " not muted.");
        }
    }
}
