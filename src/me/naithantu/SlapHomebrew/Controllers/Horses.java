package me.naithantu.SlapHomebrew.Controllers;

import me.naithantu.SlapHomebrew.Storage.HorseSerializables.MutatedHorsesCollection;
import me.naithantu.SlapHomebrew.Storage.HorseSerializables.SavedHorse;
import me.naithantu.SlapHomebrew.Storage.YamlStorage;
import me.naithantu.SlapHomebrew.Util.Util;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import nl.stoux.SlapPlayers.SlapPlayers;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Player;

import java.util.*;

/**
 * Created by Stoux on 02/09/2014.
 */
public class Horses extends AbstractController {

    //Horses YML File
    private YamlStorage yamlStorage;
    private FileConfiguration config;


    //HashMap containing all SavedHorses
    //K:[Horse UUID] => V:[SavedHorse Object]
    private HashMap<String, SavedHorse> savedHorses;

    //HashMap containing all currently loaded horses
    //K:[Horse UUID] => V:[Horse Entity Object]
    private HashMap<String, Horse> loadedHorses;

    //MultiMap containing a UserID which leads to one or more horse UUIDs
    //K:[UserID of player] => V:[Horse UUID]
    private Multimap<Integer, SavedHorse> playerToHorses;

    //HashMap containg a UserID which leads to a collection of MutatedHorses
    //K:[UserID of player] => V:[MutatedHorsesCollection]
    private HashMap<Integer, MutatedHorsesCollection> mutatedHorses;

    //Hitlist, UUIDs of horses that need to be killed when they are loaded.
    private HashSet<String> hitlist;

    //HashSet containing Player UUIDs that are going to click on a horse to get info
    private HashSet<String> infoClickPlayers;


    public Horses() {
        //Get the Config
        yamlStorage = new YamlStorage(plugin, "horses2");
        config = yamlStorage.getConfig();

        //Create the maps & sets
        savedHorses = new HashMap<>();
        loadedHorses = new HashMap<>();
        playerToHorses = ArrayListMultimap.create();
        mutatedHorses = new HashMap<>();
        infoClickPlayers = new HashSet<>();

        //Load the data from the config
        //=> Horses
        ConfigurationSection horsesSection = config.getConfigurationSection("horses");
        if (horsesSection != null) {
            //Horses Section is available
            for (String horseUUID : horsesSection.getKeys(false)) {
                //Load the saved horse
                SavedHorse savedHorse = (SavedHorse) horsesSection.get(horseUUID);
                //=> Set the UUID
                savedHorse.horseUUID = horseUUID;

                //Put it in the maps
                savedHorses.put(horseUUID, savedHorse);
                playerToHorses.put(savedHorse.ownerID, savedHorse);
            }
        }

        //=> MutatedHorses Collections
        ConfigurationSection mutatedSection = config.getConfigurationSection("mutated");
        if (mutatedSection != null) {
            //Mutated Horses are available
            for (String userIDString : mutatedSection.getKeys(false)) {
                //Parse the ID to an int
                Integer userID = Integer.valueOf(userIDString);

                //Get the collection
                MutatedHorsesCollection collection = (MutatedHorsesCollection) mutatedSection.get(userIDString);
                //=> Set ownerID
                collection.ownerID = userID;

                //Put it into the map
                mutatedHorses.put(userID, collection);
            }
        }

        //=> Hitlist
        if (config.contains("hitlist")) {
            hitlist = new HashSet<>(config.getStringList("hitlist"));
        } else {
            hitlist = new HashSet<>();
        }

        //Check for currently loaded horses
        for (World world : plugin.getServer().getWorlds()) {
            //Get all horses in a world
            Collection<Horse> horses = world.getEntitiesByClass(Horse.class);
            for (Horse horse : horses) {
                //=> Check if the UUID is known
                String horseUUID = horse.getUniqueId().toString();
                if (savedHorses.containsKey(horseUUID)) {
                    //=> Add it to the map
                    loadedHorses.put(horseUUID, horse);
                }
            }
        }

        //Start a Timer to check for unsaved changes. Runs every 5 minutes
        Util.runTimer(new Runnable() {
            @Override
            public void run() {
                if (unsavedChanges) {
                    saveConfig();
                }
            }
        }, 600, 600);
    }


    /*
     *********************
     * General Functions *
     *********************
     */

    /**
     * Check if a horse has an owner registred to it
     * @param horseUUID The UUID of the horse
     * @return has owner
     */
    public boolean hasOwner(String horseUUID) {
        return savedHorses.containsKey(horseUUID);
    }

