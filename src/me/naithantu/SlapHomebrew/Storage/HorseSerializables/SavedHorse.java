package me.naithantu.SlapHomebrew.Storage.HorseSerializables;

import me.naithantu.SlapHomebrew.Util.Util;
import mkremins.fanciful.FancyMessage;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.Horse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Stoux on 03/09/2014.
 */
public class SavedHorse implements ConfigurationSerializable {

    //The UUID of the horse
    public String horseUUID;

    //The ID (UUIDProfile ID) of the owner
    public int ownerID;

    //List with UserIDs that are allowed on this horse
    public List<Integer> allowedPlayers;


    /* Horse Information */
    //The custom name of the horse (Name Tag)
    public String customName;

    //Horse color
    public String horseColor;

    //Horse style
    public String horseStyle;

    //Horse Variant
    public String horseVariant;

    //Last known location
    public Location lastKnownLocation;

    /**
     * Create a new SavedHorse object
     * @param ownerID The ID of the owner
     * @param horse The horse
     */
    public SavedHorse(int ownerID, Horse horse) {
        this.ownerID = ownerID;

        //Set horse info
        horseUUID = horse.getUniqueId().toString();
        updateHorseInformation(horse);
    }

    public SavedHorse(Map<String, Object> map) {
        //General data
        this.ownerID = (int) map.get("owner");
        if (map.containsKey("allowed")) {
            allowedPlayers = (List<Integer>) map.get("allowed");
        }

        //Horse info
        if (map.containsKey("name")) {
            this.customName = (String) map.get("name");
        }
        if (map.containsKey("color")) {
            this.horseColor = (String) map.get("color");
        }
        if (map.containsKey("style")) {
            this.horseStyle = (String) map.get("style");
        }
        if (map.containsKey("variant")) {
            this.horseVariant = (String) map.get("variant");
        }
        if (map.containsKey("location")) {
            Map<String, Object> locationMap = (Map<String, Object>) map.get("location");
            this.lastKnownLocation = Util.ymlMapToLocation(locationMap, null);
        }
    }

    /**
     * Allow a player to the allowed players list
     * @param userID The UserID
     */
    public void allowUser(int userID) {
        //Create a list if it doesn't exist
        if (allowedPlayers == null) allowedPlayers = new ArrayList<>();

        //Add the ID
        if (!allowedPlayers.contains(userID)) {
            allowedPlayers.add(userID);
        }
    }

    /**
     * Remove a player from the allowed players list
     * @param userID The UserID
     */
    public void denyUser(int userID) {
        if (allowedPlayers != null) {
            //Remove the ID
            allowedPlayers.remove(new Integer(userID));

            //Null the list if there are no players left
            if (allowedPlayers.isEmpty()) {
                allowedPlayers = null;
            }
        }
    }

    /**
     * Update all the information to the current state of the horse
     * @param horse The horse
     */
    public void updateHorseInformation(Horse horse) {
        customName = horse.getCustomName(); //Can be null
        horseVariant = horse.getVariant().toString();
        if (horse.getVariant() == Horse.Variant.UNDEAD_HORSE | horse.getVariant() == Horse.Variant.SKELETON_HORSE) {
            horseColor = null;
            horseStyle = null;
        } else {
            horseColor = horse.getColor().toString();
            horseStyle = horse.getStyle().toString();
        }
        lastKnownLocation = horse.getLocation();
    }

