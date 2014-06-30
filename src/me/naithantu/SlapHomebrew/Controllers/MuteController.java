package me.naithantu.SlapHomebrew.Controllers;

import me.naithantu.SlapHomebrew.Storage.YamlStorage;
import me.naithantu.SlapHomebrew.Util.Util;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Created by Stoux (http://www.stoux.nl)
 * Created on 28-6-14
 * Project: SLAP Code
 */
public class MuteController extends AbstractController {

    //Constants
    private static final String PLAYERS = "players";
    private static final String PLAYER = PLAYERS + ".%s.%s";
    private static final String REASON = "reason";
    private static final String MUTED_BY = "mutedby";
    private static final String END_TIME = "endtime";

    //Yaml
    private YamlStorage storage;
    private FileConfiguration config;

    //Loaded data
    private ArrayList<Muted> mutedList;
    //K:[UUID of player] => V:[Muted object]
    private HashMap<String, Muted> mutedMap;

    public MuteController() {
        //Load config
        storage = new YamlStorage(plugin, "muted");
        config = storage.getConfig();

        //Load data
        loadData();

        //Start MuteCheck task, check muted people every 10 seconds
        Util.runASyncTimer(new MuteCheck(), 20 * 10, 20 * 10);
    }

    //<editor-fold desc="YAML private stuff">
    /**
     * Load all the data from the YML file
     */
    private void loadData() {
        //Refresh collections
        mutedList = new ArrayList<>();
        mutedMap = new HashMap<>();

        //Get section
        ConfigurationSection section = config.getConfigurationSection(PLAYERS);
        //=> check if exists
        if (section == null) return;

        //Get content
        for (String UUID : section.getKeys(false)) {
            //Get content for this UUID
            String reason = config.getString(String.format(PLAYER, UUID, REASON));
            String mutedBy = config.getString(String.format(PLAYER, UUID, MUTED_BY));
            long endTime = config.getLong(String.format(PLAYER, UUID, END_TIME));

            //Create muted object
            Muted muted = new Muted(UUID, reason, endTime, mutedBy);

            //Add to collections
            mutedList.add(muted);
            mutedMap.put(UUID, muted);
        }
    }

    /**
     * Save the muted object in the Yaml
     * @param muted The object
     */
    private void saveMuted(Muted muted) {
        //Set values
        config.set(String.format(PLAYER, muted.playerUUID, REASON), muted.reason);
        config.set(String.format(PLAYER, muted.playerUUID, MUTED_BY), muted.mutedBy);
        config.set(String.format(PLAYER, muted.playerUUID, END_TIME), muted.endTime);

        //Save config
        storage.saveConfig();
    }

    /**
     * Remove a muted entry from the YAML
     * @param muted The muted object that needs to be removed
     */
    private void removeMuted(Muted muted) {
        removeMuted(muted.playerUUID);
    }

    /**
     * Remove a muted entry from the YAML
     * @param UUID the UUID
     */
    private void removeMuted(String UUID) {
        config.set(PLAYERS + "." + UUID, null);
        storage.saveConfig();
    }

    /**
     * Class to keep track of a mute
     */
    private class Muted {

        private String playerUUID;
        private String reason;
        private long endTime;
        private String mutedBy;

        public Muted(String UUID, String reason, long endTime, String mutedBy) {
            this.playerUUID = UUID;
            this.reason = reason;
            this.endTime = endTime;
            this.mutedBy = mutedBy;
        }
    }
    //</editor-fold>

    //<editor-fold desc="Public functions">
    /**
     * Check if a player (UUID) is muted
     * @param UUID The UUID of the player
     * @return is muted
     */
    public boolean isMuted(String UUID) {
        return mutedMap.containsKey(UUID);
    }

    /**
     * Get the reason for muting a player
     * @param UUID The UUID of the player
     * @return The reason or null if not muted
     */
    public String getMutedReason(String UUID) {
        Muted muted = mutedMap.get(UUID);
        if (muted == null) return null;
        return muted.reason;
    }

    /**
     * Get the timestamp when a mute ends
     *
     * This will return:
     *  -1 = Perm mute
     *  0 = Not muted
     *  Anything else = Unix timestamp in millis
     *
     * @param UUID The UUID of the player
     * @return the timestamp
     */
    public long mutedTill(String UUID) {
        if (mutedMap.containsKey(UUID)) {
            return mutedMap.get(UUID).endTime;
        } else {
            return 0;
        }
    }

    /**
     * Mute a player
     * @param UUID The UUID of the player
     * @param reason The reason for muting the player
     * @param mutedBy The UUID of the person who muted the player
     * @param endTime The time the mute should end. -1 for perm mute.
     */
    public void setMuted(String UUID, String reason, String mutedBy, long endTime) {
        //Check if already muted
        if (isMuted(UUID)) {
            mutedList.remove(mutedMap.get(UUID));
        }

        //Create new Muted object
        Muted muted = new Muted(UUID, reason, endTime, mutedBy);

        //Add to collections
        mutedList.add(muted);
        mutedMap.put(UUID, muted);

        //Save in YAML
        saveMuted(muted);
    }

    /**
     * Unmute a player
     * @param UUID The UUID of the player
     * @return unmuted (if false is returned the player wasn't muted)
     */
    public boolean unmute(String UUID) {
        //Check if actually muted
        if (!mutedMap.containsKey(UUID)) {
            return false;
        }

        //Remove mute from YAML
        removeMuted(UUID);
        //=> From collections
        mutedList.remove(mutedMap.get(UUID));
        mutedMap.remove(UUID);

        return true;
    }
    //</editor-fold>

    private class MuteCheck extends BukkitRunnable {

        @Override
        public void run() {
            //Copy set
            HashSet<Muted> mutedSet;
            synchronized (mutedList) {
                mutedSet = new HashSet<Muted>(mutedList);
            }

            //Current time
            long currentTime = System.currentTimeMillis();

            //Loop through set
            for (Muted muted : mutedSet) {
                if (muted.endTime < currentTime && muted.endTime != -1) { //Check if end time of mute passed
                    mutedList.remove(muted);
                    mutedMap.remove(muted.playerUUID);
                }
            }

            //Clear set
            mutedSet.clear();
        }
    }


    @Override
    public void shutdown() {

    }
}
