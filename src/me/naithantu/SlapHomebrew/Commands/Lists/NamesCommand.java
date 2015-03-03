package me.naithantu.SlapHomebrew.Commands.Lists;

import lombok.AllArgsConstructor;
import lombok.Getter;
import me.naithantu.SlapHomebrew.Commands.AbstractCommand;
import me.naithantu.SlapHomebrew.Commands.Exception.CommandException;
import me.naithantu.SlapHomebrew.Commands.Exception.UsageException;
import me.naithantu.SlapHomebrew.Util.Util;
import mkremins.fanciful.FancyMessage;
import nl.stoux.SlapPlayers.Control.UUIDControl;
import nl.stoux.SlapPlayers.Model.Name;
import nl.stoux.SlapPlayers.Model.Profile;
import nl.stoux.SlapPlayers.SlapPlayers;
import nl.stoux.SlapPlayers.Util.DateUtil;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by Stoux on 04/02/2015.
 */
public class NamesCommand extends AbstractCommand {

    public NamesCommand(CommandSender sender, String[] args) {
        super(sender, args);
    }

    @Override
    public boolean handle() throws CommandException {
        testPermission("names");
        checkDoingCommand();
        if (args.length == 0) {
            throw new UsageException("names [playername]");
        }

        final String playername = args[0].toLowerCase();

        //Get the IDs
        final UUIDControl uuidControl = SlapPlayers.getUUIDController();
        final Collection<Integer> ids = uuidControl.getUserIDs(playername);

        //Check if any IDs found
        if (ids.isEmpty()) {
            throw new CommandException("No players with that name!");
        }

        //Going Async
        addDoingCommand();
        Util.runASync(() -> {
            //Stream the IDs, Map to Profiles, Map to FancyProfiles, Sort & collect
            ids.stream().map(uuidControl::getProfile).map(this::getAsFancyMessages).sorted().forEachOrdered(f -> {
                //Send the messages
                sender.sendMessage("");
                f.getFancyMessages().forEach(m -> {
                    Util.sendFancyMessage(sender, m);
                });
            });

            //Remove doing command
            removeDoingCommand();
        });
        return true;
    }

    /**
     * Create a list of FancyMessages from a Profile
     * @param profile The profile
     * @return The fancy messages profile
     */
    private FancyProfile getAsFancyMessages(Profile profile) {
        //Get the names
        List<Name> names = profile.getNames();
        int size = names.size();

        //Create a list for the FancyMessages
        List<FancyMessage> fmList = new ArrayList<>(size);

        //Create the FMs voor the ToolTip
        FancyMessage uuidTooltip = new FancyMessage("UUID: ").color(ChatColor.GRAY)
                .then(profile.getUUIDString());
        FancyMessage idTooltip = new FancyMessage("ID: ").color(ChatColor.GRAY)
                .then(String.valueOf(profile.getID()));

        //Create the first line
        fmList.add(
                new FancyMessage("[SLAP] ").color(ChatColor.GOLD)
                    .then("Player: ")
                    .then(profile.getCurrentName()).color(ChatColor.GREEN).formattedTooltip(uuidTooltip, idTooltip)
        );

        //Create the player name list
        for (int i = 1; i < size; i++) {
            //Get the name
            Name name = names.get(i);

            //Create the FancyMessage
            FancyMessage previousNameFm = new FancyMessage("  \u2517\u25B6 ").color(ChatColor.GRAY)
                    .then("Previous: ")
                    .then(name.getPlayername()).color(ChatColor.YELLOW);

            //Check if before name changing
            if (i - 1 != size) {
                previousNameFm
                        .then(" (Since ").color(ChatColor.GRAY)
                        .then("[" + DateUtil.format("MM/yyyy", name.getKnownSince()) + "]").color(ChatColor.GRAY)
                        .formattedTooltip(new FancyMessage(DateUtil.format("dd/MM/yyyy HH:mm zzz", name.getKnownSince())))
                        .then(")").color(ChatColor.GRAY);
            }

            //Add the message
            fmList.add(previousNameFm);
        }

        //Return the list
        return new FancyProfile(profile, fmList);
    }

    @Getter
    @AllArgsConstructor
    private class FancyProfile {

        /** The original profile */
        private Profile profile;

        /** The list with fancy messages */
        List<FancyMessage> fancyMessages;

    }


}
