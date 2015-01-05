package me.naithantu.SlapHomebrew.Controllers;

import me.naithantu.SlapHomebrew.Storage.JailSerializables.Jail;
import me.naithantu.SlapHomebrew.Storage.JailSerializables.JailTime;
import me.naithantu.SlapHomebrew.Storage.YamlStorage;
import me.naithantu.SlapHomebrew.Util.DateUtil;
import me.naithantu.SlapHomebrew.Util.Log;
import me.naithantu.SlapHomebrew.Util.Util;
import nl.stoux.SlapPlayers.Model.Profile;
import nl.stoux.SlapPlayers.SlapPlayers;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.*;

/**
 * Created by Stoux on 28-8-2014.
 */
public class Jails extends AbstractController {

    //A map containing all online Jailed players
    //K:[Player's UUID] => V:[JailTime object]
    private HashMap<String, JailTime> onlineJailTimeMap;

    //A map containing all offline Jailed players
    //K:[Player's UUID] => V:[JailTime object]
    private HashMap<String, JailTime> offlineJailTimeMap;

    //Jails, K:[Name] => V:[Jail]
    private HashMap<String, Jail> jails;

    //YAML
    private YamlStorage jailYML;
    private FileConfiguration config;

    public Jails() {
        //Get the YML
        this.jailYML = new YamlStorage(plugin, "jails_v2");
        this.config = jailYML.getConfig();

        //Create the maps
        this.onlineJailTimeMap = new HashMap<>();
        this.offlineJailTimeMap = new HashMap<>();
        this.jails = new HashMap<>();

        //Load the jails
        loadJails();

        //Load JailedTimes
        loadJailedTimes();

        //Activate JailChecker
        Util.runTimer(new Runnable() {
            @Override
            public void run() {
                checkOnlineJailedPlayers(1);
            }
        }, 20, 20);
    }

    /**
     * Load all jails
     */
    private void loadJails() {
        ConfigurationSection jailsSection = config.getConfigurationSection("jails");
        if (jailsSection == null) {
            Log.warn("[Jails] There are currently no Jails!");
            return; //No jails
        }

        //Loop through keys
        for (String name : jailsSection.getKeys(false)) {
            try {
                //Get jail & put in map
                Jail jail = (Jail) jailsSection.get(name);
                jails.put(name.toLowerCase(), jail);
            } catch (Exception e) {
                Log.warn("[Jails] Failed to load Jail: " + name);
            }
        }
    }

    /**
     * Load all Jail sentences
     */
    private void loadJailedTimes() {
        ConfigurationSection jailedSection = config.getConfigurationSection("jailed");
        if (jailedSection == null) {
            return; //No JailedTimes yet
        }

        long maxSentenceAge = (1000 * 60 * 60 * 24) /* 1 Day */ * 90; //Sentence will be wiped after 90 days
        boolean fileChanged = false;
        //Loop through keys
        for (String uuid : jailedSection.getKeys(false)) {
            try {
                JailTime jailTime = (JailTime) jailedSection.get(uuid);
                long sentenceAge = (System.currentTimeMillis() - jailTime.jailedOn);
                if (sentenceAge > maxSentenceAge) { //Sentence is very old
                    if (jailTime.inJail) {
                        //Currently in jail, set the remaining time to 0.
                        jailTime.timeLeft = -1L;
                        offlineJailTimeMap.put(uuid, jailTime);
                    } else {
                        //Remove it
                        jailedSection.set(uuid, null);
                        fileChanged = true;
                    }
                } else {
                    //Add to map
                    offlineJailTimeMap.put(uuid, jailTime);
                }
            } catch (Exception e) {
                Log.warn("[Jails] Failed to load JailedTime with UUID: " + uuid);
            }
        }

        //Check if file changed
        if (fileChanged) {
            //=> Save it if changed
            jailYML.saveConfig();
        }
    }

    /*
     ******************
     * Jail Functions *
     ******************
     */

    /**
     * Check if a Jail exists
     * @param jailName The name of the jail
     * @return exists
     */
    public boolean doesJailExist(String jailName) {
        return jails.containsKey(jailName.toLowerCase());
    }