    /**
     * Get the UserID of the owner of a horse
     * @param horseUUID The UUID of the horse
     * @return The UserID
     */
    public int getOwnerID(String horseUUID) {
        return savedHorses.get(horseUUID).ownerID;
    }

    /**
     * Check if a player is allowed on a horse
     * @param horseUUID The UUID of the horse
     * @param userID The UserID of the player
     * @return is allowed
     */
    public boolean isAllowedOnHorse(String horseUUID, int userID) {
        //Get the horse
        SavedHorse savedHorse = savedHorses.get(horseUUID);

        //Check if the owner
        if (savedHorse.ownerID == userID) return true;

        //Check if any players have been allowed
        List<Integer> allowedPlayers = savedHorse.allowedPlayers;
        if (allowedPlayers == null) return false;
        //Check if the player is allowed
        return allowedPlayers.contains(userID);
    }

    /**
     * Get a list of all userIDs that are allowed on the horse
     * This list does NOT include the owner's ID.
     * @param horseUUID The UUID of the horse
     * @return The list of UserIDs
     */
    public List<Integer> getAllowedUserIDs(String horseUUID) {
        //Get the SavedHorse
        SavedHorse savedHorse = savedHorses.get(horseUUID);

        //Check if list not null
        if (savedHorse.allowedPlayers == null) return new ArrayList<>();

        //Otherwise duplicate the list
        return new ArrayList<>(savedHorse.allowedPlayers);
    }

    /**
     * Check if a horse is currently on the hitlist
     * @param horseUUID The UUID of the horse
     * @return On the hitlist
     */
    public boolean isOnHitlist(String horseUUID) {
        return hitlist.contains(horseUUID);
    }

    /**
     * Add a player to the info click list.
     * @param playerUUID The UUID of the player
     */
    public void addToInfoClickList(String playerUUID) {
        infoClickPlayers.add(playerUUID);
    }

    /**
     * Check if a player has info click enabled.
     * This will remove the player from the info click list if they are in it.
     * So calling it twice can return true & false
     * @param playerUUID The UUID of the player
     * @return is in the info click list
     */
    public boolean isOnInfoClickList(String playerUUID) {
        //Check if the player i
        if (infoClickPlayers.contains(playerUUID)) {
            //=> Remove it from the list & return true
            infoClickPlayers.remove(playerUUID);
            return true;
        } else {
            //=> Not in the list
            return false;
        }
    }


    /**
     * Get all SavedHorses that a user has
     * @param userID The UserID of the player
     * @return the HashSet with SavedHorse objects, can be null.
     */
    public HashSet<SavedHorse> getAllHorsesFromUser(int userID) {
        //Check if the player has any horses
        if (!playerToHorses.containsKey(userID)) {
            return null;
        }

        //Return the HashSet
        return new HashSet<>(playerToHorses.get(userID));
    }

    /**
     * Get a currently loaded horse
     * @param horseUUID The UUID of the horse
     * @return The horse or null
     */
    public Horse getLoadedHorse(String horseUUID) {
        return loadedHorses.get(horseUUID);
    }

    /*
     ***********************
     * Horse Modifications *
     ***********************
     */

    /**
     * Allow a player on a horse
     * @param horseUUID The UUID of the horse
     * @param userIDs The UserIDs of one or more players
     */
    public void allowOnHorse(String horseUUID, Collection<Integer> userIDs) {
        //Get the SavedHorse
        SavedHorse savedHorse = savedHorses.get(horseUUID);

        //Add the IDs
        for (int userID : userIDs) {
            savedHorse.allowUser(userID);
        }
        //=> Modify it in the Config
        setHorseInConfig(savedHorse);
    }

    /**
     * Deny a player from a horse
     * @param horseUUID The UUID of the horse
     * @param userID The UserID of the player
     */
    public void denyFromHorse(String horseUUID, int userID) {
        //Get the SavedHorse
        SavedHorse savedHorse = savedHorses.get(horseUUID);

        //Remove the ID
        savedHorse.denyUser(userID);
        //=> Modify it in the Config
        setHorseInConfig(savedHorse);
    }

    /**
     * Deny all users from a horse
     * @param horseUUID The UUID of the horse
     */
    public void denyAllFromHorse(String horseUUID) {
        //Get the SavedHorse
        SavedHorse savedHorse = savedHorses.get(horseUUID);

        //Reset the allowedPlayers list
        savedHorse.allowedPlayers = null;
        //=> Modify it in the Config
        setHorseInConfig(savedHorse);
    }

