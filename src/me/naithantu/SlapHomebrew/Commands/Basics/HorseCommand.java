package me.naithantu.SlapHomebrew.Commands.Basics;

import me.naithantu.SlapHomebrew.Commands.AbstractCommand;
import me.naithantu.SlapHomebrew.Commands.Exception.CommandException;
import me.naithantu.SlapHomebrew.Commands.Exception.ErrorMsg;
import me.naithantu.SlapHomebrew.Commands.Exception.UsageException;
import me.naithantu.SlapHomebrew.Controllers.Horses;
import me.naithantu.SlapHomebrew.PlayerExtension.UUIDControl;
import me.naithantu.SlapHomebrew.Storage.HorseSerializables.SavedHorse;
import me.naithantu.SlapHomebrew.Util.Helpers.HelpMenu;
import me.naithantu.SlapHomebrew.Util.Util;
import mkremins.fanciful.FancyMessage;
import net.minecraft.server.v1_8_R1.AttributeInstance;
import net.minecraft.server.v1_8_R1.EntityInsentient;
import net.minecraft.server.v1_8_R1.GenericAttributes;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_8_R1.entity.CraftLivingEntity;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Player;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * Created by Stoux on 03/09/2014.
 */
public class HorseCommand extends AbstractCommand {

    public HorseCommand(CommandSender sender, String[] args) {
        super(sender, args);
    }

