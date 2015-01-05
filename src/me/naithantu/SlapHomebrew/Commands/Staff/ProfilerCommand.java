package me.naithantu.SlapHomebrew.Commands.Staff;

import me.naithantu.SlapHomebrew.Commands.AbstractCommand;
import me.naithantu.SlapHomebrew.Commands.Exception.CommandException;
import me.naithantu.SlapHomebrew.Commands.Exception.UsageException;
import me.naithantu.SlapHomebrew.Controllers.PlayerLogging.KickLogger;
import me.naithantu.SlapHomebrew.Controllers.PlayerLogging.NoteControl;
import me.naithantu.SlapHomebrew.Controllers.PlayerLogging.Profilable;
import me.naithantu.SlapHomebrew.Controllers.PlayerLogging.SessionLogger;
import me.naithantu.SlapHomebrew.Controllers.Profiler;
import me.naithantu.SlapHomebrew.Util.Helpers.FancyMessageMenu;
import me.naithantu.SlapHomebrew.Util.Util;
import nl.stoux.SlapPlayers.Model.Profile;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by Stoux on 09/09/2014.
 */
public class ProfilerCommand extends AbstractCommand {

    public ProfilerCommand(CommandSender sender, String[] args) {
        super(sender, args);
    }

    @Override
    public boolean handle() throws CommandException {
        testPermission("profiler");

        //usage
        if (args.length == 0) throw new UsageException("profiler addnote <Player> <Note...> | show <Player> [-flags]");

        final Profile offPlayer;

        //Switch on first arg
        switch (args[0].toLowerCase()) {
            //Add a note to a player
            //USAGE: /profiler addnote <Player> <Note...>
            case "addnote":case "add":case "note":case "a":case "n":
                //Usage
                if (args.length < 3) throw new UsageException("profiler addnote <Player> <Note...>");

                //Get the player
                offPlayer = getOfflinePlayer(args[1]);

                //Add the note
                NoteControl.addNote(sender, offPlayer.getUUIDString(), Util.buildString(args, " ", 2));
                hMsg("Note added!");
                break;

            //Display the flags
            //USAGE: /profiler showflags
            case "f":case "flag":case "flags":case "showflags":
                //Enum to String
                ShowFlag[] flags = ShowFlag.values();
                String[] flagNames = new String[flags.length];
                for (int i = 0; i < flags.length; i++) {
                    flagNames[i] = "-" + flags[i].toString().toLowerCase();
                }
                //Combine to one string & send
                hMsg("Flags: " + ChatColor.RED + Util.buildString(flagNames, ChatColor.WHITE + ", " + ChatColor.RED, 0));
                break;

            //Show notes
            //USAGE: /profiler show <Player> [-Flags]
            case "show":case "s":case "shownotes":
                checkDoingCommand();
                //Usage
                if (args.length < 2) throw new UsageException("profiler show <Player> [Page] [-Flags...]");

                //Get the player
                offPlayer = getOfflinePlayer(args[1]);

                ArrayList<ShowFlag> specifiedFlags = null;
                int page = 1;
                //More arguments
                if (args.length > 2) {
                    //Check if the argument is a page
                    for (int i = 2; i < args.length; i++) {
                        if (args[i].substring(0, 1).contains("-")) {
                            //Is a flag
                            try {
                                //Parse the argument
                                ShowFlag flag = ShowFlag.parseArg(args[i]);
                                //=> Create the list if not created yet
                                if (specifiedFlags == null) {
                                    specifiedFlags = new ArrayList<>();
                                }
                                //=> Add to list
                                specifiedFlags.add(flag);
                            } catch (IllegalArgumentException e) {
                                throw new CommandException("Invalid flag (" + args[i] + "). See /profiler showflags");
                            }
                        } else {
                            //Selected a page
                            page = parseIntPositive(args[i]);
                        }
                    }
                }

                //Get the UserID
                final int userID = offPlayer.getID();

                //Start doing the heavy stuff
                addDoingCommand();

                final ArrayList<ShowFlag> finalFlags;
                //Check if any flags given
                if (specifiedFlags == null || specifiedFlags.isEmpty()) {
                    finalFlags = new ArrayList<>();
                    finalFlags.add(ShowFlag.NOTES);
                } else {
                    finalFlags = specifiedFlags;
                }

                //Remove doubles
                boolean containsLoginIPs = finalFlags.contains(ShowFlag.LOGINS_IPS);
                boolean containsLogins = finalFlags.contains(ShowFlag.LOGINS);
                boolean containsIPs = finalFlags.contains(ShowFlag.IPS);
                //=> Check if it contains a case where there is a double
                if (((containsIPs || containsLogins) && containsLoginIPs) || (containsIPs && containsLogins)) {
                    //Remove the singles
                    finalFlags.remove(ShowFlag.IPS);
                    finalFlags.remove(ShowFlag.LOGINS);
                    //Add the combined one (if not there yet)
                    if (!finalFlags.contains(ShowFlag.LOGINS_IPS)) {
                        finalFlags.add(ShowFlag.LOGINS_IPS);
                    }
                }

                //Time to go ASync
                sender.sendMessage(ChatColor.GRAY + "Loading...");
                Util.runASync(new Runnable() {
                    @Override
                    public void run() {
                        //Create a list for all profilables
                        ArrayList<Profilable> allProfilables = new ArrayList<>();

                        try {
                            //Loop through the Flags
                            for (ShowFlag flag : finalFlags) {
                                switch (flag) {
                                    case KICKS:
                                        allProfilables.addAll(KickLogger.getKicks(userID));
                                        break;
                                    case IPS:
                                        allProfilables.addAll(SessionLogger.getSessions(userID, false, true));
                                        break;
                                    case LOGINS:
                                        allProfilables.addAll(SessionLogger.getSessions(userID, true, false));
                                        break;
                                    case LOGINS_IPS:
                                        allProfilables.addAll(SessionLogger.getSessions(userID, true, true));
                                        break;
                                    case BANS:
                                        Util.badMsg(sender, "Profiling bans is currently not supported.");
                                        break;
                                    case NOTES:
                                        allProfilables.addAll(NoteControl.getNotes(userID));
                                        break;
                                    case PROMOTIONS:
                                        //TODO
                                        break;
                                }
                            }

                            //Sort the list
                            Collections.sort(allProfilables);

                            //Create the new FancyMessageMenu
                            FancyMessageMenu fmMenu = new FancyMessageMenu(allProfilables, 18, true, offPlayer.getCurrentName() + "'s Profile", "profiler page");
                            //=> Store the menu
                            plugin.getProfiler().storeMenu(getUUIDProfile().getID(), fmMenu);

                            //Display the first page
                            fmMenu.showPage(sender, 1);
                        } catch (CommandException exception) {
                            //Exception thrown
                            Util.badMsg(sender, exception.getMessage());
                        }

                        //Remove doing command
                        removeDoingCommand();
                    }
                });
                break;

            //Go to the next page of a request
            //USAGE: /profiler page <Page>
            case "page":
                //Heavy command
                checkDoingCommand();

                //Usage
                if (args.length != 2) throw new UsageException("profiler page <Page>");

                //Parse the argument
                final int pageNumber = parseIntPositive(args[1]);

                Profiler profiler = plugin.getProfiler();
                //Check if the player has a menu
                int requesterID = getUUIDProfile().getID();
                if (!profiler.hasMenu(requesterID)) throw new CommandException("There is no Profile loaded!");

                //Get the menu
                final FancyMessageMenu menu = profiler.getMenu(requesterID);

                //Check if the page is withing limits
                if (pageNumber > menu.getNrOfPages()) {
                    throw new CommandException("This profile only has " + menu.getNrOfPages() + " " + (menu.getNrOfPages() == 1 ? "page." : "pages."));
                }

                addDoingCommand();
                //Into ASync to display
                Util.runASync(new Runnable() {
                    @Override
                    public void run() {
                        menu.showPage(sender, pageNumber);
                        removeDoingCommand();
                    }
                });
                break;

            default:
                throw new UsageException("profiler <addnote|show|page>");
        }

        return true;
    }

    private enum ShowFlag {
        KICKS,
        IPS,
        LOGINS,
        LOGINS_IPS,
        BANS,
        NOTES,
        PROMOTIONS;

        /**
         * Try to parse an argument into a Flag
         * @param arg The argument
         * @return The ShowFlag
         * @throws IllegalArgumentException if not a valid flag
         */
        private static ShowFlag parseArg(String arg) throws IllegalArgumentException {
            //Remove the -
            arg = arg.replace("-", "");
            //Add a s at the end if it's not there
            String lastChar = arg.substring(arg.length() - 1);
            if (!lastChar.equalsIgnoreCase("s")) {
                arg += "s";
            }
            return ShowFlag.valueOf(arg.toUpperCase());

        }
    }

}