    /**
     * Change the owner of a horse
     * This will remove any player that is currently allowed on this horse.
     * @param horse The horse
     * @param newOwner The new owner of the horse
     */
    public void changeOwner(Horse horse, Player newOwner) {
        //Modify the horse
        horse.setOwner(newOwner);

        //Get the SavedHorse
        SavedHorse savedHorse = savedHorses.get(horse.getUniqueId().toString());

        //Get the current owner
        int currentOwner = savedHorse.ownerID;
        //=> Remove it from the playerToHorses map
        playerToHorses.remove(currentOwner, savedHorse);

        //Set the Owner ID
        savedHorse.ownerID = SlapPlayers.getUUIDController().getProfile(newOwner).getID();
        //Remove the list of allowed players
        savedHorse.allowedPlayers = null;

        //Add it to the playerToHorsesMap
        playerToHorses.put(savedHorse.ownerID, savedHorse);

        //Modify the config
        setHorseInConfig(savedHorse);
        saveConfig();
    }

    /**
     * Add a horse to the Hitlist
     * @param horseUUID The UUID of the horse
     */
    public void addHorseToHitlist(String horseUUID) {
        //Check if the horse currently exists
        Horse horse = loadedHorses.get(horseUUID);
        if (horse != null) {
            //=> Horse can be instantly removed
            removeHorseEntity(horse, true);

            //=> No futher action has to be taken
            return;
        } else {
            //=> Add it to the hitlist
            hitlist.add(horseUUID);
            setHitlistInConfig();
        }

        //Remove the horse from the maps
        SavedHorse savedHorse = savedHorses.get(horseUUID);
        //=> Player map
        playerToHorses.remove(savedHorse.ownerID, savedHorse);
        savedHorses.remove(horseUUID);

        //Remove from config
        removeHorseFromConfig(horseUUID);

        //Determine if a mutated horse
        if (mutatedHorses.containsKey(savedHorse.ownerID)) {
            //If the user has mutated horses, try to remove this UUID
            MutatedHorsesCollection collection = mutatedHorses.get(savedHorse.ownerID);
            if (collection.mutatedHorses != null) {
                if (collection.mutatedHorses.contains(horseUUID)) {
                    removeMutatedHorse(savedHorse.ownerID, horseUUID);
                }
            }
        }

        //Save the config
        saveConfig();
    }

    /**
     * Removes a horse from all maps & from the config
     * @param horse The horse
     * @param removeFromLoaded Remove the horse from the Loaded map (this option is here to prevent concurrent modifications)
     */
    private void removeHorseEntity(Horse horse, boolean removeFromLoaded) {
        //Get the horse's UUID
        String horseUUID = horse.getUniqueId().toString();

        //Get the SavedHorse
        SavedHorse savedHorse = savedHorses.get(horseUUID);

        //Remove it from all maps
        if (removeFromLoaded) loadedHorses.remove(horseUUID);
        savedHorses.remove(horseUUID);
        playerToHorses.remove(savedHorse.ownerID, savedHorse);

        //Check if the horse is mutated
        if (mutatedHorses.containsKey(savedHorse.ownerID)) {
            //If the user has mutated horses, try to remove this UUID
            MutatedHorsesCollection collection = mutatedHorses.get(savedHorse.ownerID);
            if (collection.mutatedHorses != null) {
                if (collection.mutatedHorses.contains(horseUUID)) {
                    removeMutatedHorse(savedHorse.ownerID, horseUUID);
                }
            }
        }

        //Kill the horse
        horse.remove();

        //Remove from Config
        removeHorseFromConfig(horseUUID);
        saveConfig();
    }

    /*
     ******************
     * Mutated horses *
     ******************
     */

    /**
     * Get all mutated horses a player has
     * @param userID The UserID of the player
     * @return the list with horses (can be empty)
     */
    public List<SavedHorse> getMutatedHorses(int userID) {
        //Get the collection
        MutatedHorsesCollection collection = mutatedHorses.get(userID);
        //=> Check if it isn't null
        if (collection == null || collection.mutatedHorses == null) {
            return new ArrayList<>();
        }

        List<SavedHorse> horses = new ArrayList<>();
        //Find all the horses in the list
        for (String horseUUID : collection.mutatedHorses) {
            //Get the SavedHorse instance
            SavedHorse horse = savedHorses.get(horseUUID);
            //=> Check if the horse is currently loaded, if so, update data
            if (loadedHorses.containsKey(horseUUID)) {
                horse.updateHorseInformation(loadedHorses.get(horseUUID));
            }

            //Add it to the list
            horses.add(horse);
        }

        //Return the list
        return horses;
    }

