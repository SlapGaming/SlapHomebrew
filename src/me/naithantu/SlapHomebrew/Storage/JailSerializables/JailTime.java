package me.naithantu.SlapHomebrew.Storage.JailSerializables;

import me.naithantu.SlapHomebrew.Util.Util;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.serialization.ConfigurationSerializable;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Stoux on 29-8-2014.
 */
public class JailTime implements ConfigurationSerializable {

    //Person
    public int userID;

    //Jailer
    public String reason;
    public int jailerID;

    //Jail info
    public String jail;
    public boolean inJail;

    //Time
    public Long timeLeft;
    public Long jailedOn; //Timestamp

    //Old location of the player
    public Location oldLocation;

    public JailTime(int userID, String reason, int jailerID, String jail, long timeLeft) {
        this.userID = userID;
        this.reason = reason;
        this.jailerID = jailerID;
        this.jail = jail;
        this.timeLeft = timeLeft;
        this.jailedOn = System.currentTimeMillis();

        //Not in jail yet
        this.inJail = false;
        this.oldLocation = null;
    }

    public JailTime(Map<String, Object> objectMap) {
        //Default data
        this.userID = (int) objectMap.get("userid");
        this.reason = (String) objectMap.get("reason");
        this.jailerID = (int) objectMap.get("jailerid");
        this.jail = (String) objectMap.get("jail");
        this.inJail = (boolean) objectMap.get("injail");

        //Longs
        this.timeLeft = Util.loadLongValueFromYmlMap(objectMap.get("timeleft"));
        this.jailedOn = Util.loadLongValueFromYmlMap(objectMap.get("jailedon"));

        //Check if in jail
        if (inJail) {
            //Get the old location map
            Map<String, Object> locMap = (Map<String, Object>) objectMap.get("oldloc");
            //=> To location
            Location altLocation = Bukkit.getServer().getWorld("world_start").getSpawnLocation();
            this.oldLocation = Util.ymlMapToLocation(locMap, altLocation);
        } else {
            this.oldLocation = null;
        }
    }

    @Override
    public Map<String, Object> serialize() {
        //Create the map
        Map<String, Object> ymlMap = new HashMap<>();

        //Set required info
        ymlMap.put("userid", userID);
        ymlMap.put("reason", reason);
        ymlMap.put("jailerid", jailerID);
        ymlMap.put("jail", jail);
        ymlMap.put("injail", inJail);
        ymlMap.put("timeleft", timeLeft);
        ymlMap.put("jailedon", jailedOn);

        //Check if in jail
        if (inJail) {
            //Put the oldLocation in the main YML map
            ymlMap.put("oldloc", Util.locationToYmlMap(oldLocation));
        }
        return ymlMap;
    }

    public static JailTime deserialize(Map<String, Object> objectMap) {
        return new JailTime(objectMap);
    }

}