    @Override
    public boolean handle() throws CommandException {
        //Check if adding horses
        if (args.length > 1 && args[0].equalsIgnoreCase("addhorses")) {
            testPermission("addhorses");

            //Get howmany horses to be added
            int addingHorses = 5; //Defaults to 5
            if (args.length == 3) {
                addingHorses = parseIntPositive(args[2]);
            }

            //Get the player
            UUIDControl.UUIDProfile donatingPlayer = getOfflinePlayer(args[1]);

            //Add the horses
            plugin.getHorses().allowMoreMutatedHorses(donatingPlayer.getUserID(), addingHorses);

            //Notify & Return
            hMsg("Added " + addingHorses + " " + (addingHorses == 1 ? "horse" : "horses") + " to " + donatingPlayer.getCurrentName());
            return true;
        }

        //Actual command
        final Player player = getPlayer();
        testPermission("horse");

        //Get the controller
        Horses hController = plugin.getHorses();

        //No arguments? => Redirect to help
        if (args.length == 0) {
            args = new String[]{"help"};
        }

        //Used arguments trough the switch
        Horse horse; //The horse
        int ownerID; //The UserID of the owner

        //Switch on the command
        switch (args[0].toLowerCase()) {
            case "s":
                horse = getHorse(player);
                horse.setJumpStrength(Double.parseDouble(args[1]));
                break;

            //Change the owner of a horse
            //USAGE: /horse changeOwner <new owner>
            case "owner":case "changeowner":case "cowner":
                //Check usage
                if (args.length != 2) throw new UsageException("horse changeOwner <Player>");

                //Get the horse that the player owns
                horse = getOwnedHorse(hController, player);

                //Check if not mutated
                if (horse.getVariant() == Horse.Variant.UNDEAD_HORSE || horse.getVariant() == Horse.Variant.SKELETON_HORSE) {
                    throw new CommandException("This horse can not be given away.");
                }


                //Get the name of the new owner
                Player newOwner = getOnlinePlayer(args[1], true);

                //Change the owner
                hController.changeOwner(horse, newOwner);

                //Notify the player
                hMsg(newOwner.getName() + " now owns this horse!");
                player.leaveVehicle(); //Leave the horse

                //Notify the other player
                String name = "";
                if (horse.getCustomName() != null) {
                    //The horse has a custom name, also send that to the player
                    name = " It's name is: " + horse.getCustomName();
                }
                Util.msg(newOwner, player.getName() + " has given you one of their horses!" + name);
                break;

            //Allow one or more users on a horse
            //USAGE: /horse allow <Player1>...
            case "allow":case "allowplayer":case "allowplayers":
                //Check usage
                if (args.length < 2) throw new UsageException("horse allowPlayers <Player1> [Player2]...");

                //Get the horse that the player owns
                horse = getOwnedHorse(hController, player);

                //Get the ID of the owner
                ownerID = getUUIDProfile().getUserID();

                //Parse the names
                HashSet<Integer> allowedPlayers = new HashSet<>();
                ArrayList<String> allowedPlayerNames = new ArrayList<>();
                for (int i = 1; i < args.length; i++) {
                    //Get the offline player
                    UUIDControl.UUIDProfile offPlayer = getOfflinePlayer(args[i]);

                    //Check if the player isn't the owner
                    if (offPlayer.getUserID() == ownerID) {
                        continue;
                    }

                    //Add to sets
                    if (allowedPlayers.add(offPlayer.getUserID())) {
                        //Only add the name if the ID was added, thus preventing doubles
                        allowedPlayerNames.add(offPlayer.getCurrentName());
                    }
                }

                //Add players
                hController.allowOnHorse(horse.getUniqueId().toString(), allowedPlayers);

                //Notify the sender
                hMsg(Util.buildString(allowedPlayerNames, ", ", " & ") + (allowedPlayers.size() > 1 ? " are" : " is") + " now allowed on this horse.");
                break;

            //Deny a user from a horse
            //USAGE: /horse deny <Player>
            case "deny":case "denyplayer":
                //Check usage
                if (args.length < 2) throw new UsageException("horse denyPlayer <Player>");

                //Get the horse that the player owns
                horse = getOwnedHorse(hController, player);

                //Get ownerID
                ownerID = getUUIDProfile().getUserID();

                //Parse the player
                UUIDControl.UUIDProfile deniedPlayer = getOfflinePlayer(args[1]);

                //Check if not denying themself
                if (deniedPlayer.getUserID() == ownerID) throw new CommandException("You cannot deny yourself from a horse.");

                //Deny the user
                hController.denyFromHorse(horse.getUniqueId().toString(), deniedPlayer.getUserID());

                //Notify the sender
                hMsg(deniedPlayer.getCurrentName() + " is no longer allowed on this horse!");
                break;

            //Deny all users from using the horse
            //USAGE: /horse private
            case "private":case "denyall":
                //Get the horse that the playe rowns
                horse = getOwnedHorse(hController, player);

                //Deny all users
                hController.denyAllFromHorse(horse.getUniqueId().toString());

                //Notify the sender
                hMsg("You're once again the only player with access to this horse!");
                break;

            //Get a list of all horses a player has
            //USAGE: /horse list
            case "list":case "listhorses":
                //Prevent two heavy commands at once
                checkDoingCommand();

                //Get the userID
                int userID = getUUIDProfile().getUserID();

                //Get all horses
                final HashSet<SavedHorse> savedHorses = hController.getAllHorsesFromUser(userID);
                //=> Check if there are any horses
                if(savedHorses == null) throw new CommandException("You own no horses!");

                //Start heavy command
                addDoingCommand();

                //Update locations of all horses
                for (SavedHorse savedHorse : savedHorses) {
                    //Check if the horse is currently loaded
                    horse = hController.getLoadedHorse(savedHorse.horseUUID);
                    if (horse != null) {
                        //=> Update it
                        savedHorse.updateHorseInformation(horse);
                    }
                }

                //Send loading
                player.sendMessage(ChatColor.GRAY + "Loading horses...");

                //Time to go into aSync
                Util.runASync(new Runnable() {
                    @Override
                    public void run() {
                        //Create a list for the fancy messages
                        ArrayList<FancyMessage> fancyMessages = new ArrayList<>();

                        //Loop through the horses
                        for (SavedHorse savedHorse : savedHorses) {
                            fancyMessages.add(savedHorse.createFancyMessage());
                        }

                        //Send messages to the player
                        hMsg("List of horses " + ChatColor.GRAY + " | Mouse over [INFO] & [KILL]!");
                        for (FancyMessage fm : fancyMessages) {
                            Util.sendFancyMessage(sender, fm);
                        }

                        //Remove doing heavy command
                        removeDoingCommand();
                    }
                });
                break;

            //Get information about a horse
            //USAGE: /horse info
            case "info":
                //Check if the player is on a horse
                if (isOnHorse(player)) {
                    horse = (Horse) player.getVehicle();
                    //Check if the horse is tamed
                    if (!horse.isTamed()) throw new CommandException("This horse isn't tamed yet!");
                    //Check if the horse has an owner
                    if (!hController.hasOwner(horse.getUniqueId().toString())) throw new CommandException("This horse has no owner!");


                    sendHorseInfo(hController, player, horse);
                } else {
                    //=> Enable click on horse
                    hController.addToInfoClickList(player.getUniqueId().toString());
                    hMsg("Right click on the horse you want info about.");
                }
                break;

            //Add a horse to the Hitlist
            //USAGE: /horse hitlist <Horse UUID>
            case "hitlist":case "kill":
                //Check usage
                if (args.length == 1) throw new UsageException("/horse hitlist <Horse UUID>");

                //Checks before putting it on the hitlist
                String horseUUID = args[1];
                //=> Check if the horse is already on the hitlist (especially useful for double clicks etc)
                if (hController.isOnHitlist(horseUUID)) throw new CommandException("This horse is already on the 'hitlist'.");
                //=> Check if the horse is known
                if (!hController.hasOwner(horseUUID)) throw new CommandException("There is no horse known with this UUID. (Possibly already dead?)");
                //=> Check if the player is the owner of that horse
                ownerID = hController.getOwnerID(horseUUID);
                int playerID = getUUIDProfile().getUserID();
                if (ownerID != playerID) throw new CommandException("You aren't the owner of that horse.");

                //Add the horse to the hitlist
                hController.addHorseToHitlist(horseUUID);

                //Notify the sender
                hMsg("The horse has been killed/added to the hitlist.");
                break;


            //Mutation commands
            case "mutate":case "mutation":
                //Check usage
                if (args.length == 1) args = new String[]{"mutate", "help"};

                //Switch the sub command
                switch(args[1].toLowerCase()) {
                    //Mutate a horse into a Zombie or Skeleton horse
                    //USAGE: /horse mutate <Zombie|Skeleton>
                    case "zombie":case "skeleton":case "undead":
                        //Get the owned horse
                        horse = getOwnedHorse(hController, player);

                        //Get UUIDProfile
                        UUIDControl.UUIDProfile profile = getUUIDProfile();

                        //Check if the player is VIP
                        boolean isVIP = plugin.getVip().isVip(profile.getUUID());

                        //Check if the player can mutate horses at all
                        if (hController.getTotalAllowedMutations(profile.getUserID(), isVIP) == 0) {
                            throw new CommandException("You cannot mutate any horses. Get VIP or donate for mutations!");
                        }

                        //Check if the player can mutate a horse
                        int mutatesLeft = hController.getMutatesLeft(profile.getUserID(), isVIP);
                        if (mutatesLeft < 1) throw new CommandException("You cannot mutate any more horses. (Donate for more or kill existing ones)");

                        //Prepare the horse
                        //=> Check if it has a custom name
                        if (horse.getCustomName() == null) throw new CommandException("A " + args[1] + " horse needs to be named by using a name tag!");
                        //=> Check if it has any armor
                        if (horse.getInventory().getArmor() != null)  throw new CommandException("A " + args[1] + " horse cannot have any armor on!");

                        //Mutate the horse
                        //=> Set Variant
                        Horse.Variant variant = null;
                        switch(args[1].toLowerCase()) {
                            case "zombie":case "undead":
                                variant = Horse.Variant.UNDEAD_HORSE;
                                break;
                            case "skeleton":
                                variant = Horse.Variant.SKELETON_HORSE;
                                break;
                        }
                        horse.setVariant(variant);
                        //=> Add the horse to the controller
                        hController.addMutatedHorse(profile.getUserID(), horse.getUniqueId().toString());

                        //Notify the user
                        hMsg("The horse is now mutated!");
                        break;

                    //Get mutation stats
                    //USAGE: /horse mutate stats [Player]
                    case "info":case "stats":case "stat":case "list":
                        //Possibly a heavy command
                        checkDoingCommand();

                        //Check if staff and asking for someone else
                        if (args.length == 3 && Util.testPermission(player, "horse.staff")) {
                            //=> Staff, try to parse name
                            profile = getOfflinePlayer(args[2]);
                        } else {
                            //=> This player
                            profile = getUUIDProfile();
                        }

                        //Check if VIP
                        isVIP = plugin.getVip().isVip(profile.getUUID());

                        //Get the number of allowed horses
                        final int allowedHorses = hController.getTotalAllowedMutations(profile.getUserID(), isVIP);

                        //Get horses
                        final List<SavedHorse> mutatedHorses = hController.getMutatedHorses(profile.getUserID());
                        //=> Count howmany horses mutated
                        final int nrOfMutatedHorses = mutatedHorses.size();

                        //Check if the player is allowed to mutate horses or has any
                        if (allowedHorses == 0 && nrOfMutatedHorses == 0) {
                            //=> Neither
                            hMsg("You are not allowed to mutate horses! Get VIP or donate for horses.");
                            return true;
                        } else if (allowedHorses == 0 && nrOfMutatedHorses == 1) {
                            addDoingCommand();
                            //Go into A-Sync for sending FancyMessages
                            Util.runASync(new Runnable() {
                                @Override
                                public void run() {
                                    //=> Has a mutated left over from VIP
                                    hMsg("You have 1 mutated horse, but you aren't allowed to mutate horses anymore. Rebuy VIP or donate for horses!");
                                    //Create a FancyMessage for the horse
                                    Util.sendFancyMessage(player, mutatedHorses.get(0).createFancyMessage());
                                    //Command is done
                                    removeDoingCommand();
                                }
                            });
                            return true;
                        } else {
                            addDoingCommand();
                            //Go into A-Sync for sending FancyMessages
                            Util.runASync(new Runnable() {
                                @Override
                                public void run() {
                                    //Send the total. Number can be higher than allowed due to losing VIP.
                                    hMsg("You have " + nrOfMutatedHorses + " out of " + allowedHorses + " mutated horses. " + (nrOfMutatedHorses > allowedHorses ? ChatColor.GRAY + "(Lost VIP = -1)" : ""));
                                    //Send all horses
                                    for (SavedHorse horse : mutatedHorses) {
                                        Util.sendFancyMessage(player, horse.createFancyMessage());
                                    }
                                    //Command is done
                                    removeDoingCommand();
                                }
                            });
                        }
                        break;

                    default:
                        throw new UsageException("horse mutate <(Zombie|Skeleton) | (Info|List)>");

                }
                break;

            //The Help Command
            //USAGE: /horse help [Page]
            case "help":
                //Get the page
                int page = 1;
                if (args.length > 1) {
                    page = parseIntPositive(args[1]);
                }

                //Get the HelpMenu
                getHelpMenu().showPage(player, page);
                break;

            //Anything else
            default:
                throw new UsageException("horse help");

        }
        return true;
    }