    /**
     * Create a new Jail
     * @param jailName The name of the jail
     * @param jailLocation The location of the jail
     * @param chatAllowed Allowed chat while in jail
     * @param commandsAllowed Allow social commands while in jail
     */
    public void createJail(String jailName, Location jailLocation, boolean chatAllowed, boolean commandsAllowed) {
        //Create the new jail
        Jail newJail = new Jail(jailName, jailLocation, chatAllowed, commandsAllowed);

        //=> Put in map
        jails.put(jailName.toLowerCase(), newJail);
        //=> Put in Config
        storeAndSave("jails." + jailName.toLowerCase(), newJail);
    }

    /**
     * Delete a Jail
     * @param jailName The name of the jail
     */
    public void deleteJail(String jailName) {
        //Remove from Map
        jails.remove(jailName.toLowerCase());
        //Remove from Config
        storeAndSave("jails." + jailName.toLowerCase(), null);
    }

    /**
     * Get a list of all Jail names
     * @return the list with names
     */
    public List<String> getJailNames() {
        return new ArrayList<>(jails.keySet());
    }

    /**
     * Get a Jail's Location
     * @param name The name of the jail
     * @return The location or null
     */
    public Location getJailLocation(String name) {
        Jail jail = jails.get(name.toLowerCase());
        if (jail == null) return null;
        return jail.jailLocation;
    }

    /*
     *********************
     * Jailing Functions *
     *********************
     */

    /**
     * Jail a player
     * @param player The Player's Profile
     * @param reason The reason
     * @param jail The name of the jail
     * @param jailTime The time left (in milliseconds)
     * @param jailerID The ID of the player who jailed this player
     */
    public void jailPlayer(Profile player, String reason, String jail, long jailTime, int jailerID) {
        //Create the JailTime
        final JailTime jailSentence = new JailTime(player.getID(), reason, jailerID, jail.toLowerCase(), jailTime);

        //Check if the player is online
        Player jailedPlayer = player.getPlayer();
        if (jailedPlayer != null) { //Player is currently online
            //Check world (incase of minigames etc)
            String worldname = jailedPlayer.getWorld().getName().toLowerCase();
            boolean setOldLocToSpawn = false;
            //=> Switch on the worldname
            switch(worldname) {
                case "world_sonic":
                    //=> Minigames world
                    jailedPlayer.performCommand("gleave"); //Leave any StouxGames
                    jailedPlayer.performCommand("sw leave"); //Leave SkyWars
                    setOldLocToSpawn = true;
                    break;

                case "world_pvp":
                    //=> PvP world
                    jailedPlayer.performCommand("pvp leave"); ///Leave any PvP game
                    setOldLocToSpawn = true;
                    break;
            }

            //TODO Schedule a timer to handle the commands being executed

            //Get the OldLocation
            Location oldLoc = (setOldLocToSpawn ? plugin.getServer().getWorld("world_start").getSpawnLocation() : jailedPlayer.getLocation());
            //=> Add to JailSentence
            jailSentence.oldLocation = oldLoc;

            //Teleport the player to Jail
            jailedPlayer.teleport(jails.get(jail.toLowerCase()).jailLocation);
            jailSentence.inJail = true;

            //Put it in the online map
            onlineJailTimeMap.put(player.getUUIDString(), jailSentence);

            Util.msg(jailedPlayer, "You have been jailed! See " + ChatColor.GRAY + "/timeleft" + ChatColor.WHITE + " for more info!");
        } else {
            //Put it in the offline map
            offlineJailTimeMap.put(player.getUUIDString(), jailSentence);
        }

        //Save it in the Config
        storeAndSave("jailed." + player.getUUID(), jailSentence);
    }

