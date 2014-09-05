package me.naithantu.SlapHomebrew.Storage.HorseSerializables;

import org.bukkit.configuration.serialization.ConfigurationSerializable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Stoux on 03/09/2014.
 */
public class MutatedHorsesCollection implements ConfigurationSerializable {

    //The ID of the owner
    public int ownerID;

    //The number of allowed mutated horses
    //WARNING: This does NOT include the bonus one received for VIP
    public int mutationsAllowed;

    //The UUIDs of the mutated horses
    public List<String> mutatedHorses;

    public MutatedHorsesCollection(int ownerID, int mutationsAllowed, List<String> mutatedHorses) {
        this.ownerID = ownerID;
        this.mutationsAllowed = mutationsAllowed;
        this.mutatedHorses = mutatedHorses;
    }

    public MutatedHorsesCollection(Map<String, Object> ymlMap) {
        this.mutationsAllowed = (int) ymlMap.get("allowed");
        if (ymlMap.containsKey("horses")) {
            mutatedHorses = (List<String>) ymlMap.get("horses");
        }
    }

    /**
     * Add a horse to the collection
     * @param horseUUID The UUID of the horse
     */
    public void addHorse(String horseUUID) {
        //Create the list if it doesn't exists
        if (mutatedHorses == null) {
            mutatedHorses = new ArrayList<>();
        }

        //Add the horse
        if (!mutatedHorses.contains(horseUUID)) {
            mutatedHorses.add(horseUUID);
        }
    }

    /**
     * Remove a horse from the collection
     * @param horseUUID The UUID of the horse
     */
    public void removeHorse(String horseUUID) {
        //Remove it form the list
        mutatedHorses.remove(horseUUID);

        //Check if list should be nullified
        if (mutatedHorses.isEmpty()) {
            mutatedHorses = null;
        }
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = new HashMap<>();
        map.put("allowed", mutationsAllowed);
        if (mutatedHorses != null) {
            map.put("horses", mutatedHorses);
        }
        return map;
    }

    public static MutatedHorsesCollection deserialize(Map<String, Object> map) {
        return new MutatedHorsesCollection(map);
    }
}