    @Override
    protected HelpMenu createHelpMenu() {
        return new HelpMenu("Horses", 4,
                HelpMenu.createMenuLine("Horse Info", "Get info about the horse."),
                HelpMenu.createMenuLine("Horse Allow <Player1>..", "Allow one or more players on your horse."),
                HelpMenu.createMenuLine("Horse Deny <Player>", "Deny a player from using your horse."),
                HelpMenu.createMenuLine("Horse list", "Get a list of all your horses."),
                HelpMenu.createMenuLine("Horse Owner <Player>", "Give all rights to another player. You will LOSE ALL rights for that horse."),
                HelpMenu.createMenuLine("Horse Private", "Allow no other players on your horse."),
                HelpMenu.createMenuLine("Horse Mutate <Zombie|Skeleton>", "Mutate a horse! " + ChatColor.GRAY + "[Donate/VIP only]"),
                HelpMenu.createMenuLine("Horse Mutation Stats", "Get the Stats of your mutated horses. " + ChatColor.GRAY + "[Donate/VIP only]")
                );
    }

    /**
     * Get the horse a player is sitting on
     * @param player The player
     * @return The horse
     * @throws CommandException if the player isn't sitting on a horse
     */
    private static Horse getHorse(Player player) throws CommandException {
        //Check if on a horse
        if (!isOnHorse(player)) throw new CommandException(ErrorMsg.NOT_ON_HORSE);
        Horse horse = (Horse) player.getVehicle();
        //=> Check if not a slapVehicle
        if (horse.hasMetadata("slapVehicle")) throw new CommandException("That's SlapVehicle Horse! It will be removed once you unmount it.");
        return horse;
    }

