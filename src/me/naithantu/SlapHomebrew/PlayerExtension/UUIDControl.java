package me.naithantu.SlapHomebrew.PlayerExtension;

import me.naithantu.SlapHomebrew.Util.Log;
import me.naithantu.SlapHomebrew.Util.SQLPool;
import me.naithantu.SlapHomebrew.Util.Util;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.sql.*;
import java.util.*;

/**
 * Created by Stoux on 23-8-2014.
 */
public class UUIDControl {

    //Static instance
    private static UUIDControl instance;

    //Setup boolean, true = working, false = not working.
    private boolean working = false;


    //Map which has the String version of a player's UUID to their UUID Profile
    private HashMap<String, UUIDProfile> uuidToProfile;

    //Map which has the User ID (in the database) to their UUID Profile
    private HashMap<Integer, UUIDProfile> idToProfile;

    //Multimap which has a playername lead to one or more User IDs (Database)
    private Multimap<String, Integer> playerToIDs;

    //Set which contains UUID scurrently being added
    private HashSet<String> uuidsBeingAdded;

    public UUIDControl() {
        instance = this;
        uuidToProfile = new HashMap<>();
        idToProfile = new HashMap<>();
        playerToIDs = ArrayListMultimap.create();
        uuidsBeingAdded = new HashSet<>();

        //Load all current entries
        loadFromDatabase();
    }

    /**
     * Load all entries currently in the database.
     */
    private void loadFromDatabase() {
        Connection con = SQLPool.getConnection();
        try {
            //Get all 'Users'. UUID -> ID
            ResultSet users = con.createStatement().executeQuery("SELECT `user_id` as `ID`, `UUID` FROM `sh_user`;");
            while(users.next()) {
                //Get data
                int ID = users.getInt(1);
                String UUID = users.getString(2);

                //Create the profile
                UUIDProfile profile = new UUIDProfile(ID, UUID);
                //Add the profile to the maps
                uuidToProfile.put(UUID, profile);
                idToProfile.put(ID, profile);
            }

            int currentID = -1;
            ArrayList<NameProfile> profiles = new ArrayList<>();
            //Get all names
            ResultSet names = con.createStatement().executeQuery("SELECT `user_id`, `name`, `known_since` FROM `sh_names` ORDER BY `user_id` ASC;");
            while(names.next()) {
                //Get data
                int ID = names.getInt(1);
                String name = names.getString(2);
                long knownSince = names.getLong(3);

                //Check if still adding to the same UUID Profile
                if (currentID != -1) {
                    if (currentID != ID) { //Otherwise store them
                        UUIDProfile uuidProfile = idToProfile.get(currentID);
                        uuidProfile.addNameProfiles(profiles);
                        profiles.clear();
                    }
                }

                //Set current ID
                currentID = ID;

                //Create the profile
                NameProfile nProfile = new NameProfile(ID, name, knownSince);
                profiles.add(nProfile);

                //Add to the map
                playerToIDs.put(name.toLowerCase(), ID);
            }

            //Store any remaining profiles
            if (currentID != -1) {
                UUIDProfile uuidProfile = idToProfile.get(currentID);
                uuidProfile.addNameProfiles(profiles);
            }

            //Clear profiles
            profiles = null;

            //Set value
            working = true;

            //Log
            Log.info("[UUIDControl] Loaded " + idToProfile.size() + " UUID Profiles.");
            Log.info("[UUIDControl] Found " + playerToIDs.size() + " different Playernames.");
        } catch (SQLException e) {
            Log.severe("[UUIDControl] Failed to get all users. SlapHomebrew cannot function without this. Shutting down.");
        } finally {
            SQLPool.returnConnection(con);
        }
    }