    /**
     * Get the number of horses that this player can still mutate
     * @param userID The UserID of the player
     * @param isVIP The player is VIP
     * @return number of mutations
     */
    public int getMutatesLeft(int userID, boolean isVIP) {
        //Get the total number of allowed mutations
        int allowedMutations = getTotalAllowedMutations(userID, isVIP);
        if (allowedMutations == 0) return 0;

        //Get the collection
        MutatedHorsesCollection collection = mutatedHorses.get(userID);
        if (collection != null) {
            //Check if any horses currently mutated
            if (collection.mutatedHorses != null) {
                allowedMutations -= collection.mutatedHorses.size();
            }
        }

        //Return the result
        return allowedMutations;
    }

    /**
     * Get the total number of horses a player can mutate
     * @param userID The UserID of the player
     * @param isVIP The player is VIP
     * @return the number of horses
     */
    public int getTotalAllowedMutations(int userID, boolean isVIP) {
        int allowed = 0;
        //Add a free one if VIP
        if (isVIP) allowed++;

        //Get the collection
        MutatedHorsesCollection collection = mutatedHorses.get(userID);
        if (collection != null) {
            allowed += collection.mutationsAllowed;
        }

        //Return the result
        return allowed;
    }

    /**
     * Add a mutated horse UUID to the collection of a player
     * @param userID The UserID of the player
     * @param horseUUID The UUID of the horse
     */
    public void addMutatedHorse(int userID, String horseUUID) {
        //Get the collection
        MutatedHorsesCollection collection = mutatedHorses.get(userID);
        if (collection == null) {
            //=> Create the collection if it doesn't exist
            collection = new MutatedHorsesCollection(userID, 0, null);
            //=> Put it in the map
            mutatedHorses.put(userID, collection);
        }

        //Add the horse
        collection.addHorse(horseUUID);

        //Save into config
        setCollectionInConfig(collection);
        saveConfig();
    }

    /**
     * Remove a mutated horse from a player's collection
     * @param userID The UserID of the player
     * @param horseUUID The UUID of the horse
     */
    public void removeMutatedHorse(int userID, String horseUUID) {
        //Get the collection
        MutatedHorsesCollection collection = mutatedHorses.get(userID);

        //Remove the horse
        collection.removeHorse(horseUUID);

        //Remove the collection if no horses left & no extras bought
        if (collection.mutatedHorses == null && collection.mutationsAllowed == 0) {
            //Remove from map
            mutatedHorses.remove(userID);

            //Remove from config
            removeCollectionFromConfig(userID);
        } else {
            //Save into config
            setCollectionInConfig(collection);
        }
    }

    /**
     * Allow a user to create more mutated horses
     * @param userID The UserID of the player
     * @param addAllowed The number of extra horses allowed
     */
    public void allowMoreMutatedHorses(int userID, int addAllowed) {
        //Get the collection
        MutatedHorsesCollection collection = mutatedHorses.get(userID);

        //Check if it exists
        if (collection == null) {
            //=> Does not exists yet, create it
            collection = new MutatedHorsesCollection(userID, addAllowed, null);
            mutatedHorses.put(userID, collection);
        } else {
            //=> Does exist, add the number
            collection.mutationsAllowed += addAllowed;
        }

        //Save the changes
        setCollectionInConfig(collection);
        saveConfig();
    }



    /*
     **********
     * Events *
     **********
     */

    /**
     * Action to be taken when a player tames a horse, and thus claiming it.
     * @param player The player
     * @param tamedHorse The tamed horse
     */
    public void onTameEvent(Player player, Horse tamedHorse) {
        //Get the UserID of the player
        int userID = SlapPlayers.getUUIDController().getProfile(player).getID();

        //Create the SavedHorse object
        SavedHorse savedHorse = new SavedHorse(userID, tamedHorse);

        //Modify the horse to not despawn
        tamedHorse.setRemoveWhenFarAway(false);

        //Save the Horse
        String horseUUID = tamedHorse.getUniqueId().toString();
        //=> In the maps
        loadedHorses.put(horseUUID, tamedHorse);
        savedHorses.put(horseUUID, savedHorse);
        playerToHorses.put(userID, savedHorse);
        //=> Config
        setHorseInConfig(savedHorse);
        saveConfig();
    }