    /**
     * Get the horse that the player is currently riding on & that they own
     * @param hController The The horses controller
     * @param player The player
     * @return The horse
     * @throws CommandException if they aren't on a horse that they own
     */
    private static Horse getOwnedHorse(Horses hController, Player player) throws CommandException {
        if (isOnHorse(player)) {
            //Get the horse
            Horse horse = (Horse) player.getVehicle();

            //Check if tamed
            if (!horse.isTamed()) throw new CommandException("The horse isn't tamed yet.");

            //Check if the owner
            if (isOwner(hController, player, horse)) {
                //Is the owner, return the horse
                return horse;
            }
        }

        //Wasn't able to return the horse
        throw new CommandException(ErrorMsg.NOT_ON_OWNED_HORSE);
    }

    /**
     * Check if a player is on a horse
     * @param player The player
     * @return is on a horse
     */
    private static boolean isOnHorse(Player player) {
        //Check if the vehicle is a horse
        return (player.getVehicle() instanceof Horse);
    }

    /**
     * Check if the player is the owner of a horse
     * @param hController The horses controller
     * @param player The player
     * @param horse The horse
     * @return is the owner
     */
    private static boolean isOwner(Horses hController, Player player, Horse horse) {
        //Get the ID of the owner
        int ownerID = hController.getOwnerID(horse.getUniqueId().toString());

        //Get the Player's ID
        int playerID = UUIDControl.getUserID(player);

        //Return the result
        return (playerID == ownerID);
    }