    /**
     * UUIDControl's function for when a player logs in.
     *
     * This function will add a new UUID to the database or add a new Playername to a UUID.
     * @param p The player that logged in.
     */
    public void onLogin(final Player p) {
        final String UUID = p.getUniqueId().toString();
        final long loginTime = System.currentTimeMillis();
        final String playername = p.getName();
        //Check if player is already known
        if (uuidToProfile.containsKey(UUID)) {
            //UUID is already known, check playername.
            UUIDProfile profile = uuidToProfile.get(UUID);
            NameProfile latestName = profile.names.get(0);
            if (!latestName.playername.equalsIgnoreCase(playername)) {
                //Has a new name
                final NameProfile newName = new NameProfile(profile.userID, playername, loginTime);
                //Add it to the maps
                playerToIDs.put(p.getName().toLowerCase(), profile.userID);

                //Store the new name
                Util.runASync(new Runnable() {
                    @Override
                    public void run() {
                        newName.insertInDatabase();
                    }
                });
            }
        } else {
            //UUID is not known yet
            uuidsBeingAdded.add(UUID);
            Log.info("New UUID detected (" + UUID + ")! Adding to database.");

            //Add to the database
            Util.runASync(new Runnable() {
                @Override
                public void run() {
                    Connection con = SQLPool.getConnection();
                    try {
                        //Prepare the statement
                        PreparedStatement prep = con.prepareStatement("INSERT INTO `sh_user`(`UUID`) VALUES (?);", Statement.RETURN_GENERATED_KEYS);
                        //Set data
                        prep.setString(1, UUID);
                        //Execute & get keys
                        prep.executeUpdate();
                        ResultSet key = prep.getGeneratedKeys();
                        key.next();
                        int ID = key.getInt(1);

                        //Create the profile
                        UUIDProfile newProfile = new UUIDProfile(ID, UUID);
                        NameProfile newName = new NameProfile(ID, playername, loginTime);
                        newProfile.addNameProfile(newName);

                        //Put it in the maps
                        synchronized (uuidToProfile) {
                            uuidToProfile.put(UUID, newProfile);
                        }
                        synchronized (idToProfile) {
                            idToProfile.put(ID, newProfile);
                        }
                        synchronized (playerToIDs) {
                            playerToIDs.put(playername.toLowerCase(), ID);
                        }
                        synchronized (uuidsBeingAdded){
                            uuidsBeingAdded.remove(UUID);
                        }

                        //Log
                        Log.info("New UUID Profile added. ID:" + ID + " | UUID: " + UUID + " | P: " + playername);

                        //Insert the name into the database
                        newName.insertInDatabase();
                    } catch (SQLException e) {
                        Log.severe("Failed to register new UUID Profile (UUID: " + UUID + ").");
                        e.printStackTrace();
                    } finally {
                        SQLPool.returnConnection(con);
                    }
                }
            });
        }
    }

    /**
     * Check if the UUID Control is working.
     * @return is working
     */
    public boolean isWorking() {
        return working;
    }

    /**
     * Get a player's UUIDProfile based on their UUID
     * @param UUID the UUID
     * @return the profile or null
     */
    public UUIDProfile getUUIDProfile(UUID UUID) {
        return getUUIDProfile(UUID.toString());
    }

    /**
     * Get a player's UUIDProfile based on their UUID
     * @param UUID the UUID as String
     * @return the profile or null
     */
    public UUIDProfile getUUIDProfile(String UUID) {
        return uuidToProfile.get(UUID);
    }

    /**
     * Get a player's UUIDProfile based on their User ID as specified in the database
     * @param userID the UserID
     * @return the profile or null
     */
    public UUIDProfile getUUIDProfile(int userID) {
        return idToProfile.get(userID);
    }

    /**
     * Get the UserIDs that have ever used this playername
     * @param playername The name of the player
     * @return a collection of UserIDs or null if the name has never been used
     */
    public Collection<Integer> getUserIDs(String playername) {
        return playerToIDs.get(playername.toLowerCase());
    }

    /**
     * Check if a UUID is currently being added
     * @param UUID the UUID as string
     * @return currently being added
     */
    public boolean currentlyBeingAdded(String UUID) {
        return uuidsBeingAdded.contains(UUID);
    }

    /**
     * Check if a UUID is currently being added
     * @param UUID the UUID
     * @return currently being added
     */
    public boolean currentlyBeingAdded(UUID UUID) {
        return uuidsBeingAdded.contains(UUID);
    }

    /**
     * Get the UserID of a player
     * @param player The player
     * @return the ID or -1
     */
    public static int getUserID(Player player) {
        UUIDProfile profile = instance.getUUIDProfile(player.getUniqueId().toString());
        if (profile == null) return -1;
        return profile.getUserID();
    }


    public class UUIDProfile {
        //The ID of the UUID in the SH database
        private int userID;
        //The UUID of the player
        private String UUID;