    /**
     * Unjail a player
     * @param player The Player's Profile
     * @return successful (True = Success, False = Failed/Not Jailed, Null = Successful, but still in jail while offline)
     */
    public Boolean unjailPlayer(Profile player) {
        String UUID = player.getUUIDString();
        //Check if online or offline
        JailTime sentence;
        if (onlineJailTimeMap.containsKey(UUID)) {
            //=> Player is currently online
            sentence = onlineJailTimeMap.get(UUID);
            onlineJailTimeMap.remove(UUID);

            //Get the player & teleport them back
            Player onlinePlayer = player.getPlayer();
            onlinePlayer.teleport(sentence.oldLocation);
            //=> No longer in jail
            sentence.inJail = false;
        } else if (offlineJailTimeMap.containsKey(UUID)) {
            //=> Player is currently offline
            sentence = offlineJailTimeMap.get(UUID);
            offlineJailTimeMap.remove(UUID);
        } else {
            //=> Not jailed?
            return false;
        }

        //Check if the player is still in Jail
        if (sentence.inJail) {
            //=> Will need to be released from Jail when the player logs in
            sentence.timeLeft = -1L;

            //Put in the offlineMap
            offlineJailTimeMap.put(UUID, sentence);
            //Save in Config
            storeAndSave("jailed." + UUID, sentence);

            //Return null, successful unjail, but still in jail.
            return null;
        } else {
            //=> No longer in jail. Records can be whiped
            storeAndSave("jailed." + UUID, null);
            return true;
        }
    }

    /**
     * A jailed player logs in
     * @param player The player
     */
    public void jailedPlayerLogsIn(final Player player) {
        //Get player's UUID
        final String UUID = player.getUniqueId().toString();

        //Get the sentence
        final JailTime jailSentence = offlineJailTimeMap.get(UUID);
        //=> Remove from map
        offlineJailTimeMap.remove(UUID);

        //Check jailTime
        if (jailSentence.timeLeft <= 0) {
            //=> No time left. Release from Prison
            if (jailSentence.inJail) {
                //Teleport the player to their old Location after a 10 tick delay (due to just logging in)
                Util.runLater(new Runnable() {
                    @Override
                    public void run() {
                        if (player.isOnline()) {
                            player.teleport(jailSentence.oldLocation);
                            Util.msg(player, "You have been unjailed.");
                        }
                    }
                }, 10);
            }

            //Remove from Config
            storeAndSave("jailed." + UUID, null);
        } else {
            //=> Still got time left. Move to Online
            if (!jailSentence.inJail) {
                //If not in Jail yet move them to the jail
                jailSentence.oldLocation = player.getLocation();

                //Vehicle
                player.eject();
                player.leaveVehicle();
                player.closeInventory();

                //Teleport to jail
                Location jailLoc = getJailLocation(jailSentence.jail);
                player.teleport(jailLoc);
                jailSentence.inJail = true;

                //Save in config
                storeAndSave("jailed." + UUID, jailSentence);

                //Broadcast the jail
                Util.broadcastHeader(player.getName() + " has been jailed.");
            }

            //Move to online Map
            onlineJailTimeMap.put(UUID, jailSentence);
        }
    }

    /**
     * A jailed player logs out
     * @param player The player
     */
    public void jailedPlayerLogsOut(Player player) {
        //Get player's UUID
        final String UUID = player.getUniqueId().toString();

        //Get the sentence
        final JailTime sentence = onlineJailTimeMap.get(UUID);
        //=> Remove from online map
        onlineJailTimeMap.remove(UUID);

        //Check if time left
        if (sentence.timeLeft <= 0) {
            //=> Can be released
            player.teleport(sentence.oldLocation);

            //=> Remove from Config
            storeAndSave("jailed." + UUID, null);
        } else {
            //=> Move to offline map
            offlineJailTimeMap.put(UUID, sentence);

            //=> Save in config
            storeAndSave("jailed." + UUID, sentence);
        }
    }

    /*
     ********************
     * Jailed Functions *
     ********************
     */

    /**
     * Check if a player is jailed
     * @param UUID The player's UUID
     * @return is jailed (online or offline)
     */
    public boolean isJailed(String UUID) {
        return (offlineJailTimeMap.containsKey(UUID) || onlineJailTimeMap.containsKey(UUID));
    }

    /**
     * Get the Jail a player is in
     * @param UUID The player's UUID
     * @return The jail
     */
    private Jail getPlayerJail(String UUID) {
        JailTime jailTime = onlineJailTimeMap.get(UUID);
        return jails.get(jailTime.jail);
    }

    /**
     * Check if a player is allowed to msg (social commands) while jailed
     * @param UUID The player's UUID
     * @return allowed
     */
    public boolean isAllowedToMsg(String UUID) {
        return getPlayerJail(UUID).msgAllowed;
    }

