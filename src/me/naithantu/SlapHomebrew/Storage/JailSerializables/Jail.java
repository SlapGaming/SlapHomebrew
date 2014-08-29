package me.naithantu.SlapHomebrew.Storage.JailSerializables;

import me.naithantu.SlapHomebrew.Util.Util;
import org.bukkit.Location;
import org.bukkit.configuration.serialization.ConfigurationSerializable;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Stoux on 29-8-2014.
 */
public class Jail implements ConfigurationSerializable {

    //Jail name
    public String name;

    //Jail location
    public Location jailLocation;

    //Allowed
    public boolean chatAllowed;
    public boolean msgAllowed;

    public Jail(String name, Location jailLocation, boolean chatAllowed, boolean msgAllowed) {
        this.name = name;
        this.jailLocation = jailLocation;
        this.chatAllowed = chatAllowed;
        this.msgAllowed = msgAllowed;
    }

    public Jail(Map<String, Object> ymlMap) {
        this.name = (String) ymlMap.get("name");
        this.chatAllowed = (boolean) ymlMap.get("chatallowed");
        this.msgAllowed = (boolean) ymlMap.get("msgallowed");
        //Get the Location map
        Map<String, Object> locationMap = (Map<String, Object>) ymlMap.get("jaillocation");
        jailLocation = Util.ymlMapToLocation(locationMap, null);
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = new HashMap<>();
        map.put("name", name);
        map.put("jaillocation", Util.locationToYmlMap(jailLocation));
        map.put("chatallowed", chatAllowed);
        map.put("msgallowed", msgAllowed);
        return map;
    }

    public static Jail deserialize(Map<String, Object> objectMap) {
        return new Jail(objectMap);
    }
}