        //The list of names, in the order that they are being used.
        //AKA: names[0] will be the current one. names[1] will be their previous name.
        private ArrayList<NameProfile> names;

        /**
         * Create a new UUID Profile
         * @param userID The ID in the database
         * @param UUID The player's UUID supplied by Mojang
         */
        private UUIDProfile(int userID, String UUID) {
            this.userID = userID;
            this.UUID = UUID;
            names = new ArrayList<>();
        }

        /**
         * Add a new NameProfile to the UUIDProfile
         * @param nProfiles One or more NameProfiles
         */
        public void addNameProfile(NameProfile... nProfiles) {
            for (NameProfile nameProfile : nProfiles) {
                names.add(nameProfile);
            }
            Collections.sort(names);
        }

        /**
         * Add a collection of NameProfiles to the UUIDProfile
         * @param nProfiles The list of profiles
         */
        public void addNameProfiles(Collection<NameProfile> nProfiles) {
            for (NameProfile nameProfile : nProfiles) {
                names.add(nameProfile);
            }
            Collections.sort(names);
        }

        /**
         * Get the player's Database User ID
         * @return The ID
         */
        public int getUserID() {
            return userID;
        }

        /**
         * Get the player's Unique Identifier
         * @return the UUID as String
         */
        public String getUUID() {
            return UUID;
        }

        /**
         * Get the list of used names.
         * It goes from new -> old, thus list[0] is the one currently being used.
         * @return The list with NameProfiles
         */
        public ArrayList<NameProfile> getNames() {
            return names;
        }

        /**
         * Get a player's current username
         * @return the username
         */
        public String getCurrentName() {
            if (!names.isEmpty()) {
                return names.get(0).getPlayername();
            } else {
                return "N/A (UUID: " + getUUID() + ")";
            }
        }

        /**
         * Get the player that belongs to this UUIDProfile.
         * This will only return a result if that player is currently online.
         * @return The player or null
         */
        public Player getPlayer() {
            return Bukkit.getPlayer(java.util.UUID.fromString(UUID));
        }
    }

    public class NameProfile implements Comparable<NameProfile> {
        //The ID of the UUID in the SH database
        private int userID;

        //The name of the player
        private String playername;

        //The timestamp since the player is known under that name
        private long knownSince;

        /**
         * Create a new NameProfile
         * @param userID The The ID in the database
         * @param playername The playername that the player is known under
         * @param knownSince The first time the server has seen this playername in combination with this UUID
         */
        private NameProfile(int userID, String playername, long knownSince) {
            this.userID = userID;
            this.playername = playername;
            this.knownSince = knownSince;
        }

        /**
         * Get the user ID (as specified in the database)
         * @return the ID
         */
        public int getUserID() {
            return userID;
        }

        /**
         * Get the timestamp (in millis) since this username was first known
         * @return the timestamp
         */
        public long getKnownSince() {
            return knownSince;
        }

        /**
         * Get the playername being used in this profile
         * @return the playername
         */
        public String getPlayername() {
            return playername;
        }

        /**
         * Insert the NameProfile into the database
         * WARNING: This is executed in the same thread as the caller.
         */
        private void insertInDatabase() {
            Connection con = SQLPool.getConnection();
            try {
                //Prepare statement
                PreparedStatement prep = con.prepareStatement("INSERT INTO `sh_names`(`user_id`, `name`, `known_since`) VALUES (?, ?, ?)");
                //Set data
                prep.setInt(1, userID);
                prep.setString(2, playername);
                prep.setLong(3, knownSince);
                //Execute
                prep.executeUpdate();

                //Log
                Log.info("New NameProfile added to ID: " + userID + " (" + playername + ")");
            } catch (SQLException e) {
                Log.severe("Failed to insert a new NameProfile into the database. (ID: " + userID + " | P: " + playername + ")");
                e.printStackTrace();
            } finally {
                SQLPool.returnConnection(con);
            }

        }

        @Override
        public int compareTo(NameProfile o) {
            return (int)(o.knownSince - knownSince);
        }
    }

    /**
     * Get the UUIDControl instance
     * @return the UUIDControl
     */
    public static UUIDControl getInstance() {
        return instance;
    }

    /**
     * Initialize a new UUIDControl
     */
    public static void initializeUUIDControl() {
        instance = new UUIDControl();
    }
}