    /**
     * Check if a player is allowed to chat while jailed
     * @param UUID The player's UUID
     * @return allowed
     */
    public boolean isAllowedToChat(String UUID) {
        return getPlayerJail(UUID).chatAllowed;
    }

    /**
     * Send the JailTime sentence info to the player who's jailed
     * @param p The player
     * @param UUID The player's UUID
     */
    public void sendPlayerJailInfo(Player p, String UUID) {
        JailTime sentence = onlineJailTimeMap.get(UUID);
        Util.msg(p, "You are currently jailed for: " + Util.getTimePlayedString(sentence.timeLeft));
        p.sendMessage(ChatColor.GOLD + "  ┗▶ " + ChatColor.WHITE + "Reason: " + ChatColor.GRAY + sentence.reason);
    }

    /**
     * Send all the JailTime sentence info to teh Staff member who requested it
     * @param staff The staffmember
     * @param jailedPlayer The Profile of the jailed player
     */
    public void sendStaffJailInfo(CommandSender staff, Profile jailedPlayer) {
        String UUID = jailedPlayer.getUUIDString();

        //Get the JailTime
        JailTime sentence = offlineJailTimeMap.get(UUID);
        if (sentence == null) {
            sentence = onlineJailTimeMap.get(UUID);
        }

        //Some strings
        String goldArrow = ChatColor.GOLD + "  ┗▶ " + ChatColor.WHITE;
        String W = ChatColor.WHITE.toString();
        String G = ChatColor.GREEN.toString();


        //Get the jailer's name
        String jailerName = SlapPlayers.getUUIDController().getProfile(sentence.jailerID).getCurrentName();
        //Get the date
        String date = DateUtil.format("dd/MM/yyyy HH:mm", sentence.jailedOn);

        //Send info
        Util.msg(staff, jailedPlayer.getCurrentName() + " has been jailed by " + G + jailerName + W + ".");
        staff.sendMessage(goldArrow + "Jail: " + G + sentence.jail); //TODO Add a TP button (Fanciful)
        staff.sendMessage(goldArrow + "Time left: " + G + Util.getTimePlayedString(sentence.timeLeft));
        staff.sendMessage(goldArrow + "Jailed on: " + G + date);
        staff.sendMessage(goldArrow + "Reason: " + ChatColor.GRAY + sentence.reason);
    }

    //A Set that is being used by checkOnlineJailedPlayers to store UserIDs in (for unjailing them)
    private HashSet<Integer> unjailPlayerIDs = new HashSet<>();

    /**
     * Check if any player needs to be unjailed that's currently online doing time.
     * @param takeOffSeconds The number of seconds taken off their jailtime
     */
    public void checkOnlineJailedPlayers(int takeOffSeconds) {
        //Check if any online players are online
        if (onlineJailTimeMap.isEmpty()) return;

        //Calculate millis
        long takeMillisOff = takeOffSeconds * 1000;

        //Loop through jailed players and take [takeOffSeconds] off their jail time
        for (Map.Entry<String, JailTime> entry : onlineJailTimeMap.entrySet()) {
            //Get the JailTime
            JailTime jailTime = entry.getValue();

            //Take the seconds off
            long timeLeft = jailTime.timeLeft - takeMillisOff;
            jailTime.timeLeft = timeLeft;

            //Check if the player should be released
            if (timeLeft <= 0) {
                //Add the ID to be unjailed
                unjailPlayerIDs.add(jailTime.userID);
            }
        }

        //Unjail players
        if (!unjailPlayerIDs.isEmpty()) {
            //=> Loop through IDs
            for (Integer userID : unjailPlayerIDs) {
                //=> Unjail the player
                Profile profile = SlapPlayers.getUUIDController().getProfile(userID);
                unjailPlayer(profile);
                Util.msg(profile.getPlayer(), "Your jail sentence has ended!");
            }

            //=> Clear Set
            unjailPlayerIDs.clear();
        }

    }

    /**
     * Store an Object in the YML file & force a save after that.
     * @param path The path to the object
     * @param object The object
     */
    private void storeAndSave(String path, Object object) {
        config.set(path, object);
        jailYML.saveConfig();
    }

    @Override
    public void shutdown() {

    }
}