    /**
     * Action to be taken when a claimed horse dies
     * @param horse The horse
     */
    public void onDeathEvent(Horse horse) {
        //Remove the horse
        removeHorseEntity(horse, true);
        //Save the config
        saveConfig();
    }

    /**
     * A chunk gets loaded
     * This will add all the owned horses to a map to keep track of
     * @param loadedChunk The chunk
     */
    public void onChunkLoad(Chunk loadedChunk) {
        boolean hitlistChanged = false;

        //Loop through entities
        for (Entity e : loadedChunk.getEntities()) {
            //=> Find horses
            if (e instanceof Horse) {
                String horseUUID = e.getUniqueId().toString();

                //Check if the horse is on the hitlist
                if (hitlist.contains(horseUUID)) {
                    //Remove the entity
                    e.remove();

                    //Update the histlist
                    hitlist.remove(horseUUID);
                    hitlistChanged = true;

                    //Continue to next entity
                    continue;
                }

                //Check if the horse is owned
                if (!hasOwner(horseUUID)) continue;

                //=> Add to map with currently loaded horses
                loadedHorses.put(horseUUID, (Horse) e);
            }
        }

        //Check if the hitlist was changed
        if (hitlistChanged) {
            setHitlistInConfig();
            saveConfig();
        }
    }

    /**
     * A chunk gets unloaded
     * This will try to find all horses in the chunk and remove them from any map/store the data about the horse.
     * @param unloadedChunk The chunk
     */
    public void onChunkUnload(Chunk unloadedChunk) {
        //Loop through entities
        for (Entity e : unloadedChunk.getEntities()) {
            //=> Find horses
            if (e instanceof Horse) {
                String horseUUID = e.getUniqueId().toString();
                //Check if the horse is owned
                if (!hasOwner(horseUUID)) continue;

                //Remove it from the loaded map
                loadedHorses.remove(horseUUID);

                SavedHorse savedHorse = savedHorses.get(horseUUID);
                //Update horse info
                savedHorse.updateHorseInformation((Horse) e);

                //Set in config
                setHorseInConfig(savedHorse);
            }
        }
    }


    /*
     *********************
     * YML Configuration *
     *********************
     */
    //Boolean keeps track of unsaved changes
    private boolean unsavedChanges = false;

    /**
     * Set the horse in the Config
     * @param horse The SavedHorse
     */
    private void setHorseInConfig(SavedHorse horse) {
        config.set("horses." + horse.horseUUID, horse);
        unsavedChanges = true;
    }

    /**
     * Remove a horse from the config
     * @param horseUUID The UUID of the horse
     */
    public void removeHorseFromConfig(String horseUUID) {
        config.set("horses." + horseUUID, null);
        unsavedChanges = true;
    }

    /**
     * Set a MutatedHorsesCollection in the config
     * @param collection The collection
     */
    private void setCollectionInConfig(MutatedHorsesCollection collection) {
        config.set("mutated." + collection.ownerID, collection);
        unsavedChanges = true;
    }

    /**
     * Remove a MutatedHorsesCollection from the config
     * @param userID The UserID of the owner
     */
    private void removeCollectionFromConfig(int userID) {
        config.set("mutated." + userID, null);
        unsavedChanges = true;
    }

    /**
     * Set the hitlist in the config
     */
    private void setHitlistInConfig() {
        if (hitlist.isEmpty()) {
            //Check if there are any entries in the hitlist
            config.set("hitlist", null);
        } else {
            //Create a new arraylist which contains the UUIDs of the hitlist & store it
            config.set("hitlist", new ArrayList<String>(hitlist));
        }
        unsavedChanges = true;
    }

    /**
     * Save the config
     */
    private void saveConfig() {
        yamlStorage.saveConfig();
        unsavedChanges = false;
    }


    @Override
    public void shutdown() {
        //Loop through loaded horses
        for (Map.Entry<String, Horse> entry : loadedHorses.entrySet()) {
            //Check if the horse died
            Horse horse = entry.getValue();
            if (horse.isDead()) {
                //=> Horse is dead. It got killed somehow (plugin?)
                removeHorseEntity(horse, false);
            } else {
                //=> Horse is not dead
                SavedHorse savedHorse = savedHorses.get(entry.getKey());
                //=> Update data
                savedHorse.updateHorseInformation(horse);

                //Put in config
                setHorseInConfig(savedHorse);
            }
        }

        //Set the hitlist
        setHitlistInConfig();

        //Save the config
        saveConfig();

        //Clear maps
        savedHorses.clear();
        loadedHorses.clear();
        mutatedHorses.clear();
        playerToHorses.clear();
    }
}