    public FancyMessage createFancyMessage() {
        //Determine the name in the send line
        String lineName = customName;
        if (lineName == null) {
            //Check if variant is known
            if (horseVariant == null) {
                lineName = "Unknown horse";
            } else {
                //Switch on the variant
                switch(horseVariant) {
                    case "DONKEY":case "HORSE":case "MULE":
                        //Try to add the color to the name
                        if (horseColor != null) {
                            lineName = capitalizeFirstLetter(horseColor + " " + horseVariant);
                        } else {
                            lineName = capitalizeFirstLetter(horseVariant);
                        }
                        break;
                    case "UNDEAD_HORSE":
                        lineName = "Undead horse";
                        break;
                    case "SKELETON_HORSE":
                        lineName = "Skeleton horse";
                        break;
                }
            }
        }

        //Create the Kill FancyMessage
        FancyMessage[] killMessage = new FancyMessage[] {
                new FancyMessage("Click on this & press enter to kill/remove this horse.").color(ChatColor.RED),
                new FancyMessage("THIS CANNOT BE UNDONE!").color(ChatColor.DARK_RED)
        };

        //Create the info message
        FancyMessage[] infoLines;
        //=> Check if we know anything bout this horse
        if (horseVariant == null) {
            //The horse variant is null. This is only null if the horse has never been seen since the new system
            infoLines = new FancyMessage[] {
                    new FancyMessage("This horse hasn't been seen since this new system.").color(ChatColor.RED),
                    new FancyMessage("Thus it is impossible to show more info about this horse.").color(ChatColor.RED)
            };
        } else {
            final ChatColor GREEN = ChatColor.GREEN;
            final ChatColor GOLD = ChatColor.GOLD;
            //Check if mutated
            boolean mutated = (horseVariant.equals("UNDEAD_HORSE") || horseVariant.equals("SKELETON_HORSE"));

            //Horse has been found since new system
            infoLines = new FancyMessage[mutated ? 5 : 7];

            int line = 0;
            //Set the name & variant
            boolean hasName = (customName != null);
            infoLines[line++] = new FancyMessage("Name: ").then(hasName ? customName : "none").color(hasName ? GREEN : ChatColor.RED);
            infoLines[line++] = new FancyMessage("Variant: ").then(capitalizeFirstLetter(horseVariant)).color(GOLD);

            //Only add if not mutated
            if (!mutated) {
                infoLines[line++] = new FancyMessage("Style: ").then(capitalizeFirstLetter(horseStyle)).color(GREEN);
                infoLines[line++] = new FancyMessage("Color: ").then(capitalizeFirstLetter(horseColor)).color(GOLD);
            }

            //Location
            infoLines[line++] = new FancyMessage("Last known location:");
            infoLines[line++] =
                    new FancyMessage("   X: ").then(lastKnownLocation.getBlockX() + "").color(GREEN)
                    .then(" | Y: ").then(lastKnownLocation.getBlockY() + "").color(GREEN)
                    .then(" | Z: ").then(lastKnownLocation.getBlockZ() + "").color(GREEN);
            infoLines[line++] = new FancyMessage("   World: ").then(getWorld(lastKnownLocation.getWorld().getName())).color(GOLD);
        }

        //Create the full message
        FancyMessage fullMessage = new FancyMessage("  ┗▶ ").color(ChatColor.GOLD)
                .then(lineName + " ")
                .then("[INFO]").color(ChatColor.DARK_AQUA).formattedTooltip(infoLines)
                .then(" | ")
                .then("[KILL]").color(ChatColor.DARK_RED).suggest("/horse hitlist " + horseUUID).formattedTooltip(killMessage);

        //Return the Fancy message
        return fullMessage;
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = new HashMap<>();
        //General data
        map.put("owner", ownerID);
        putIfNotNull(map, "allowed", allowedPlayers);

        //Horse info
        putIfNotNull(map, "name", customName);
        putIfNotNull(map, "color", horseColor);
        putIfNotNull(map, "style", horseStyle);
        putIfNotNull(map, "variant", horseVariant);
        if (lastKnownLocation != null) {
            map.put("location", Util.locationToYmlMap(lastKnownLocation));
        }
        return map;
    }

    /**
     * Put an object in the map if the object is not null
     * @param map The target map
     * @param key The key of the object
     * @param object The object
     */
    private void putIfNotNull(Map<String, Object> map, String key, Object object) {
        if (object != null) {
            map.put(key, object);
        }
    }

    /**
     * Capitalize the first letter, lowercase the rest of the string.
     * Also replaces all _'s with spaces
     * @param s The string
     * @return The result
     */
    private static String capitalizeFirstLetter(String s) {
        String ss = s.replace("_", " ");
        return ss.substring(0, 1).toUpperCase() + ss.substring(1).toLowerCase();
    }

    /**
     * Try to get a better world name
     * @param world The world
     * @return The (better) worldname
     */
    private static String getWorld(String world) {
        if (world.contains("resource")) {
            world = "world_resource";
        }
        switch(world) {
            case "world":
                return "Old Survival";
            case "world_survival3":
                return "New Survival";
            case "world_resource":
                return "Resource World";
            case "world_creative":
                return "Creative World";
            case "world_the_end":
                return "The End";
            case "world_nether":
                return "The Nether";
            default:
                return world;
        }
    }


    public static SavedHorse deserialize(Map<String, Object> objectMap) {
        return new SavedHorse(objectMap);
    }
}