    /**
     * Send horse information to a player
     * @param hController The The horses controller
     * @param player The The player
     * @param horse The horse
     */
    public static void sendHorseInfo(Horses hController, Player player, Horse horse) {
        String horseUUID = horse.getUniqueId().toString();
        //Get the ID of the owner
        int ownerID = hController.getOwnerID(horseUUID);
        //=> Get profiles
        UUIDControl uuidControl = UUIDControl.getInstance();
        UUIDControl.UUIDProfile ownerProfile = uuidControl.getUUIDProfile(ownerID);
        UUIDControl.UUIDProfile playerProfile = uuidControl.getUUIDProfile(player.getUniqueId().toString());
        //=> Check if owner
        boolean isOwner = (playerProfile.getUserID() == ownerID);


        //Send info
        Util.msg(player, "This horse belongs to " + (isOwner ? "you" : ownerProfile.getCurrentName()) + ".");

        String arrow = ChatColor.GOLD + "  ┗▶ " + ChatColor.WHITE;
        //If owner or staff also send allowed info
        if (isOwner || Util.testPermission(player, "horse.staff")) {

            //Get all allowed players
            List<Integer> allowedIDs = hController.getAllowedUserIDs(horseUUID);
            if (allowedIDs.isEmpty()) {
                //No players
                player.sendMessage(arrow + "Allowed players: " + ChatColor.GRAY + "None");
            } else {
                //Create a list of all playernames
                List<String> playerNames = new ArrayList<>();
                for (Integer userID : allowedIDs) {
                    playerNames.add(uuidControl.getUUIDProfile(userID).getCurrentName());
                }
                //Send msg
                player.sendMessage(arrow + "Allowed players: " + ChatColor.GRAY + Util.buildString(playerNames, ", ", " & "));
            }
        }

        DecimalFormat format = new DecimalFormat("###.#");
        //Stats
        player.sendMessage(arrow + "Jump height: " + ChatColor.GRAY + "\u00B1" + format.format(horse.getJumpStrength() * 10.0) + " blocks");
        player.sendMessage(arrow + "Speed: " + ChatColor.GRAY + "\u00B1" + format.format(getHorseSpeed(horse)) + "x human speed");
    }

    /**
     * Get the horse speed
     * @param horse The horse
     * @return The speed (is multiplier of normal speed)
     */
    private static double getHorseSpeed(Horse horse) {
        AttributeInstance attributes = ((EntityInsentient)((CraftLivingEntity) horse).getHandle()).getAttributeInstance(GenericAttributes.d);
        return attributes.getValue() * 10.0;
    }

}
